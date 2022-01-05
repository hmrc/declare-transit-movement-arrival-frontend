/*
 * Copyright 2022 HM Revenue & Customs
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
import forms.MovementReferenceNumberFormProvider
import models.NormalMode
import navigation.Navigator
import pages.MovementReferenceNumberPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import repositories.SessionRepository
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.NunjucksSupport

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class MovementReferenceNumberController @Inject() (override val messagesApi: MessagesApi,
                                                   sessionRepository: SessionRepository,
                                                   navigator: Navigator,
                                                   identify: IdentifierAction,
                                                   formProvider: MovementReferenceNumberFormProvider,
                                                   userAnswersService: UserAnswersService,
                                                   val controllerComponents: MessagesControllerComponents,
                                                   renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport {

  private val form = formProvider()

  def onPageLoad(): Action[AnyContent] = identify.async {
    implicit request =>
      val json = Json.obj("form" -> form)

      renderer.render("movementReferenceNumber.njk", json).map(Ok(_))
  }

  def onSubmit(): Action[AnyContent] = identify.async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => {
            val json = Json.obj("form" -> formWithErrors)
            renderer.render("movementReferenceNumber.njk", json).map(BadRequest(_))
          },
          value =>
            for {
              userAnswers <- userAnswersService.getOrCreateUserAnswers(request.eoriNumber, value)
              _           <- sessionRepository.set(userAnswers)
            } yield Redirect(navigator.nextPage(MovementReferenceNumberPage, NormalMode, userAnswers))
        )
  }

}
