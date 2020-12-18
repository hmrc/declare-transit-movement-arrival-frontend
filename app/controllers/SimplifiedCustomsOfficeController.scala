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
import forms.SimplifiedCustomsOfficeFormProvider
import javax.inject.Inject
import models.reference.CustomsOffice
import models.{Mode, MovementReferenceNumber}
import navigation.Navigator
import pages.{ConsigneeNamePage, CustomsSubPlacePage, SimplifiedCustomsOfficePage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc._
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.NunjucksSupport

import scala.concurrent.{ExecutionContext, Future}

class SimplifiedCustomsOfficeController @Inject()(
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalActionProvider,
  requireData: DataRequiredAction,
  formProvider: SimplifiedCustomsOfficeFormProvider,
  referenceDataConnector: ReferenceDataConnector,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport {

  def onPageLoad(mrn: MovementReferenceNumber, mode: Mode): Action[AnyContent] =
    (identify andThen getData(mrn) andThen requireData).async {
      implicit request =>
        val consigneeName = request.userAnswers.get(ConsigneeNamePage)

        val locationName = (request.userAnswers.get(CustomsSubPlacePage), consigneeName) match {
          case (Some(customsSubPlace), None) => customsSubPlace
          case (None, Some(cName))           => cName
          case _                             => None
        }

        referenceDataConnector.getCustomsOffices flatMap {
          customsOffices =>
            locationName match {
              case locationName: String =>
                val form = formProvider(consigneeName.getOrElse(""), customsOffices)
                val preparedForm = request.userAnswers.get(SimplifiedCustomsOfficePage) match {
                  case None        => form
                  case Some(value) => form.fill(value)
                }
                renderView(
                  mrn            = mrn,
                  mode           = mode,
                  consigneeName  = consigneeName.getOrElse(""),
                  customsOffice  = locationName,
                  form           = preparedForm,
                  customsOffices = customsOffices,
                  status         = Results.Ok
                )
            }
        }
    }

  private def renderView(
    mrn: MovementReferenceNumber,
    mode: Mode,
    consigneeName: String,
    customsOffice: String,
    form: Form[CustomsOffice],
    customsOffices: Seq[CustomsOffice],
    status: Results.Status
  )(implicit request: Request[AnyContent]): Future[Result] = {

    val json = Json.obj(
      "form"           -> form,
      "mrn"            -> mrn,
      "mode"           -> mode,
      "customsOffices" -> getCustomsOfficesAsJson(form.value, customsOffices),
      "header"         -> msg"customsOffice.simplified.heading".withArgs(customsOffice),
      "consigneeName"  -> consigneeName,
      "locationName"   -> customsOffice
    )
    renderer.render("customsOfficeSimplified.njk", json).map(status(_))
  }

  def onSubmit(mrn: MovementReferenceNumber, mode: Mode): Action[AnyContent] =
    (identify andThen getData(mrn) andThen requireData).async {
      implicit request =>
        val consigneeName = request.userAnswers.get(ConsigneeNamePage)
        val locationName = (request.userAnswers.get(CustomsSubPlacePage), consigneeName) match {
          case (Some(customsSubPlace), None) => customsSubPlace
          case (None, Some(cName))           => cName
          case _                             => None
        }

        referenceDataConnector.getCustomsOffices flatMap {
          customsOffices =>
            locationName match {
              case locationName: String =>
                val form = formProvider(consigneeName.getOrElse(""), customsOffices)
                form
                  .bindFromRequest()
                  .fold(
                    formWithErrors => {
                      renderView(
                        mrn            = mrn,
                        mode           = mode,
                        consigneeName  = consigneeName.getOrElse(""),
                        customsOffice  = locationName,
                        form           = formWithErrors,
                        customsOffices = customsOffices,
                        status         = Results.BadRequest
                      )
                    },
                    value =>
                      for {
                        updatedAnswers <- Future.fromTry(request.userAnswers.set(SimplifiedCustomsOfficePage, value))
                        _              <- sessionRepository.set(updatedAnswers)
                      } yield Redirect(navigator.nextPage(SimplifiedCustomsOfficePage, mode, updatedAnswers))
                  )
              case _ => Future.successful(Redirect(routes.SessionExpiredController.onPageLoad()))

            }
        }
    }

  private def getCustomsOfficesAsJson(value: Option[CustomsOffice], customsOffices: Seq[CustomsOffice]): Seq[JsObject] = {
    val customsOfficeObjects = customsOffices.map {
      office =>
        val officeName = office.name match {
          case Some(name) => s"$name (${office.id})"
          case _          => s"${office.id}"
        }
        Json.obj(
          "value"    -> office.id,
          "text"     -> s"$officeName",
          "selected" -> value.contains(office)
        )
    }

    Json.obj("value" -> "", "text" -> "") +: customsOfficeObjects
  }

}
