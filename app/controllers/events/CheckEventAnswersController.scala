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

import com.google.inject.Inject
import controllers.actions.{DataRequiredAction, DataRetrievalActionProvider, IdentifierAction}
import models.{CheckMode, MovementReferenceNumber, NormalMode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.NunjucksSupport
import viewModels.CheckEventAnswersViewModel

import scala.concurrent.{ExecutionContext, Future}

class CheckEventAnswersController @Inject()(override val messagesApi: MessagesApi,
                                            identify: IdentifierAction,
                                            getData: DataRetrievalActionProvider,
                                            requireData: DataRequiredAction,
                                            val controllerComponents: MessagesControllerComponents,
                                            renderer: Renderer)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport {

  def onPageLoad(mrn: MovementReferenceNumber, index: Int): Action[AnyContent] = (identify andThen getData(mrn) andThen requireData).async {
    implicit request =>
      val json = Json.obj(
        "mrn" -> mrn
      ) ++ Json.toJsObject {
        CheckEventAnswersViewModel(request.userAnswers, index, CheckMode)
      }

      renderer.render("events/check-event-answers.njk", json).map(Ok(_))
  }

  def onSubmit(mrn: MovementReferenceNumber, index: Int): Action[AnyContent] = (identify andThen getData(mrn) andThen requireData).async {
    implicit request =>
      Future.successful(Redirect(controllers.events.routes.AddEventController.onPageLoad(mrn, NormalMode)))
  }

}
