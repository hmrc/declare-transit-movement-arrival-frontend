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

package controllers.events.seals

import controllers.actions._
import derivable.DeriveNumberOfSeals
import forms.events.seals.ConfirmRemoveSealFormProvider
import handlers.ErrorHandler
import javax.inject.Inject
import models.domain.SealDomain
import models.requests.DataRequest
import models.{Index, Mode, MovementReferenceNumber}
import navigation.Navigator
import pages.events.seals.{ConfirmRemoveSealPage, SealIdentityPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import play.twirl.api.Html
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.{NunjucksSupport, Radios}

import scala.concurrent.{ExecutionContext, Future}

class ConfirmRemoveSealController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalActionProvider,
  requireData: DataRequiredAction,
  formProvider: ConfirmRemoveSealFormProvider,
  errorHandler: ErrorHandler,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport {

  private val confirmRemoveSealTemplate = "events/seals/confirmRemoveSeal.njk"

  def onPageLoad(mrn: MovementReferenceNumber, eventIndex: Index, sealIndex: Index, mode: Mode): Action[AnyContent] =
    (identify andThen getData(mrn) andThen requireData).async {
      implicit request =>
        request.userAnswers.get(SealIdentityPage(eventIndex, sealIndex)) match {
          case Some(seal) =>
            val form = formProvider(seal)
            renderPage(mrn, eventIndex, sealIndex, mode, form, seal).map(Ok(_))
          case _ =>
            renderErrorPage(eventIndex, mode)
        }
    }

  def onSubmit(mrn: MovementReferenceNumber, eventIndex: Index, sealIndex: Index, mode: Mode): Action[AnyContent] =
    (identify andThen getData(mrn) andThen requireData).async {
      implicit request =>
        request.userAnswers.get(SealIdentityPage(eventIndex, sealIndex)) match {
          case Some(seal) =>
            formProvider(seal)
              .bindFromRequest()
              .fold(
                formWithErrors => renderPage(mrn, eventIndex, sealIndex, mode, formWithErrors, seal).map(BadRequest(_)),
                value =>
                  if (value) {
                    for {
                      updatedAnswers <- Future.fromTry(request.userAnswers.remove(SealIdentityPage(eventIndex, sealIndex)))
                      _              <- sessionRepository.set(updatedAnswers)
                    } yield Redirect(navigator.nextPage(ConfirmRemoveSealPage(eventIndex), mode, updatedAnswers))
                  } else {
                    Future.successful(Redirect(navigator.nextPage(ConfirmRemoveSealPage(eventIndex), mode, request.userAnswers)))
                  }
              )
          case _ =>
            renderErrorPage(eventIndex, mode)
        }
    }

  private def renderPage(mrn: MovementReferenceNumber, eventIndex: Index, sealIndex: Index, mode: Mode, form: Form[Boolean], seal: SealDomain)(implicit
    request: DataRequest[AnyContent]
  ): Future[Html] = {
    val json = Json.obj(
      "form"        -> form,
      "mode"        -> mode,
      "mrn"         -> mrn,
      "sealNumber"  -> seal.numberOrMark,
      "radios"      -> Radios.yesNo(form("value")),
      "onSubmitUrl" -> routes.ConfirmRemoveSealController.onSubmit(mrn, eventIndex, sealIndex, mode).url
    )
    renderer.render(confirmRemoveSealTemplate, json)

  }

  private def renderErrorPage(eventIndex: Index, mode: Mode)(implicit request: DataRequest[AnyContent]): Future[Result] = {
    val redirectLinkText = if (request.userAnswers.get(DeriveNumberOfSeals(eventIndex)).contains(0)) "noSeal" else "multipleSeal"
    val redirectLink     = navigator.nextPage(ConfirmRemoveSealPage(eventIndex), mode, request.userAnswers).url

    errorHandler.onConcurrentError(redirectLinkText, redirectLink, "concurrent.seal")
  }
}
