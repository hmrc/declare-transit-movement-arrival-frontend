/*
 * Copyright 2019 HM Revenue & Customs
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
import forms.PresentationOfficeFormProvider
import javax.inject.Inject
import models.{Mode, MovementReferenceNumber}
import navigation.Navigator
import pages.{CustomsSubPlacePage, PresentationOfficePage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc._
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.NunjucksSupport

import scala.concurrent.{ExecutionContext, Future}

class PresentationOfficeController @Inject()(
                                              override val messagesApi: MessagesApi,
                                              sessionRepository: SessionRepository,
                                              navigator: Navigator,
                                              identify: IdentifierAction,
                                              getData: DataRetrievalActionProvider,
                                              requireData: DataRequiredAction,
                                              formProvider: PresentationOfficeFormProvider,
                                              val controllerComponents: MessagesControllerComponents,
                                              renderer: Renderer
                                            )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with NunjucksSupport {


  def onPageLoad(mrn: MovementReferenceNumber, mode: Mode): Action[AnyContent] = (identify andThen getData(mrn) andThen requireData).async {
    implicit request =>

      request.userAnswers.get(CustomsSubPlacePage) match {
        case Some(subsPlace) =>
          val form = formProvider(subsPlace)
          val preparedForm = request.userAnswers.get(PresentationOfficePage) match {
            case None => form
            case Some(value) => form.fill(value)
          }
          renderView(mrn, mode, subsPlace, preparedForm, Results.Ok)

        case _ => Future.successful(Redirect(routes.SessionExpiredController.onPageLoad()))
      }
  }

  private def renderView(mrn: MovementReferenceNumber,
                       mode: Mode,
                       subsPlace: String,
                       form: Form[String],
                       status: Results.Status)(implicit request: Request[AnyContent]): Future[Result] = {
    val json = Json.obj(
      "form" -> form,
      "mrn" -> mrn,
      "mode" -> mode,
      "header" -> Messages("presentationOffice.title", subsPlace)
    )
    renderer.render("presentationOffice.njk", json).map(status(_))
  }

  def onSubmit(mrn: MovementReferenceNumber, mode: Mode): Action[AnyContent] = (identify andThen getData(mrn) andThen requireData).async {
    implicit request =>

      request.userAnswers.get(CustomsSubPlacePage) match {
        case Some(subsPlace) =>
          val form = formProvider(subsPlace)
          form.bindFromRequest().fold(
            formWithErrors => {
              renderView(mrn, mode, subsPlace, formWithErrors, Results.BadRequest)
            },
            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(PresentationOfficePage, value))
                _ <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(PresentationOfficePage, mode, updatedAnswers))
          )
        case _ => Future.successful(Redirect(routes.SessionExpiredController.onPageLoad()))
      }
  }
}
