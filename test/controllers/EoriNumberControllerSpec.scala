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

import base.SpecBase
import forms.EoriNumberFormProvider
import matchers.JsonMatchers
import models.NormalMode
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.{ConsigneeEoriConfirmationPage, ConsigneeEoriNumberPage, ConsigneeNamePage}
import play.api.inject.bind
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import repositories.SessionRepository
import uk.gov.hmrc.viewmodels.NunjucksSupport

import scala.concurrent.Future

class EoriNumberControllerSpec extends SpecBase with MockitoSugar with NunjucksSupport with JsonMatchers {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new EoriNumberFormProvider()
  val form         = formProvider(traderName)

  lazy val eoriNumberRoute = routes.ConsigneeEoriNumberController.onPageLoad(ref, NormalMode).url

  "EoriNumber Controller" - {

    "must return OK and the correct view for a GET" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))
      val userAnswers = emptyUserAnswers
        .set(ConsigneeEoriConfirmationPage, true)
        .success
        .value
        .set(ConsigneeNamePage, "Fred")
        .success
        .value

      val application    = applicationBuilder(userAnswers = Some(userAnswers)).build()
      val request        = FakeRequest(GET, eoriNumberRoute)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(application, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "form" -> form,
        "ref"  -> ref,
        "mode" -> NormalMode
      )

      templateCaptor.getValue mustEqual "eoriNumber.njk"
      jsonCaptor.getValue must containJson(expectedJson)

      application.stop()
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))
      val userAnswers = emptyUserAnswers
        .set(ConsigneeEoriConfirmationPage, false)
        .success
        .value
        .set(ConsigneeNamePage, traderName)
        .success
        .value
        .set(ConsigneeEoriNumberPage, eoriNumber.value)
        .success
        .value

      val application    = applicationBuilder(userAnswers = Some(userAnswers)).build()
      val request        = FakeRequest(GET, eoriNumberRoute)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(application, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val filledForm = formProvider(traderName).bind(Map("value" -> eoriNumber.value))

      val expectedJson = Json.obj(
        "form"       -> filledForm,
        "ref"        -> ref,
        "mode"       -> NormalMode,
        "eoriNumber" -> eoriNumber
      )

      templateCaptor.getValue mustEqual "eoriNumber.njk"

      jsonCaptor.getValue must containJson(expectedJson)

      application.stop()
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      val userAnswers = emptyUserAnswers
        .set(ConsigneeEoriConfirmationPage, false)
        .success
        .value
        .set(ConsigneeNamePage, traderName)
        .success
        .value
      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      val request =
        FakeRequest(POST, eoriNumberRoute)
          .withFormUrlEncodedBody(("value", eoriNumber.value))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual onwardRoute.url

      application.stop()
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))
      val userAnswers = emptyUserAnswers
        .set(ConsigneeEoriConfirmationPage, false)
        .success
        .value
        .set(ConsigneeNamePage, traderName)
        .success
        .value

      val application    = applicationBuilder(userAnswers = Some(userAnswers)).build()
      val request        = FakeRequest(POST, eoriNumberRoute).withFormUrlEncodedBody(("value", ""))
      val boundForm      = form.bind(Map("value" -> ""))
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "form" -> boundForm,
        "ref"  -> ref,
        "mode" -> NormalMode
      )

      templateCaptor.getValue mustEqual "eoriNumber.njk"
      jsonCaptor.getValue must containJson(expectedJson)

      application.stop()
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, eoriNumberRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, eoriNumberRoute)
          .withFormUrlEncodedBody(("value", "answer"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }
  }
}
