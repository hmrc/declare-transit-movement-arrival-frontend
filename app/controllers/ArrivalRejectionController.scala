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
import connectors.ArrivalMovementConnector
import controllers.actions._
import javax.inject.Inject
import models.messages.ArrivalNotificationRejectionMessage
import models.{ArrivalId, MessageId}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController

import scala.concurrent.{ExecutionContext, Future}

class ArrivalRejectionController @Inject()(
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer,
  arrivalMovementConnector: ArrivalMovementConnector,
  frontendAppConfig: FrontendAppConfig
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(arrivalId: ArrivalId, messageId: MessageId): Action[AnyContent] = identify.async {
    implicit request =>
      if (frontendAppConfig.featureToggleArrivalRejection) {
        arrivalMovementConnector.getRejectionMessage(arrivalId, messageId) flatMap {
          rejectionMessage: ArrivalNotificationRejectionMessage =>
            val json = Json.obj("mrn" -> rejectionMessage.movementReferenceNumber, "errors" -> rejectionMessage.errors)

            renderer.render("arrivalRejection.njk", json).map(Ok(_))
        }
      } else {
        Future.successful(Redirect(routes.UnauthorisedController.onPageLoad()))
      }
  }
}
