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
import forms.GoodsLocationFormProvider
import javax.inject.Inject
import models.GoodsLocation.{AuthorisedConsigneesLocation, BorderForceOffice}
import models.{GoodsLocation, Mode, MovementReferenceNumber}
import pages.GoodsLocationPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.NunjucksSupport

import scala.concurrent.{ExecutionContext, Future}

class GoodsLocationController @Inject() (override val messagesApi: MessagesApi,
                                         sessionRepository: SessionRepository,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalActionProvider,
                                         requireData: DataRequiredAction,
                                         formProvider: GoodsLocationFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport {

  private val form = formProvider()

  def onPageLoad(mrn: MovementReferenceNumber, mode: Mode): Action[AnyContent] = (identify andThen getData(mrn) andThen requireData).async {
    implicit request =>
      val preparedForm = request.userAnswers.get(GoodsLocationPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      val json = Json.obj(
        "form"   -> preparedForm,
        "mode"   -> mode,
        "mrn"    -> mrn,
        "radios" -> GoodsLocation.radios(preparedForm)
      )

      renderer.render("goodsLocation.njk", json).map(Ok(_))
  }

  def onSubmit(mrn: MovementReferenceNumber, mode: Mode): Action[AnyContent] = (identify andThen getData(mrn) andThen requireData).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => {

            val json = Json.obj(
              "form"   -> formWithErrors,
              "mode"   -> mode,
              "mrn"    -> mrn,
              "radios" -> GoodsLocation.radios(formWithErrors)
            )

            renderer.render("goodsLocation.njk", json).map(BadRequest(_))
          },
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(GoodsLocationPage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield value match {
              case BorderForceOffice            => Redirect(routes.CustomsSubPlaceController.onPageLoad(updatedAnswers.movementReferenceNumber, mode))
              case AuthorisedConsigneesLocation => Redirect(routes.AuthorisedLocationController.onPageLoad(updatedAnswers.movementReferenceNumber, mode))
            }
        )
  }
}
