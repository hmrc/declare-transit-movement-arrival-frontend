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

package controllers.events

import controllers.actions._
import derivable.DeriveNumberOfEvents
import forms.events.ConfirmRemoveEventFormProvider
import handlers.ErrorHandler
import javax.inject.Inject
import models.requests.DataRequest
import models.{Index, Mode, MovementReferenceNumber, UserAnswers}
import navigation.Navigator
import pages.events.{ConfirmRemoveEventPage, EventCountryPage, EventPlacePage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import queries.EventQuery
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.{NunjucksSupport, Radios}

import scala.concurrent.{ExecutionContext, Future}

class ConfirmRemoveEventController @Inject()(
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalActionProvider,
  requireData: DataRequiredAction,
  errorHandler: ErrorHandler,
  formProvider: ConfirmRemoveEventFormProvider,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport {

  private val form                       = formProvider()
  private val confirmRemoveEventTemplate = "events/confirmRemoveEvent.njk"

  def onPageLoad(mrn: MovementReferenceNumber, eventIndex: Index, mode: Mode): Action[AnyContent] = (identify andThen getData(mrn) andThen requireData).async {
    implicit request =>
      renderPage(mrn, eventIndex, mode, form, Ok)
  }

  def onSubmit(mrn: MovementReferenceNumber, eventIndex: Index, mode: Mode): Action[AnyContent] = (identify andThen getData(mrn) andThen requireData).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => renderPage(mrn, eventIndex, mode, formWithErrors, BadRequest),
          value => {
            if (value) {
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.remove(EventQuery(eventIndex)))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(ConfirmRemoveEventPage(eventIndex), mode, updatedAnswers))
            } else {
              Future.successful(Redirect(navigator.nextPage(ConfirmRemoveEventPage(eventIndex), mode, request.userAnswers)))
            }
          }
        )
  }

  private def eventPlace(userAnswers: UserAnswers, eventIndex: Index): Option[String] = userAnswers.get(EventPlacePage(eventIndex)) match {
    case Some(answer) => Some(answer)
    case _            => userAnswers.get(EventCountryPage(eventIndex)).map(_.code)
  }

  private def renderPage(mrn: MovementReferenceNumber, eventIndex: Index, mode: Mode, form: Form[Boolean], status: Status)(
    implicit request: DataRequest[AnyContent]): Future[Result] =
    eventPlace(request.userAnswers, eventIndex) match {

      case Some(place) =>
        val json = Json.obj(
          "form"       -> form,
          "mode"       -> mode,
          "mrn"        -> mrn,
          "eventTitle" -> place,
          "radios"     -> Radios.yesNo(form("value"))
        )
        renderer.render(confirmRemoveEventTemplate, json).map(status(_))

      case _ =>
        val redirectLinkText = if (request.userAnswers.get(DeriveNumberOfEvents).contains(0)) "noEvent" else "multipleEvent"
        val redirectLink     = navigator.nextPage(ConfirmRemoveEventPage(eventIndex), mode, request.userAnswers).url

        errorHandler.onConcurrentError(redirectLinkText, redirectLink, "concurrent.event")
    }

}
