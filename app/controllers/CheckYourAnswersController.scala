/*
 * Copyright 2020 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import com.google.inject.Inject
import controllers.actions.{DataRequiredAction, DataRetrievalActionProvider, IdentifierAction}
import derivable.DeriveNumberOfEvents
import handlers.ErrorHandler
import models.GoodsLocation.BorderForceOffice
import models.{GoodsLocation, Index, MovementReferenceNumber, UserAnswers}
import pages.{AuthorisedLocationPage, ConsigneeEoriConfirmationPage, GoodsLocationPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.Results.BadRequest
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import services.ArrivalNotificationService
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.{NunjucksSupport, SummaryList}
import utils.{AddEventsHelper, CheckYourAnswersHelper}
import viewModels.sections.Section
import uk.gov.hmrc.http.HttpErrorFunctions

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject()(override val messagesApi: MessagesApi,
                                           identify: IdentifierAction,
                                           getData: DataRetrievalActionProvider,
                                           requireData: DataRequiredAction,
                                           service: ArrivalNotificationService,
                                           errorHandler: ErrorHandler,
                                           val controllerComponents: MessagesControllerComponents,
                                           renderer: Renderer)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport
    with HttpErrorFunctions {

  def onPageLoad(mrn: MovementReferenceNumber): Action[AnyContent] = (identify andThen getData(mrn) andThen requireData).async {
    implicit request =>
      val answers: Seq[Section] = createSections(request.userAnswers, request.eoriNumber)

      val json = Json.obj(
        "sections" -> Json.toJson(answers),
        "mrn"      -> mrn
      )
      renderer.render("check-your-answers.njk", json).map(Ok(_))
  }

  def onPost(mrn: MovementReferenceNumber): Action[AnyContent] =
    (identify andThen getData(mrn) andThen requireData).async {
      implicit request =>
        service.submit(request.userAnswers, request.eoriNumber) flatMap {
          case Some(result) =>
            result.status match {
              case status if is2xx(status) => Future.successful(Redirect(routes.ConfirmationController.onPageLoad(mrn)))
              case status if is4xx(status) => errorHandler.onClientError(request, status)
              case _                       => Future.successful(Redirect(routes.TechnicalDifficultiesController.onPageLoad()))
            }
          case None => errorHandler.onClientError(request, BAD_REQUEST) //TODO waiting for design
        }
    }

  private def createSections(userAnswers: UserAnswers, eori: String): Seq[Section] = {
    val helper = new CheckYourAnswersHelper(userAnswers)

    val mrn = Section(Seq(helper.movementReferenceNumber))
    val whereAreTheGoods: Section = (
      userAnswers.get(GoodsLocationPage) match {
        case Some(BorderForceOffice) =>
          Section(
            msg"checkYourAnswers.section.goodsLocation",
            Seq(helper.goodsLocation, helper.authorisedLocation, helper.customsSubPlace, helper.presentationOffice).flatten
          )
        case _ =>
          Section(
            msg"checkYourAnswers.section.goodsLocation",
            Seq(helper.goodsLocation, helper.authorisedLocation).flatten
          )
      }
    )
    val traderDetails = Section(
      msg"checkYourAnswers.section.traderDetails",
      Seq(
        helper.traderName,
        helper.traderEori,
        helper.traderAddress
      ).flatten
    )
    val consigneeDetails = Section(
      msg"checkYourAnswers.section.consigneeDetails",
      Seq(
        helper.consigneeName,
        helper.eoriConfirmation(eori),
        userAnswers.get(ConsigneeEoriConfirmationPage) match {
          case Some(false) => helper.eoriNumber
          case _           => None
        },
        helper.consigneeAddress
      ).flatten
    )
    val placeOfNotification = Section(
      msg"checkYourAnswers.section.placeOfNotificationDetails",
      Seq(
        helper.isTraderAddressPlaceOfNotification,
        helper.placeOfNotification
      ).flatten
    )
    val eventSeq = helper.incidentOnRoute.toSeq ++ eventList(userAnswers)
    val events   = Section(msg"checkYourAnswers.section.events", eventSeq)

    userAnswers.get(GoodsLocationPage) match {
      case Some(BorderForceOffice) => Seq(mrn, whereAreTheGoods, traderDetails, placeOfNotification, events)
      case _                       => Seq(mrn, whereAreTheGoods, consigneeDetails, events)
    }
  }
  private def eventList(userAnswers: UserAnswers): Seq[SummaryList.Row] = {
    val numberOfEvents = userAnswers.get(DeriveNumberOfEvents).getOrElse(0)
    val cyaHelper      = new AddEventsHelper(userAnswers)
    val listOfEvents   = List.range(0, numberOfEvents).map(Index(_))
    listOfEvents.flatMap(cyaHelper.cyaListOfEvent)
  }
}
