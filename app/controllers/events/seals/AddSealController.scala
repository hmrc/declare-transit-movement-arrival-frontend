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

import controllers.actions.{DataRequiredAction, DataRetrievalActionProvider, IdentifierAction}
import derivable.DeriveNumberOfSeals
import forms.events.seals.AddSealFormProvider
import javax.inject.Inject
import models.requests.DataRequest
import models.{Mode, MovementReferenceNumber}
import navigation.Navigator
import pages.events.seals.AddSealPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request, Result}
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.SummaryList.Row
import uk.gov.hmrc.viewmodels.{NunjucksSupport, Radios}
import utils.AddSealHelper

import scala.concurrent.{ExecutionContext, Future}

class AddSealController @Inject()(override val messagesApi: MessagesApi,
                                  sessionRepository: SessionRepository,
                                  navigator: Navigator,
                                  identify: IdentifierAction,
                                  getData: DataRetrievalActionProvider,
                                  requireData: DataRequiredAction,
                                  formProvider: AddSealFormProvider,
                                  val controllerComponents: MessagesControllerComponents,
                                  renderer: Renderer)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport {

  private val form = formProvider()

  def onPageLoad(mrn: MovementReferenceNumber, eventIndex: Int, mode: Mode): Action[AnyContent] = (identify andThen getData(mrn) andThen requireData).async {
    implicit request =>
      val preparedForm = request.userAnswers.get(AddSealPage(eventIndex)) match {
        case None        => form
        case Some(value) => form.fill(value)
      }
      renderPage(mrn, eventIndex, mode, preparedForm, Ok)
  }

  def onSubmit(mrn: MovementReferenceNumber, eventIndex: Int, mode: Mode): Action[AnyContent] = (identify andThen getData(mrn) andThen requireData).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => renderPage(mrn, eventIndex, mode, formWithErrors, BadRequest),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(AddSealPage(eventIndex), value))
            } yield Redirect(navigator.nextPage(AddSealPage(eventIndex), mode, updatedAnswers))
        )
  }

  private def renderPage(mrn: MovementReferenceNumber, eventIndex: Int, mode: Mode, form: Form[Boolean], status: Status)(
    implicit request: DataRequest[AnyContent]): Future[Result] = {

    val numberOfSeals       = request.userAnswers.get(DeriveNumberOfSeals(eventIndex)).getOrElse(0)
    val sealsRows: Seq[Row] = (0 to numberOfSeals).flatMap(AddSealHelper.apply(request.userAnswers).sealRow(eventIndex, _))

    val singularOrPlural = if (numberOfSeals == 1) "singular" else "plural"

    val json = Json.obj(
      "form"      -> form,
      "mode"      -> mode,
      "mrn"       -> mrn,
      "pageTitle" -> msg"addSeal.title.$singularOrPlural".withArgs(numberOfSeals),
      "heading"   -> msg"addSeal.title.$singularOrPlural".withArgs(numberOfSeals),
      "seals"     -> sealsRows,
      "radios"    -> Radios.yesNo(form("value"))
    )

    renderer.render("events/seals/addSeal.njk", json).map(status(_))
  }
}
