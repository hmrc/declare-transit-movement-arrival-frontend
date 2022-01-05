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
import forms.ConsigneeAddressFormProvider
import javax.inject.Inject
import models.{Mode, MovementReferenceNumber}
import navigation.Navigator
import pages.{ConsigneeAddressPage, ConsigneeNamePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.NunjucksSupport

import scala.concurrent.{ExecutionContext, Future}

class ConsigneeAddressController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalActionProvider,
  requireData: DataRequiredAction,
  formProvider: ConsigneeAddressFormProvider,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport {

  def onPageLoad(mrn: MovementReferenceNumber, mode: Mode): Action[AnyContent] = (identify andThen getData(mrn) andThen requireData).async {
    implicit request =>
      request.userAnswers.get(ConsigneeNamePage) match {
        case Some(consigneeName) =>
          val preparedForm = request.userAnswers.get(ConsigneeAddressPage) match {
            case Some(value) => formProvider(consigneeName).fill(value)
            case None        => formProvider(consigneeName)
          }

          val json = Json.obj(
            "form"          -> preparedForm,
            "mrn"           -> mrn,
            "mode"          -> mode,
            "consigneeName" -> consigneeName
          )

          renderer.render("consigneeAddress.njk", json).map(Ok(_))
        case _ => Future.successful(Redirect(routes.SessionExpiredController.onPageLoad()))
      }
  }

  def onSubmit(mrn: MovementReferenceNumber, mode: Mode): Action[AnyContent] = (identify andThen getData(mrn) andThen requireData).async {
    implicit request =>
      request.userAnswers.get(ConsigneeNamePage) match {
        case Some(consigneeName) =>
          formProvider(consigneeName)
            .bindFromRequest()
            .fold(
              formWithErrors => {

                val json = Json.obj(
                  "form"          -> formWithErrors,
                  "mrn"           -> mrn,
                  "mode"          -> mode,
                  "consigneeName" -> consigneeName
                )

                renderer.render("consigneeAddress.njk", json).map(BadRequest(_))
              },
              value =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(ConsigneeAddressPage, value))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(ConsigneeAddressPage, mode, updatedAnswers))
            )
        case _ => Future.successful(Redirect(routes.SessionExpiredController.onPageLoad()))
      }
  }
}
