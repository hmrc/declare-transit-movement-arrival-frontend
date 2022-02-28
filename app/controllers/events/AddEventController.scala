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

import config.FrontendAppConfig
import controllers.actions._
import derivable.DeriveNumberOfEvents
import forms.events.AddEventFormProvider
import models.requests.DataRequest
import models.{Index, Mode, MovementReferenceNumber, UserAnswers}
import navigation.Navigator
import pages.events.AddEventPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc._
import renderer.Renderer
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.SummaryList.Row
import uk.gov.hmrc.viewmodels.{NunjucksSupport, Radios}
import utils.AddEventsHelper
import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

class AddEventController @Inject() (override val messagesApi: MessagesApi,
                                    navigator: Navigator,
                                    identify: IdentifierAction,
                                    getData: DataRetrievalActionProvider,
                                    requireData: DataRequiredAction,
                                    formProvider: AddEventFormProvider,
                                    val controllerComponents: MessagesControllerComponents,
                                    renderer: Renderer,
                                    config: FrontendAppConfig
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport {

  def onPageLoad(mrn: MovementReferenceNumber, mode: Mode): Action[AnyContent] = (identify andThen getData(mrn) andThen requireData).async {
    implicit request =>
      val form = formProvider(allowMoreEvents(request.userAnswers))
      val preparedForm = request.userAnswers.get(AddEventPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }
      renderView(mrn, mode, preparedForm, Results.Ok)
  }

  def onSubmit(mrn: MovementReferenceNumber, mode: Mode): Action[AnyContent] = (identify andThen getData(mrn) andThen requireData).async {
    implicit request =>
      formProvider(allowMoreEvents(request.userAnswers))
        .bindFromRequest()
        .fold(
          formWithErrors => renderView(mrn, mode, formWithErrors, Results.BadRequest),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(AddEventPage, value))
            } yield Redirect(navigator.nextPage(AddEventPage, mode, updatedAnswers))
        )
  }

  private def renderView(mrn: MovementReferenceNumber, mode: Mode, form: Form[Boolean], status: Results.Status)(implicit
    request: DataRequest[AnyContent]
  ): Future[Result] = {

    val numberOfEvents = request.userAnswers.get(DeriveNumberOfEvents).getOrElse(0)

    val cyaHelper            = new AddEventsHelper(request.userAnswers, mode)
    val listOfEvents         = List.range(0, numberOfEvents).map(Index(_))
    val eventsRows: Seq[Row] = listOfEvents.flatMap(cyaHelper.listOfEvent)

    val singularOrPlural = if (numberOfEvents == 1) "singular" else "plural"

    val json = Json.obj(
      "form"            -> form,
      "mode"            -> mode,
      "mrn"             -> mrn,
      "radios"          -> Radios.yesNo(form("value")),
      "titleOfPage"     -> msg"addEvent.title.$singularOrPlural".withArgs(numberOfEvents),
      "heading"         -> msg"addEvent.heading.$singularOrPlural".withArgs(numberOfEvents),
      "allowMoreEvents" -> allowMoreEvents(request.userAnswers),
      "events"          -> eventsRows
    )

    renderer.render("events/addEvent.njk", json).map(status(_))
  }

  private def allowMoreEvents(ua: UserAnswers): Boolean =
    ua.get(DeriveNumberOfEvents).getOrElse(0) < config.maxEvents
}
