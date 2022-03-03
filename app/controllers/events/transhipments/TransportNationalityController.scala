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

package controllers.events.transhipments

import controllers.actions.{DataRequiredAction, DataRetrievalActionProvider, IdentifierAction}
import forms.events.transhipments.TransportNationalityFormProvider
import models.reference.Country
import models.{Index, Mode, MovementReferenceNumber}
import navigation.Navigator
import pages.events.transhipments.TransportNationalityPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc._
import renderer.Renderer
import repositories.SessionRepository
import services.CountriesService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.NunjucksSupport

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TransportNationalityController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalActionProvider,
  requireData: DataRequiredAction,
  formProvider: TransportNationalityFormProvider,
  countriesService: CountriesService,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport {

  def onPageLoad(mrn: MovementReferenceNumber, eventIndex: Index, mode: Mode): Action[AnyContent] = (identify andThen getData(mrn) andThen requireData).async {
    implicit request =>
      countriesService.getCountries() flatMap {
        countryList =>
          val form = formProvider(countryList)

          val preparedForm = request.userAnswers
            .get(TransportNationalityPage(eventIndex))
            .flatMap(countryList.getCountry)
            .map(form.fill)
            .getOrElse(form)

          renderPage(mrn, mode, preparedForm, countryList.countries, Ok, eventIndex)
      }
  }

  def onSubmit(mrn: MovementReferenceNumber, eventIndex: Index, mode: Mode): Action[AnyContent] = (identify andThen getData(mrn) andThen requireData).async {
    implicit request =>
      countriesService.getCountries() flatMap {
        countryList =>
          val form = formProvider(countryList)

          form
            .bindFromRequest()
            .fold(
              formWithErrors => renderPage(mrn, mode, formWithErrors, countryList.countries, BadRequest, eventIndex),
              value =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(TransportNationalityPage(eventIndex), value.code))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(TransportNationalityPage(eventIndex), mode, updatedAnswers))
            )
      }
  }

  private def renderPage(mrn: MovementReferenceNumber, mode: Mode, form: Form[Country], countries: Seq[Country], status: Status, eventIndex: Index)(implicit
    request: Request[AnyContent]
  ): Future[Result] = {
    val json = Json.obj(
      "form"        -> form,
      "mrn"         -> mrn,
      "mode"        -> mode,
      "countries"   -> countryJsonList(form.value, countries),
      "onSubmitUrl" -> routes.TransportNationalityController.onSubmit(mrn, eventIndex, mode).url
    )

    renderer.render("events/transhipments/transportNationality.njk", json).map(status(_))
  }

  private def countryJsonList(value: Option[Country], countries: Seq[Country]): Seq[JsObject] = {
    val countryJsonList = countries.map {
      country =>
        Json.obj("text" -> country.toString, "value" -> country.code, "selected" -> value.contains(country))
    }

    Json.obj("value" -> "", "text" -> "Select a country") +: countryJsonList
  }
}
