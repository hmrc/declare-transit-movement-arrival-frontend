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

import config.FrontendAppConfig
import controllers.actions._
import javax.inject.Inject
import models.MovementReferenceNumber
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.NunjucksSupport

import scala.concurrent.ExecutionContext

class RejectionConfirmationController @Inject()(override val messagesApi: MessagesApi,
                                                appConfig: FrontendAppConfig,
                                                sessionRepository: SessionRepository,
                                                identify: IdentifierAction,
                                                getData: DataRetrievalActionProvider,
                                                val controllerComponents: MessagesControllerComponents,
                                                renderer: Renderer)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport {

  def onPageLoad(mrn: MovementReferenceNumber): Action[AnyContent] = (identify andThen getData(mrn)).async {
    implicit request =>
      sessionRepository.remove(mrn.toString) flatMap {
        _ =>
          val json = Json.obj(
            "mrn"                       -> mrn,
            "manageTransitMovementsUrl" -> appConfig.manageTransitMovementsUrl
          )

          renderer.render("rejectedArrivalComplete.njk", json).map(Ok(_))
      }
  }
}