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
import controllers.events.transhipments.{routes => transhipmentRoutes}
import forms.events.transhipments.TranshipmentTypeFormProvider
import matchers.JsonMatchers
import models.{NormalMode, TranshipmentType}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import pages.events.transhipments.TranshipmentTypePage
import play.api.data.Form
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import uk.gov.hmrc.viewmodels.NunjucksSupport

import scala.concurrent.Future

class TranshipmentTypeControllerSpec extends SpecBase with AppWithDefaultMockFixtures with NunjucksSupport with JsonMatchers {

  lazy val transhipmentTypeRoute: String = transhipmentRoutes.TranshipmentTypeController.onPageLoad(mrn, eventIndex, NormalMode).url

  val formProvider                 = new TranshipmentTypeFormProvider()
  val form: Form[TranshipmentType] = formProvider()

  private val transhipmentTypeTemplate = "events/transhipments/transhipmentType.njk"

  "TranshipmentType Controller" - {

    "must return OK and the correct view for a GET" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      setExistingUserAnswers(emptyUserAnswers)

      val request        = FakeRequest(GET, transhipmentTypeRoute)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "form"        -> form,
        "mode"        -> NormalMode,
        "mrn"         -> mrn,
        "radios"      -> TranshipmentType.radios(form),
        "onSubmitUrl" -> routes.TranshipmentTypeController.onSubmit(mrn, eventIndex, NormalMode).url
      )

      templateCaptor.getValue mustEqual transhipmentTypeTemplate
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val userAnswers = emptyUserAnswers.set(TranshipmentTypePage(eventIndex), TranshipmentType.values.head).success.value
      setExistingUserAnswers(userAnswers)

      val request        = FakeRequest(GET, transhipmentTypeRoute)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val filledForm = form.bind(Map("value" -> TranshipmentType.values.head.toString))

      val expectedJson = Json.obj(
        "form"        -> filledForm,
        "mode"        -> NormalMode,
        "mrn"         -> mrn,
        "radios"      -> TranshipmentType.radios(filledForm),
        "onSubmitUrl" -> routes.TranshipmentTypeController.onSubmit(mrn, eventIndex, NormalMode).url
      )

      templateCaptor.getValue mustEqual transhipmentTypeTemplate
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      setExistingUserAnswers(emptyUserAnswers)

      val request =
        FakeRequest(POST, transhipmentTypeRoute)
          .withFormUrlEncodedBody(("value", TranshipmentType.values.head.toString))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      setExistingUserAnswers(emptyUserAnswers)

      val request        = FakeRequest(POST, transhipmentTypeRoute).withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm      = form.bind(Map("value" -> "invalid value"))
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "form"        -> boundForm,
        "mode"        -> NormalMode,
        "mrn"         -> mrn,
        "radios"      -> TranshipmentType.radios(boundForm),
        "onSubmitUrl" -> routes.TranshipmentTypeController.onSubmit(mrn, eventIndex, NormalMode).url
      )

      templateCaptor.getValue mustEqual transhipmentTypeTemplate
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, transhipmentTypeRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request =
        FakeRequest(POST, transhipmentTypeRoute)
          .withFormUrlEncodedBody(("value", TranshipmentType.values.head.toString))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }
  }
}
