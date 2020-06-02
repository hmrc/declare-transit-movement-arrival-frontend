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

package controllers

import com.google.inject.Inject
import controllers.actions.{DataRequiredAction, DataRetrievalActionProvider, IdentifierAction}
import derivable.DeriveNumberOfEvents
import handlers.ErrorHandler
import models.{ArrivalId, Index, MovementReferenceNumber, UserAnswers}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import services.ArrivalNotificationService
import uk.gov.hmrc.http.HttpErrorFunctions
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.{NunjucksSupport, SummaryList}
import utils.{AddEventsHelper, CheckYourAnswersHelper}
import viewModels.sections.Section

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersRejectionsController @Inject()(override val messagesApi: MessagesApi,
                                                     identify: IdentifierAction,
                                                     getData: DataRetrievalActionProvider,
                                                     val controllerComponents: MessagesControllerComponents,
                                                     renderer: Renderer)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport
    with HttpErrorFunctions {

  def onPageLoad(mrn: MovementReferenceNumber, arrivalId: ArrivalId): Action[AnyContent] = (identify andThen getData(mrn)).async {
    implicit request =>
      val helper = new CheckYourAnswersHelper(UserAnswers(mrn))

      val json = Json.obj(
        "sections" -> Json.toJson(Seq(Section(Seq(helper.movementReferenceNumber))))
      )
      renderer.render("check-your-answers.njk", json).map(Ok(_))
  }

  def onPost(mrn: MovementReferenceNumber, arrivalId: ArrivalId): Action[AnyContent] = ???

}
