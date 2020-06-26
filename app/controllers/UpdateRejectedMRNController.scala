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

import controllers.actions._
import forms.UpdateRejectedMRNFormProvider
import javax.inject.Inject
import models.{ArrivalId, MovementReferenceNumber}
import navigation.Navigator
import pages.UpdateRejectedMRNPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import repositories.SessionRepository
import services.{ArrivalNotificationMessageService, UserAnswersService}
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.NunjucksSupport

import scala.concurrent.{ExecutionContext, Future}

class UpdateRejectedMRNController @Inject()(override val messagesApi: MessagesApi,
                                            navigator: Navigator,
                                            identify: IdentifierAction,
                                            formProvider: UpdateRejectedMRNFormProvider,
                                            sessionRepository: SessionRepository,
                                            arrivalMovementMessageService: ArrivalNotificationMessageService,
                                            userAnswersService: UserAnswersService,
                                            val controllerComponents: MessagesControllerComponents,
                                            renderer: Renderer)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport {

  private val form = formProvider()

  def onPageLoad(arrivalId: ArrivalId): Action[AnyContent] = identify.async {
    implicit request =>
      arrivalMovementMessageService.getArrivalNotificationMessage(arrivalId) flatMap {
        case Some(arrivalMovementRequest) =>
          MovementReferenceNumber(arrivalMovementRequest.header.movementReferenceNumber) match {
            case Some(mrn) =>
              val json = Json.obj("form" -> form.fill(mrn), "arrivalId" -> arrivalId.value)
              renderer.render("updateMovementReferenceNumber.njk", json).map(Ok(_))
            case _ => Future.successful(Redirect(routes.TechnicalDifficultiesController.onPageLoad()))
          }
        case _ => Future.successful(Redirect(routes.TechnicalDifficultiesController.onPageLoad()))
      }
  }

  def onSubmit(arrivalId: ArrivalId): Action[AnyContent] = identify.async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => {

            val json = Json.obj("form" -> formWithErrors)

            renderer.render("updateMovementReferenceNumber.njk", json).map(BadRequest(_))
          },
          value =>
            userAnswersService.getUserAnswers(arrivalId) map {
              case Some(userAnswers) =>
                sessionRepository.set(userAnswers.copy(id = value))
                Redirect(navigator.nextRejectionPage(UpdateRejectedMRNPage, value, arrivalId))
              case _ => Redirect(routes.TechnicalDifficultiesController.onPageLoad())
          }
        )
  }
}
