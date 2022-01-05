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
import javax.inject.Inject
import models.ArrivalId
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import services.ArrivalRejectionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.ArrivalRejectionViewModel
import viewModels.sections.ViewModelConfig

import scala.concurrent.ExecutionContext

class ArrivalRejectionController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  val controllerComponents: MessagesControllerComponents,
  val renderer: Renderer,
  arrivalRejectionService: ArrivalRejectionService,
  val viewModelConfig: ViewModelConfig
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with TechnicalDifficultiesPage {

  def onPageLoad(arrivalId: ArrivalId): Action[AnyContent] = identify.async {
    implicit request =>
      arrivalRejectionService.arrivalRejectionMessage(arrivalId).flatMap {
        case Some(rejectionMessage) =>
          val viewModel = ArrivalRejectionViewModel(rejectionMessage, viewModelConfig.nctsEnquiriesUrl, arrivalId)
          renderer.render(viewModel.page, viewModel.viewData).map(Ok(_))
        case _ => renderTechnicalDifficultiesPage
      }
  }
}
