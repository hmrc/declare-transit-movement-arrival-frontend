/*
 * Copyright 2022 HM Revenue & Customs
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
import models.requests.DataRequest
import navigation.Navigator
import pages.{IsTraderAddressPlaceOfNotificationPage, TraderAddressPage, TraderNamePage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.Html
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.Radios

import scala.concurrent.{ExecutionContext, Future}

class IsTraderAddressPlaceOfNotificationController @Inject() (override val messagesApi: MessagesApi,
                                                              sessionRepository: SessionRepository,
                                                              navigator: Navigator,
                                                              identify: IdentifierAction,
                                                              getData: DataRetrievalActionProvider,
                                                              requireData: DataRequiredAction,
                                                              formProvider: IsTraderAddressPlaceOfNotificationFormProvider,
                                                              val controllerComponents: MessagesControllerComponents,
                                                              renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with uk.gov.hmrc.nunjucks.NunjucksSupport {

  def onPageLoad(mrn: MovementReferenceNumber, mode: Mode): Action[AnyContent] =
    (identify andThen getData(mrn) andThen requireData).async {
      implicit request =>
        request.userAnswers.get(TraderAddressPage) match {
          case Some(traderAddress) =>
            val traderName = request.userAnswers.get(TraderNamePage).getOrElse("")
            val form       = formProvider(traderName)

            val preparedForm = request.userAnswers.get(IsTraderAddressPlaceOfNotificationPage) match {
              case None        => form
              case Some(value) => form.fill(value)
            }

            renderPage(preparedForm, traderAddress, mode)
              .map(Ok(_))
          case _ => Future.successful(Redirect(routes.SessionExpiredController.onPageLoad()))
        }

    }

  def onSubmit(mrn: MovementReferenceNumber, mode: Mode): Action[AnyContent] =
    (identify andThen getData(mrn) andThen requireData).async {
      implicit request =>
        request.userAnswers.get(TraderAddressPage) match {
          case Some(traderAddress) =>
            val traderName = request.userAnswers.get(TraderNamePage).getOrElse("")
            val form       = formProvider(traderName)

            form
              .bindFromRequest()
              .fold(
                formWithErrors =>
                  renderPage(formWithErrors, traderAddress, mode)
                    .map(BadRequest(_)),
                value =>
                  for {
                    updatedAnswers <- Future.fromTry(request.userAnswers.set(IsTraderAddressPlaceOfNotificationPage, value))
                    _              <- sessionRepository.set(updatedAnswers)
                  } yield Redirect(navigator.nextPage(IsTraderAddressPlaceOfNotificationPage, mode, updatedAnswers))
              )
          case _ => Future.successful(Redirect(routes.SessionExpiredController.onPageLoad()))
        }
    }

  private def renderPage(form: Form[Boolean], traderAddress: Address, mode: Mode)(implicit request: DataRequest[AnyContent]): Future[Html] = {
    val addressLine1    = traderAddress.buildingAndStreet
    val addressTown     = traderAddress.city
    val addressPostcode = traderAddress.postcode

    val traderName = request.userAnswers.get(TraderNamePage).getOrElse("")

    val json = Json.obj(
      "form"           -> form,
      "mode"           -> mode,
      "mrn"            -> request.userAnswers.movementReferenceNumber,
      "traderLine1"    -> addressLine1,
      "traderTown"     -> addressTown,
      "traderPostcode" -> addressPostcode,
      "radios"         -> Radios.yesNo(form("value")),
      "traderName"     -> traderName
    )

    renderer.render("isTraderAddressPlaceOfNotification.njk", json)
  }
}
