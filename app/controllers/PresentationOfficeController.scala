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

import connectors.ReferenceDataConnector
import controllers.actions._
import forms.PresentationOfficeFormProvider
import javax.inject.Inject
import models.GoodsLocation.BorderForceOffice
import models.reference.CustomsOffice
import models.{Mode, MovementReferenceNumber}
import navigation.Navigator
import pages.{ConsigneeNamePage, CustomsSubPlacePage, PresentationOfficePage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc._
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.NunjucksSupport

import scala.concurrent.{ExecutionContext, Future}

class PresentationOfficeController @Inject()(override val messagesApi: MessagesApi,
                                             sessionRepository: SessionRepository,
                                             navigator: Navigator,
                                             identify: IdentifierAction,
                                             getData: DataRetrievalActionProvider,
                                             requireData: DataRequiredAction,
                                             formProvider: PresentationOfficeFormProvider,
                                             referenceDataConnector: ReferenceDataConnector,
                                             val controllerComponents: MessagesControllerComponents,
                                             renderer: Renderer)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport {

  def onPageLoad(mrn: MovementReferenceNumber, mode: Mode): Action[AnyContent] =
    (identify andThen getData(mrn) andThen requireData).async {
      implicit request =>
        val locationName = (request.userAnswers.get(CustomsSubPlacePage), request.userAnswers.get(ConsigneeNamePage)) match {
          case (Some(customsSubPlace), None) => customsSubPlace
          case (None, Some(consigneeName))   => consigneeName
          case _                             => None
        }
        referenceDataConnector.getCustomsOffices flatMap {
          customsOffices =>
            locationName match {
              case locationName: String =>
                val form = formProvider(locationName, customsOffices)
                val preparedForm = request.userAnswers.get(PresentationOfficePage) match {
                  case None        => form
                  case Some(value) => form.fill(value)
                }
                renderView(mrn, mode, locationName, preparedForm, customsOffices, Results.Ok)
              case _ =>
                Future.successful(Redirect(routes.SessionExpiredController.onPageLoad()))
            }
        }
    }

  private def renderView(mrn: MovementReferenceNumber,
                         mode: Mode,
                         presentationOffice: String,
                         form: Form[CustomsOffice],
                         customsOffices: Seq[CustomsOffice],
                         status: Results.Status)(implicit request: Request[AnyContent]): Future[Result] = {

    val json = Json.obj(
      "form"           -> form,
      "mrn"            -> mrn,
      "mode"           -> mode,
      "customsOffices" -> getCustomsOfficesAsJson(form.value, customsOffices),
      "header"         -> msg"presentationOffice.title".withArgs(presentationOffice)
    )
    renderer.render("presentationOffice.njk", json).map(status(_))
  }

  def onSubmit(ref: ArrivalUniqueRef, mode: Mode): Action[AnyContent] =
    (identify andThen getData(mrn) andThen requireData).async {
      implicit request =>
        val locationName = (request.userAnswers.get(CustomsSubPlacePage), request.userAnswers.get(ConsigneeNamePage)) match {
          case (Some(customsSubPlace), None) => customsSubPlace
          case (None, Some(consigneeName))   => consigneeName
          case _                             => None
        }

        referenceDataConnector.getCustomsOffices flatMap {
          customsOffices =>
            locationName match {
              case locationName: String =>
                val form = formProvider(locationName, customsOffices)
                form
                  .bindFromRequest()
                  .fold(
                    formWithErrors => {
                      renderView(mrn, mode, locationName, formWithErrors, customsOffices, Results.BadRequest)
                    },
                    value =>
                      for {
                        updatedAnswers <- Future.fromTry(request.userAnswers.set(PresentationOfficePage, value))
                        _              <- sessionRepository.set(updatedAnswers)
                      } yield Redirect(navigator.nextPage(PresentationOfficePage, mode, updatedAnswers))
                  )
              case _ => Future.successful(Redirect(routes.SessionExpiredController.onPageLoad()))

            }
        }
    }

  private def getCustomsOfficesAsJson(value: Option[CustomsOffice], customsOffices: Seq[CustomsOffice]): Seq[JsObject] = {
    val customsOfficeObjects = customsOffices.map {
      office =>
        Json.obj(
          "value"    -> office.id,
          "text"     -> s"${office.name} (${office.id})",
          "selected" -> value.contains(office)
        )
    }
    Json.obj("value" -> "", "text" -> "") +: customsOfficeObjects
  }

}
