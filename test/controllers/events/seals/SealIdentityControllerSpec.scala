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

package controllers.events.seals

import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.events.seals.SealIdentityFormProvider
import generators.MessagesModelGenerators
import matchers.JsonMatchers
import models.domain.SealDomain
import models.messages.Seal
import models.{Index, NormalMode}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import pages.events.seals.SealIdentityPage
import play.api.data.Form
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import uk.gov.hmrc.viewmodels.NunjucksSupport

import scala.concurrent.Future

class SealIdentityControllerSpec extends SpecBase with AppWithDefaultMockFixtures with NunjucksSupport with JsonMatchers with MessagesModelGenerators {

  val formProvider       = new SealIdentityFormProvider()
  val form: Form[String] = formProvider(sealIndex)

  private def sealIdentityRoute(index: Index = sealIndex): String = routes.SealIdentityController.onPageLoad(mrn, eventIndex, index, NormalMode).url

  "SealIdentity Controller" - {

    "must return OK and the correct view for a GET" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      setExistingUserAnswers(emptyUserAnswers)

      val request        = FakeRequest(GET, sealIdentityRoute())
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "form"        -> form,
        "mrn"         -> mrn,
        "mode"        -> NormalMode,
        "onSubmitUrl" -> routes.SealIdentityController.onSubmit(mrn, eventIndex, sealIndex, NormalMode).url
      )

      templateCaptor.getValue mustEqual "events/seals/sealIdentity.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val userAnswers = emptyUserAnswers.set(SealIdentityPage(eventIndex, sealIndex), sealDomain).success.value
      setExistingUserAnswers(userAnswers)

      val request        = FakeRequest(GET, sealIdentityRoute())
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val filledForm = form.bind(Map("value" -> seal.numberOrMark))

      val expectedJson = Json.obj(
        "form"        -> filledForm,
        "mrn"         -> mrn,
        "mode"        -> NormalMode,
        "onSubmitUrl" -> routes.SealIdentityController.onSubmit(mrn, eventIndex, sealIndex, NormalMode).url
      )

      templateCaptor.getValue mustEqual "events/seals/sealIdentity.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      setExistingUserAnswers(emptyUserAnswers)

      val request =
        FakeRequest(POST, sealIdentityRoute())
          .withFormUrlEncodedBody(("value", "answer"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must redirect to the next page when a value that is the same as the previous is submitted within the same index" in {

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val seal        = arbitrary[Seal].sample.value
      val userAnswers = emptyUserAnswers.set(SealIdentityPage(eventIndex, sealIndex), sealDomain).success.value

      setExistingUserAnswers(userAnswers)

      val request =
        FakeRequest(POST, sealIdentityRoute())
          .withFormUrlEncodedBody(("value", seal.numberOrMark))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      setExistingUserAnswers(emptyUserAnswers)

      val request        = FakeRequest(POST, sealIdentityRoute()).withFormUrlEncodedBody(("value", ""))
      val boundForm      = form.bind(Map("value" -> ""))
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "form"        -> boundForm,
        "mrn"         -> mrn,
        "mode"        -> NormalMode,
        "onSubmitUrl" -> routes.SealIdentityController.onSubmit(mrn, eventIndex, sealIndex, NormalMode).url
      )

      templateCaptor.getValue mustEqual "events/seals/sealIdentity.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "must return a Bad Request and errors when an existing seal is submitted and index is different to current index" in {
      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val seal        = arbitrary[SealDomain].sample.value
      val userAnswers = emptyUserAnswers.set(SealIdentityPage(eventIndex, sealIndex), seal).success.value

      val nextIndex = Index(1)
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(POST, sealIdentityRoute(nextIndex)).withFormUrlEncodedBody(("value", seal.numberOrMark))

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      templateCaptor.getValue mustEqual "events/seals/sealIdentity.njk"
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, sealIdentityRoute())

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request =
        FakeRequest(POST, sealIdentityRoute())
          .withFormUrlEncodedBody(("value", "answer"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }
  }
}
