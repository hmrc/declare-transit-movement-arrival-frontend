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

import derivable.DeriveNumberOfEvents
import controllers.actions._
import forms.events.AddEventFormProvider
import javax.inject.Inject
import models.requests.DataRequest
import models.{ArrivalUniqueRef, Index, Mode, MovementReferenceNumber}
import navigation.Navigator
import pages.events.AddEventPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc._
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.SummaryList.Row
import uk.gov.hmrc.viewmodels.{NunjucksSupport, Radios}
import utils.AddEventsHelper

import scala.concurrent.{ExecutionContext, Future}

class AddEventController @Inject()(override val messagesApi: MessagesApi,
                                   navigator: Navigator,
                                   identify: IdentifierAction,
                                   getData: DataRetrievalActionProvider,
                                   requireData: DataRequiredAction,
                                   formProvider: AddEventFormProvider,
                                   val controllerComponents: MessagesControllerComponents,
                                   renderer: Renderer)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport {

  private val form = formProvider()

  def onPageLoad(ref: ArrivalUniqueRef, mode: Mode): Action[AnyContent] = (identify andThen getData(ref) andThen requireData).async {
    implicit request =>
      val preparedForm = request.userAnswers.get(AddEventPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }
      renderView(ref, mode, preparedForm, Results.Ok)
  }

  def onSubmit(ref: ArrivalUniqueRef, mode: Mode): Action[AnyContent] = (identify andThen getData(ref) andThen requireData).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => {
            renderView(ref, mode, formWithErrors, Results.BadRequest)
          },
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(AddEventPage, value))
            } yield Redirect(navigator.nextPage(AddEventPage, mode, updatedAnswers))
        )
  }

  private def renderView(ref: ArrivalUniqueRef, mode: Mode, form: Form[Boolean], status: Results.Status)(
    implicit request: DataRequest[AnyContent]): Future[Result] = {

    val numberOfEvents = request.userAnswers.get(DeriveNumberOfEvents).getOrElse(0)

    val cyaHelper            = new AddEventsHelper(request.userAnswers)
    val listOfEvents         = List.range(0, numberOfEvents).map(Index(_))
    val eventsRows: Seq[Row] = listOfEvents.flatMap(cyaHelper.listOfEvent)

    val title =
      if (numberOfEvents == 1)
        msg"addEvent.title.singular".withArgs(numberOfEvents)
      else
        msg"addEvent.title.plural".withArgs(numberOfEvents)

    val heading =
      if (numberOfEvents == 1)
        msg"addEvent.heading.singular".withArgs(numberOfEvents)
      else
        msg"addEvent.heading.plural".withArgs(numberOfEvents)

    val json = Json.obj(
      "form"        -> form,
      "mode"        -> mode,
      "ref"         -> ref,
      "radios"      -> Radios.yesNo(form("value")),
      "titleOfPage" -> title,
      "heading"     -> heading,
      "events"      -> eventsRows
    )

    renderer.render("events/addEvent.njk", json).map(status(_))
  }
}
