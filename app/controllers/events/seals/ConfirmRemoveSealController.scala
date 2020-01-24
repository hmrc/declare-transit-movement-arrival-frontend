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

package controllers.events.seals

import controllers.actions._
import forms.events.seals.ConfirmRemoveSealFormProvider
import javax.inject.Inject
import models.requests.DataRequest
import models.{Mode, MovementReferenceNumber}
import navigation.Navigator
import pages.events.seals.{ConfirmRemoveSealPage, SealIdentityPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.Html
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.{NunjucksSupport, Radios}

import scala.concurrent.{ExecutionContext, Future}

class ConfirmRemoveSealController @Inject()(
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalActionProvider,
  requireData: DataRequiredAction,
  formProvider: ConfirmRemoveSealFormProvider,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport {

  private val form = formProvider()

  def onPageLoad(mrn: MovementReferenceNumber, eventIndex: Int, sealIndex: Int, mode: Mode): Action[AnyContent] =
    (identify andThen getData(mrn) andThen requireData).async {
      implicit request =>
        request.userAnswers.get(SealIdentityPage(eventIndex, sealIndex)) match {
          case Some(sealNumber) =>
            renderView(mrn, mode, sealNumber, form).map(Ok(_))
          case _ => Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
        }
    }

  private def renderView(mrn: MovementReferenceNumber, mode: Mode, sealNumber: String, form: Form[Boolean])(
    implicit request: DataRequest[AnyContent]): Future[Html] = {
    val json = Json.obj(
      "form"       -> form,
      "mode"       -> mode,
      "mrn"        -> mrn,
      "sealNumber" -> sealNumber,
      "radios"     -> Radios.yesNo(form("value"))
    )
    renderer.render("events/seals/confirmRemoveSeal.njk", json)
  }

  def onSubmit(mrn: MovementReferenceNumber, eventIndex: Int, sealIndex: Int, mode: Mode): Action[AnyContent] =
    (identify andThen getData(mrn) andThen requireData).async {
      implicit request =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors => {
              request.userAnswers.get(SealIdentityPage(eventIndex, sealIndex)) match {
                case Some(sealNumber) =>
                  renderView(mrn, mode, sealNumber, formWithErrors).map(BadRequest(_))
                case _ => Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
              }
            },
            value =>
              value match {
                case true =>
                  for {
                    updatedAnswers <- Future.fromTry(request.userAnswers.remove(SealIdentityPage(eventIndex, sealIndex)))
                    _              <- sessionRepository.set(updatedAnswers)
                  } yield Redirect(navigator.nextPage(ConfirmRemoveSealPage(eventIndex), mode, updatedAnswers))

                case false => Future.successful(Redirect(navigator.nextPage(ConfirmRemoveSealPage(eventIndex), mode, request.userAnswers)))
            }
          )
    }
}
