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

package controllers.events

import controllers.actions._
import derivable.DeriveNumberOfEvents
import forms.events.ConfirmRemoveEventFormProvider
import handlers.ErrorHandler
import javax.inject.Inject
import models.requests.DataRequest
import models.{Index, Mode, MovementReferenceNumber, NormalMode, UserAnswers}
import navigation.Navigator
import pages.events.{ConfirmRemoveEventPage, EventCountryPage, EventPlacePage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import play.twirl.api.Html
import queries.EventQuery
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.{NunjucksSupport, Radios}

import scala.concurrent.{ExecutionContext, Future}

class ConfirmRemoveEventController @Inject() (
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

  private val confirmRemoveEventTemplate = "events/confirmRemoveEvent.njk"

  def onPageLoad(mrn: MovementReferenceNumber, eventIndex: Index, mode: Mode): Action[AnyContent] = (identify andThen getData(mrn) andThen requireData).async {
    implicit request =>
      eventPlaceOrCountry(request.userAnswers, eventIndex) match {
        case Some(placeOrCountry) =>
          renderPage(mrn, eventIndex, mode, formProvider(placeOrCountry), placeOrCountry).map(Ok(_))
        case _ => renderErrorPage(request.userAnswers, eventIndex, mode)
      }
  }

  def onSubmit(mrn: MovementReferenceNumber, eventIndex: Index, mode: Mode): Action[AnyContent] = (identify andThen getData(mrn) andThen requireData).async {
    implicit request =>
      eventPlaceOrCountry(request.userAnswers, eventIndex) match {
        case Some(placeOrCountry) =>
          formProvider(placeOrCountry)
            .bindFromRequest()
            .fold(
              formWithErrors => renderPage(mrn, eventIndex, mode, formWithErrors, placeOrCountry).map(BadRequest(_)),
              value =>
                if (value) {
                  for {
                    updatedAnswers <- Future.fromTry(request.userAnswers.remove(EventQuery(eventIndex)))
                    _              <- sessionRepository.set(updatedAnswers)
                  } yield Redirect(navigator.nextPage(ConfirmRemoveEventPage(eventIndex), mode, updatedAnswers))
                } else {
                  Future.successful(Redirect(navigator.nextPage(ConfirmRemoveEventPage(eventIndex), mode, request.userAnswers)))
                }
            )
        case _ => renderErrorPage(request.userAnswers, eventIndex, mode)
      }

  }

  private def eventPlaceOrCountry(userAnswers: UserAnswers, eventIndex: Index): Option[String] =
    userAnswers.get(EventPlacePage(eventIndex)) match {
      case Some(answer) => Some(answer)
      case _            => userAnswers.get(EventCountryPage(eventIndex)).map(_.code)
    }

  private def renderPage(mrn: MovementReferenceNumber, eventIndex: Index, mode: Mode, form: Form[Boolean], eventTitle: String)(implicit
    request: DataRequest[AnyContent]
  ): Future[Html] = {
    val json = Json.obj(
      "form"        -> form,
      "mode"        -> mode,
      "mrn"         -> mrn,
      "eventTitle"  -> eventTitle,
      "radios"      -> Radios.yesNo(form("value")),
      "onSubmitUrl" -> routes.ConfirmRemoveEventController.onSubmit(mrn, eventIndex, NormalMode).url
    )

    renderer.render(confirmRemoveEventTemplate, json)
  }

  private def renderErrorPage(userAnswers: UserAnswers, eventIndex: Index, mode: Mode)(implicit request: DataRequest[AnyContent]): Future[Result] = {

    val redirectLinkText = if (userAnswers.get(DeriveNumberOfEvents).contains(0)) "noEvent" else "multipleEvent"
    val redirectLink     = navigator.nextPage(ConfirmRemoveEventPage(eventIndex), mode, userAnswers).url

    errorHandler.onConcurrentError(redirectLinkText, redirectLink, "concurrent.event")
  }

}
