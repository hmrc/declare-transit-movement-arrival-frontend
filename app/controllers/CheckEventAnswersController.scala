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

package controllers

import com.google.inject.Inject
import controllers.actions.{DataRequiredAction, DataRetrievalActionProvider, IdentifierAction}
import models.{MovementReferenceNumber, UserAnswers}
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.NunjucksSupport
import utils.CheckYourAnswersHelper
import viewModels.Section

import scala.concurrent.ExecutionContext

class CheckEventAnswersController @Inject()(
                                             override val messagesApi: MessagesApi,
                                             identify: IdentifierAction,
                                             getData: DataRetrievalActionProvider,
                                             requireData: DataRequiredAction,
                                             val controllerComponents: MessagesControllerComponents,
                                             renderer: Renderer
                                           )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with NunjucksSupport {


  def onPageLoad(mrn: MovementReferenceNumber): Action[AnyContent] = (identify andThen getData(mrn) andThen requireData).async {
    implicit request =>

      val sections: Seq[Section] = eventSections(request.userAnswers)
      val json = Json.obj(
        "sections" -> Json.toJson(sections),
        "mrn" -> mrn
      )
      renderer.render("check-event-answers.njk", json).map(Ok(_))
  }

  private def eventSections(userAnswers: UserAnswers)(implicit messages: Messages): Seq[Section] = {
    val helper = new CheckYourAnswersHelper(userAnswers)
    val events = Seq(
      helper.eventCountry,
      helper.eventPlace,
      helper.eventReported,
      helper.isTranshipment,
      helper.incidentInformation,
      helper.sealsChanged
    ).flatten

    Seq(Section(None, events))
  }
}
