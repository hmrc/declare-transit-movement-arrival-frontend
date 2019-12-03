/*
 * Copyright 2019 HM Revenue & Customs
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

import com.google.inject.Inject
import controllers.actions.DataRequiredAction
import controllers.actions.DataRetrievalActionProvider
import controllers.actions.IdentifierAction
import models.MovementReferenceNumber
import models.UserAnswers
import play.api.i18n.I18nSupport
import play.api.i18n.Messages
import play.api.i18n.MessagesApi
import play.api.libs.json.Json
import play.api.mvc.Action
import play.api.mvc.AnyContent
import play.api.mvc.MessagesControllerComponents
import renderer.Renderer
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.NunjucksSupport
import uk.gov.hmrc.viewmodels.SummaryList.Row
import utils.CheckYourAnswersHelper
import viewModels.Section

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class CheckEventAnswersController @Inject()(
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalActionProvider,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport {

  def onPageLoad(mrn: MovementReferenceNumber, index: Int): Action[AnyContent] = (identify andThen getData(mrn) andThen requireData).async {
    implicit request =>
      val json = Json.obj(
        "sections" -> Json.toJson(completeSections(request.userAnswers, index)),
        "mrn"      -> mrn
      )
      renderer.render("events/check-event-answers.njk", json).map(Ok(_))
  }

  def onSubmit(mrn: MovementReferenceNumber): Action[AnyContent] = (identify andThen getData(mrn) andThen requireData).async {
    implicit request =>
      Future.successful(Redirect(controllers.routes.CheckYourAnswersController.onPageLoad(mrn)))
  }

  private def completeSections(userAnswers: UserAnswers, index: Int)(implicit messages: Messages): Seq[Section] = {
    val helper = new CheckYourAnswersHelper(userAnswers)
    Seq(
      Section(messages("checkEventAnswers.section.events"), eventsSection(helper, index)),
      Section(messages("checkEventAnswers.section.vehicleOrContainer"), isTranshipmentSection(helper, index))
    )
  }

  private def eventsSection(helper: CheckYourAnswersHelper, index: Int): Seq[Row] =
    Seq(
      helper.eventCountry(index),
      helper.eventPlace(index),
      helper.eventReported(index),
      helper.isTranshipment(index),
      helper.incidentInformation(index)
    ).flatten

  private def isTranshipmentSection(helper: CheckYourAnswersHelper, index: Int): Seq[Row] =
    Seq(
      helper.isTranshipment(index)
    ).flatten
}
