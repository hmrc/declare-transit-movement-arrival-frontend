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
import forms.events.ConfirmRemoveEventFormProvider
import javax.inject.Inject
import models.{Mode, MovementReferenceNumber, UserAnswers}
import navigation.Navigator
import pages.events.{ConfirmRemoveEventPage, EventCountryPage, EventPlacePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
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
  formProvider: ConfirmRemoveEventFormProvider,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport {

  private val form                       = formProvider()
  private val confirmRemoveEventTemplate = "events/confirmRemoveEvent.njk"

  def onPageLoad(mrn: MovementReferenceNumber, eventIndex: Int, mode: Mode): Action[AnyContent] = (identify andThen getData(mrn) andThen requireData).async {
    implicit request =>
      eventPlace(request.userAnswers, eventIndex) match {
        case Some(place) => {
          val json = Json.obj(
            "form"       -> form,
            "mode"       -> mode,
            "mrn"        -> mrn,
            "eventTitle" -> place,
            "radios"     -> Radios.yesNo(form("value"))
          )

          renderer.render(confirmRemoveEventTemplate, json).map(Ok(_))
        }
        case _ => Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
      }
  }

  def onSubmit(mrn: MovementReferenceNumber, eventIndex: Int, mode: Mode): Action[AnyContent] = (identify andThen getData(mrn) andThen requireData).async {
    implicit request =>
      eventPlace(request.userAnswers, eventIndex) match {
        case Some(place) => {
          form
            .bindFromRequest()
            .fold(
              formWithErrors => {

                val json = Json.obj(
                  "form"       -> formWithErrors,
                  "mode"       -> mode,
                  "mrn"        -> mrn,
                  "eventTitle" -> place,
                  "radios"     -> Radios.yesNo(formWithErrors("value"))
                )

                renderer.render(confirmRemoveEventTemplate, json).map(BadRequest(_))
              },
              userAnswer =>
                userAnswer match {
                  case true =>
                    for {
                      updatedAnswers <- Future.fromTry(request.userAnswers.remove(EventQuery(eventIndex)))
                      _              <- sessionRepository.set(updatedAnswers)
                    } yield Redirect(navigator.nextPage(ConfirmRemoveEventPage(eventIndex), mode, updatedAnswers))
                  case false => Future.successful(Redirect(navigator.nextPage(ConfirmRemoveEventPage(eventIndex), mode, request.userAnswers)))

              }
            )
        }
        case _ => Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
      }
  }

  private def eventPlace(userAnswers: UserAnswers, eventIndex: Int) = userAnswers.get(EventPlacePage(eventIndex)) match {
    case Some(answer) => Some(answer)
    case _            => userAnswers.get(EventCountryPage(eventIndex)).map(_.code)
  }
}
