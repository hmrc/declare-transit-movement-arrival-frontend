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
import derivable.DeriveNumberOfEvents
import controllers.actions.{DataRequiredAction, DataRetrievalActionProvider, IdentifierAction}
import handlers.ErrorHandler
import models.{MovementReferenceNumber, UserAnswers}
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import services.ArrivalNotificationService
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.SummaryList.Row
import uk.gov.hmrc.viewmodels.{NunjucksSupport, SummaryList}
import utils.{AddEventsHelper, CheckYourAnswersHelper}
import viewModels.Section

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
    with NunjucksSupport {

  def onPageLoad(mrn: MovementReferenceNumber): Action[AnyContent] = (identify andThen getData(mrn) andThen requireData).async {
    implicit request =>
      val answers: Seq[Section] = createSections(request.userAnswers)

      val json = Json.obj(
        "sections" -> Json.toJson(answers),
        "mrn"      -> mrn
      )
      renderer.render("check-your-answers.njk", json).map(Ok(_))
  }

  def onPost(mrn: MovementReferenceNumber): Action[AnyContent] =
    (identify andThen getData(mrn) andThen requireData).async {
      implicit request =>
        service.submit(request.userAnswers) flatMap {
          case Some(result) =>
            result.status match {
              case OK | NO_CONTENT => Future.successful(Redirect(routes.ConfirmationController.onPageLoad(mrn)))
              case status          => errorHandler.onClientError(request, status)
            }
          case None => errorHandler.onClientError(request, BAD_REQUEST) //TODO waiting for design
        }
    }

  private def createSections(userAnswers: UserAnswers)(implicit messages: Messages): Seq[Section] = {
    val helper = new CheckYourAnswersHelper(userAnswers)

    val mrn = Section(Seq(helper.movementReferenceNumber))
    val goodsLocation =
      Section(messages("checkYourAnswers.section.goodsLocation"), Seq(helper.goodsLocation, helper.authorisedLocation, helper.presentationOffice).flatten)
    val traderDetails = Section(messages("checkYourAnswers.section.traderDetails"), Seq(helper.traderName, helper.traderAddress, helper.traderEori).flatten)
    val notificationDetails =
      Section(messages("checkYourAnswers.section.notificationDetails"), Seq(helper.isTraderAddressPlaceOfNotification, helper.placeOfNotification).flatten)
    val events = Section(messages("checkYourAnswers.section.events"), eventList(userAnswers))

    Seq(mrn, goodsLocation, traderDetails, notificationDetails, events)
  }

  private def eventList(userAnswers: UserAnswers): Seq[SummaryList.Row] = {
    val numberOfEvents = userAnswers.get(DeriveNumberOfEvents).getOrElse(0)
    val cyaHelper      = new AddEventsHelper(userAnswers)

    (0 to numberOfEvents).flatMap(cyaHelper.cyaListOfEvent)
  }
}
