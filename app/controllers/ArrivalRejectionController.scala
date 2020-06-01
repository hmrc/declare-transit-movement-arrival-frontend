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
import models.ArrivalId
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import services.ArrivalRejectionService
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController

import scala.concurrent.{ExecutionContext, Future}

class ArrivalRejectionController @Inject()(
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer,
  appConfig: FrontendAppConfig,
  arrivalRejectionService: ArrivalRejectionService,
  frontendAppConfig: FrontendAppConfig
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(arrivalId: ArrivalId): Action[AnyContent] = identify.async {
    implicit request =>
      if (frontendAppConfig.featureToggleArrivalRejection) {
        arrivalRejectionService.arrivalRejectionMessage(arrivalId).flatMap {
          case Some(rejectionMessage) =>
            val errorType = rejectionMessage.errors.head.errorType.code

            if (errorType == 90 || errorType == 91 || errorType == 93) {

              val errorKey = errorType match {
                case 90 => "movementReferenceNumberRejection.error.unknown"
                case 91 => "movementReferenceNumberRejection.error.duplicate"
                case 93 => "movementReferenceNumberRejection.error.invalid"
              }

              val json = Json.obj(
                "mrn"                        -> rejectionMessage.movementReferenceNumber,
                "errorKey"                   -> errorKey,
                "contactUrl"                 -> appConfig.nctsEnquiriesUrl,
                "movementReferenceNumberUrl" -> routes.MovementReferenceNumberController.onPageLoad().url
              )

              renderer.render("movementReferenceNumberRejection.njk", json).map(Ok(_))

            } else {
              val json = Json.obj(
                "mrn"              -> rejectionMessage.movementReferenceNumber,
                "errors"           -> rejectionMessage.errors,
                "contactUrl"       -> appConfig.nctsEnquiriesUrl,
                "createArrivalUrl" -> routes.MovementReferenceNumberController.onPageLoad().url
              )

              renderer.render("arrivalGeneralRejection.njk", json).map(Ok(_))

            }

          case _ => Future.successful(Redirect(routes.TechnicalDifficultiesController.onPageLoad()))
        }
      } else {
        Future.successful(Redirect(routes.UnauthorisedController.onPageLoad()))
      }
  }
}
