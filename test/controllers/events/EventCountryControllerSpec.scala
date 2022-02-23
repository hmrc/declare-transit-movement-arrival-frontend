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

package controllers.events

import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.events.EventCountryFormProvider
import matchers.JsonMatchers
import models.reference.{Country, CountryCode}
import models.{CountryList, NormalMode, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import pages.events.EventCountryPage
import play.api.data.Form
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import services.CountriesService
import uk.gov.hmrc.viewmodels.NunjucksSupport

import scala.concurrent.Future

class EventCountryControllerSpec extends SpecBase with AppWithDefaultMockFixtures with NunjucksSupport with JsonMatchers {

  val formProvider                   = new EventCountryFormProvider()
  private val country                = Country(CountryCode("GB"), "United Kingdom")
  val countries                      = CountryList(Vector(country))
  val form                           = formProvider(countries)
  private val mockCountriesService   = mock[CountriesService]
  lazy val eventCountryRoute: String = routes.EventCountryController.onPageLoad(mrn, eventIndex, NormalMode).url

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[CountriesService].toInstance(mockCountriesService))

  "EventCountry Controller" - {

    "must return OK and the correct view for a GET" in {
      verifyOnPageLoad(Some(emptyUserAnswers), form, false)
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswers = UserAnswers(mrn, eoriNumber).set(EventCountryPage(eventIndex), country.code).success.value
      val filledForm  = form.bind(Map("value" -> "GB"))

      verifyOnPageLoad(Some(userAnswers), filledForm, true)
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockCountriesService.getCountries(any())(any())).thenReturn(Future.successful(countries))

      setExistingUserAnswers(emptyUserAnswers)

      val request =
        FakeRequest(POST, eventCountryRoute)
          .withFormUrlEncodedBody(("value", "GB"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))
      when(mockCountriesService.getCountries(any())(any())).thenReturn(Future.successful(countries))

      val json = Seq(
        Json.obj("text" -> "Select a country", "value" -> ""),
        Json.obj("text" -> "United Kingdom", "value"   -> "GB", "selected" -> false)
      )

      setExistingUserAnswers(emptyUserAnswers)

      val request                                = FakeRequest(POST, eventCountryRoute).withFormUrlEncodedBody(("value", ""))
      val boundForm                              = form.bind(Map("value" -> ""))
      val templateCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor: ArgumentCaptor[JsObject]   = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "form"        -> boundForm,
        "mrn"         -> mrn,
        "mode"        -> NormalMode,
        "countries"   -> json,
        "onSubmitUrl" -> routes.EventCountryController.onSubmit(mrn, eventIndex, NormalMode).url
      )

      templateCaptor.getValue mustEqual "events/eventCountry.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, eventCountryRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request =
        FakeRequest(POST, eventCountryRoute)
          .withFormUrlEncodedBody(("value", "GB"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }
  }

  private def verifyOnPageLoad(userAnswers: Option[UserAnswers], form1: Form[Country], preSelected: Boolean) = {
    when(mockRenderer.render(any(), any())(any()))
      .thenReturn(Future.successful(Html("")))

    when(mockCountriesService.getCountries(any())(any())).thenReturn(Future.successful(countries))

    val countriesJson = Seq(
      Json.obj("text" -> "Select a country", "value" -> ""),
      Json.obj("text" -> "United Kingdom", "value"   -> "GB", "selected" -> preSelected)
    )

    userAnswers.map(setExistingUserAnswers).getOrElse(setNoExistingUserAnswers())

    val templateCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
    val jsonCaptor: ArgumentCaptor[JsObject]   = ArgumentCaptor.forClass(classOf[JsObject])

    val request = FakeRequest(GET, eventCountryRoute)

    val result = route(app, request).value

    status(result) mustEqual OK

    verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

    val expectedJson = Json.obj(
      "form"      -> form1,
      "mrn"       -> mrn,
      "mode"      -> NormalMode,
      "countries" -> countriesJson
    )

    templateCaptor.getValue mustEqual "events/eventCountry.njk"
    jsonCaptor.getValue must containJson(expectedJson)
  }
}
