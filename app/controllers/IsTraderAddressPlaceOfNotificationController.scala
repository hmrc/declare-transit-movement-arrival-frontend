/*
 * Copyright 2019 HM Revenue & Customs
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

import controllers.actions._
import forms.IsTraderAddressPlaceOfNotificationFormProvider
import javax.inject.Inject
import models._
import navigation.Navigator
import pages.{IsTraderAddressPlaceOfNotificationPage, TraderAddressPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.{NunjucksSupport, Radios}

import scala.concurrent.{ExecutionContext, Future}

class IsTraderAddressPlaceOfNotificationController @Inject()(
                                                              override val messagesApi: MessagesApi,
                                                              sessionRepository: SessionRepository,
                                                              navigator: Navigator,
                                                              identify: IdentifierAction,
                                                              getData: DataRetrievalActionProvider,
                                                              requireData: DataRequiredAction,
                                                              formProvider: IsTraderAddressPlaceOfNotificationFormProvider,
                                                              val controllerComponents: MessagesControllerComponents,
                                                              renderer: Renderer
                                                            )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with uk.gov.hmrc.nunjucks.NunjucksSupport {


  def onPageLoad(mrn: MovementReferenceNumber, mode: Mode): Action[AnyContent] = (identify andThen getData(mrn) andThen requireData).async {
    implicit request =>

      request.userAnswers.get(TraderAddressPage) match {
        case Some(traderAddress) => {
          val postcode = traderAddress.postcode
          val form = formProvider(postcode)

          val preparedForm = request.userAnswers.get(IsTraderAddressPlaceOfNotificationPage) match {
            case None => form
            case Some(value) => form.fill(value)
          }

          val json = Json.obj(
            "form" -> preparedForm,
            "mode" -> mode,
            "mrn" -> mrn,
            "traderPostcode" -> postcode,
            "radios" -> Radios.yesNo(preparedForm("value"))
          )

          renderer.render("isTraderAddressPlaceOfNotification.njk", json).map(Ok(_))
        }
        case _ => Future.successful(Redirect(routes.SessionExpiredController.onPageLoad()))
      }

  }

  def onSubmit(mrn: MovementReferenceNumber, mode: Mode): Action[AnyContent] = (identify andThen getData(mrn) andThen requireData).async {
    implicit request =>

      request.userAnswers.get(TraderAddressPage) match {
        case Some(traderAddress) => {
          val postcode = traderAddress.postcode
          val form = formProvider(postcode)

          form.bindFromRequest().fold(
            formWithErrors => {

              val json = Json.obj(
                "form" -> formWithErrors,
                "mode" -> mode,
                "mrn" -> mrn,
                "traderPostcode" -> postcode,
                "radios" -> Radios.yesNo(formWithErrors("value"))
              )

              renderer.render("isTraderAddressPlaceOfNotification.njk", json).map(BadRequest(_))
            },
            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(IsTraderAddressPlaceOfNotificationPage, value))
                _ <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(IsTraderAddressPlaceOfNotificationPage, mode, updatedAnswers))
          )
        }
        case _ => Future.successful(Redirect(routes.SessionExpiredController.onPageLoad()))
      }
  }
}
