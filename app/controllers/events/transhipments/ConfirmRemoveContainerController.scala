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

package controllers.events.transhipments

import controllers.actions.{DataRequiredAction, DataRetrievalActionProvider, IdentifierAction}
import derivable.DeriveNumberOfContainers
import forms.events.transhipments.ConfirmRemoveContainerFormProvider
import handlers.ErrorHandler
import javax.inject.Inject
import models.domain.ContainerDomain
import models.requests.DataRequest
import models.{Index, Mode, MovementReferenceNumber, UserAnswers}
import navigation.Navigator
import pages.events.transhipments.{ConfirmRemoveContainerPage, ContainerNumberPage}
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

class ConfirmRemoveContainerController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalActionProvider,
  requireData: DataRequiredAction,
  errorHandler: ErrorHandler,
  formProvider: ConfirmRemoveContainerFormProvider,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport {

  private val confirmRemoveContainerTemplate = "events/transhipments/confirmRemoveContainer.njk"

  def onPageLoad(mrn: MovementReferenceNumber, eventIndex: Index, containerIndex: Index, mode: Mode): Action[AnyContent] =
    (identify andThen getData(mrn) andThen requireData).async {
      implicit request =>
        request.userAnswers.get(ContainerNumberPage(eventIndex, containerIndex)) match {
          case Some(container) =>
            val form = formProvider(container)
            renderPage(mrn, eventIndex, containerIndex, form, mode, container).map(Ok(_))
          case _ =>
            renderErrorPage(eventIndex, mode, request.userAnswers)
        }

    }

  def onSubmit(mrn: MovementReferenceNumber, eventIndex: Index, containerIndex: Index, mode: Mode): Action[AnyContent] =
    (identify andThen getData(mrn) andThen requireData).async {
      implicit request =>
        request.userAnswers.get(ContainerNumberPage(eventIndex, containerIndex)) match {
          case Some(container) =>
            formProvider(container)
              .bindFromRequest()
              .fold(
                formWithErrors => renderPage(mrn, eventIndex, containerIndex, formWithErrors, mode, container).map(BadRequest(_)),
                value =>
                  if (value) {
                    for {
                      updatedAnswers <- Future.fromTry(request.userAnswers.remove(ContainerNumberPage(eventIndex, containerIndex)))
                      _              <- sessionRepository.set(updatedAnswers)
                    } yield Redirect(navigator.nextPage(ConfirmRemoveContainerPage(eventIndex), mode, updatedAnswers))
                  } else {
                    Future.successful(Redirect(navigator.nextPage(ConfirmRemoveContainerPage(eventIndex), mode, request.userAnswers)))
                  }
              )
          case _ =>
            renderErrorPage(eventIndex, mode, request.userAnswers)
        }
    }

  private def renderPage(mrn: MovementReferenceNumber, eventIndex: Index, containerIndex: Index, form: Form[Boolean], mode: Mode, container: ContainerDomain)(
    implicit request: DataRequest[AnyContent]
  ): Future[Html] = {

    val json = Json.obj(
      "form"            -> form,
      "mode"            -> mode,
      "mrn"             -> mrn,
      "containerNumber" -> container.containerNumber,
      "radios"          -> Radios.yesNo(form("value")),
      "onSubmitUrl"     -> routes.ConfirmRemoveContainerController.onSubmit(mrn, eventIndex, containerIndex, mode).url
    )

    renderer.render(confirmRemoveContainerTemplate, json)
  }

  private def renderErrorPage(eventIndex: Index, mode: Mode, userAnswers: UserAnswers)(implicit request: DataRequest[AnyContent]): Future[Result] = {
    val redirectLinkText = if (userAnswers.get(DeriveNumberOfContainers(eventIndex)).contains(0)) "noContainer" else "multipleContainer"
    val redirectLink     = navigator.nextPage(ConfirmRemoveContainerPage(eventIndex), mode, userAnswers).url

    errorHandler.onConcurrentError(redirectLinkText, redirectLink, "concurrent.container")
  }

}
