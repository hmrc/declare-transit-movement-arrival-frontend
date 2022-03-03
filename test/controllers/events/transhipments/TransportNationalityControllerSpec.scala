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

import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.events.transhipments.TransportNationalityFormProvider
import matchers.JsonMatchers
import models.reference.{Country, CountryCode}
import models.{CountryList, NormalMode, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import pages.events.transhipments.TransportNationalityPage
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

class TransportNationalityControllerSpec extends SpecBase with AppWithDefaultMockFixtures with NunjucksSupport with JsonMatchers {

  val formProvider    = new TransportNationalityFormProvider()
  private val country = Country(CountryCode("GB"), "United Kingdom")
  val countries       = CountryList(Seq(country))
  val form            = formProvider(countries)

  private val mockCountriesService           = mock[CountriesService]
  lazy val transportNationalityRoute: String = routes.TransportNationalityController.onPageLoad(mrn, eventIndex, NormalMode).url
  private val transportNationalityTemplate   = "events/transhipments/transportNationality.njk"

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[CountriesService].toInstance(mockCountriesService))

  "TransportNationality Controller" - {

    "must return OK and the correct view for a GET" in {

      verifyOnPageLoad(form, userAnswers = emptyUserAnswers, preSelect = false)
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val filledForm  = form.bind(Map("value" -> "GB"))
      val userAnswers = UserAnswers(mrn, eoriNumber).set(TransportNationalityPage(eventIndex), country.code).success.value

      verifyOnPageLoad(filledForm, userAnswers, preSelect = true)
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockCountriesService.getCountries()(any())).thenReturn(Future.successful(countries))
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      setExistingUserAnswers(emptyUserAnswers)

      val request =
        FakeRequest(POST, transportNationalityRoute)
          .withFormUrlEncodedBody(("value", "GB"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      when(mockCountriesService.getCountries()(any())).thenReturn(Future.successful(countries))
      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      setExistingUserAnswers(emptyUserAnswers)

      val request                                = FakeRequest(POST, transportNationalityRoute).withFormUrlEncodedBody(("value", ""))
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
        "countries"   -> countriesJson(),
        "onSubmitUrl" -> routes.TransportNationalityController.onSubmit(mrn, eventIndex, NormalMode).url
      )

      templateCaptor.getValue mustEqual transportNationalityTemplate
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, transportNationalityRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request =
        FakeRequest(POST, transportNationalityRoute)
          .withFormUrlEncodedBody(("value", "answer"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }
  }

  private def verifyOnPageLoad(form: Form[Country], userAnswers: UserAnswers, preSelect: Boolean) = {

    when(mockCountriesService.getCountries()(any())).thenReturn(Future.successful(countries))
    when(mockRenderer.render(any(), any())(any()))
      .thenReturn(Future.successful(Html("")))

    setExistingUserAnswers(userAnswers)

    val templateCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
    val jsonCaptor: ArgumentCaptor[JsObject]   = ArgumentCaptor.forClass(classOf[JsObject])

    val request = FakeRequest(GET, transportNationalityRoute)

    val result = route(app, request).value

    status(result) mustEqual OK

    verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

    val expectedJson = Json.obj(
      "form"      -> form,
      "mrn"       -> mrn,
      "mode"      -> NormalMode,
      "countries" -> countriesJson(preSelect)
    )

    templateCaptor.getValue mustEqual transportNationalityTemplate
    jsonCaptor.getValue must containJson(expectedJson)
  }

  private def countriesJson(preSelect: Boolean = false): Seq[JsObject] =
    Seq(
      Json.obj("text" -> "Select a country", "value" -> ""),
      Json.obj("text" -> "United Kingdom", "value"   -> "GB", "selected" -> preSelect)
    )
}
