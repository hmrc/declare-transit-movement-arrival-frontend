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

package controllers.events

import base.SpecBase
import connectors.ReferenceDataConnector
import forms.events.EventCountryFormProvider
import matchers.JsonMatchers
import models.NormalMode
import models.UserAnswers
import models.reference.Country
import navigation.FakeNavigator
import navigation.Navigator
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.any
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.events.EventCountryPage
import play.api.data.Form
import play.api.inject.bind
import play.api.libs.json.JsObject
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import repositories.SessionRepository
import uk.gov.hmrc.viewmodels.NunjucksSupport

import scala.concurrent.Future

class EventCountryControllerSpec extends SpecBase with MockitoSugar with NunjucksSupport with JsonMatchers {

  def onwardRoute: Call = Call("GET", "/foo")

  val formProvider                                       = new EventCountryFormProvider()
  private val country: Country                           = Country("valid", "GB", "United Kingdom")
  val countries                                          = Seq(country)
  val form: Form[Country]                                = formProvider(countries)
  val mockReferenceDataConnector: ReferenceDataConnector = mock[ReferenceDataConnector]
  lazy val eventCountryRoute: String                     = routes.EventCountryController.onPageLoad(mrn, eventIndex, NormalMode).url

  "EventCountry Controller" - {

    "must return OK and the correct view for a GET" in {
      verifyOnPageLoad(Some(emptyUserAnswers), form, false)
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswers = UserAnswers(mrn).set(EventCountryPage(eventIndex), country).success.value
      val filledForm  = form.bind(Map("value" -> "GB"))

      verifyOnPageLoad(Some(userAnswers), filledForm, true)
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockReferenceDataConnector.getCountryList()(any(), any())).thenReturn(Future.successful(countries))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[ReferenceDataConnector].toInstance(mockReferenceDataConnector)
          )
          .build()

      val request =
        FakeRequest(POST, eventCountryRoute)
          .withFormUrlEncodedBody(("value", "GB"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual onwardRoute.url

      application.stop()
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))
      when(mockReferenceDataConnector.getCountryList()(any(), any())).thenReturn(Future.successful(countries))

      val json = Seq(
        Json.obj("text" -> "", "value"               -> ""),
        Json.obj("text" -> "United Kingdom", "value" -> "GB", "selected" -> false)
      )

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides {
          bind[ReferenceDataConnector].toInstance(mockReferenceDataConnector)
        }
        .build()
      val request                                = FakeRequest(POST, eventCountryRoute).withFormUrlEncodedBody(("value", ""))
      val boundForm                              = form.bind(Map("value" -> ""))
      val templateCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor: ArgumentCaptor[JsObject]   = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(application, request).value

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

      application.stop()
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, eventCountryRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, eventCountryRoute)
          .withFormUrlEncodedBody(("value", "GB"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }
  }

  private def verifyOnPageLoad(userAnswers: Option[UserAnswers], form1: Form[Country], preSelected: Boolean): Future[_] = {
    when(mockRenderer.render(any(), any())(any()))
      .thenReturn(Future.successful(Html("")))

    when(mockReferenceDataConnector.getCountryList()(any(), any())).thenReturn(Future.successful(countries))

    val countriesJson = Seq(
      Json.obj("text" -> "", "value"               -> ""),
      Json.obj("text" -> "United Kingdom", "value" -> "GB", "selected" -> preSelected)
    )

    val application = applicationBuilder(userAnswers = userAnswers)
      .overrides {
        bind[ReferenceDataConnector].toInstance(mockReferenceDataConnector)
      }
      .build()
    val templateCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
    val jsonCaptor: ArgumentCaptor[JsObject]   = ArgumentCaptor.forClass(classOf[JsObject])

    val request = FakeRequest(GET, eventCountryRoute)

    val result = route(application, request).value

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

    application.stop()
  }
}
