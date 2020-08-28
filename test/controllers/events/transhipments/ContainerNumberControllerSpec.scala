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

package controllers.events.transhipments

import base.SpecBase
import forms.events.transhipments.ContainerNumberFormProvider
import generators.MessagesModelGenerators
import matchers.JsonMatchers
import models.domain.ContainerDomain
import models.messages.Container
import models.{Index, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.mockito.MockitoSugar
import pages.events.transhipments.ContainerNumberPage
import play.api.data.Form
import play.api.inject.bind
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import repositories.SessionRepository
import uk.gov.hmrc.viewmodels.NunjucksSupport

import scala.concurrent.Future

class ContainerNumberControllerSpec extends SpecBase with MockitoSugar with NunjucksSupport with JsonMatchers with MessagesModelGenerators {

  def onwardRoute = Call("GET", "/foo")

  private val formProvider       = new ContainerNumberFormProvider()
  private val form: Form[String] = formProvider(containerIndex, Seq.empty)

  private def containerNumberRoute(index: Index = containerIndex): String = routes.ContainerNumberController.onPageLoad(ref, eventIndex, index, NormalMode).url
  private lazy val containerNumberTemplate = "events/transhipments/containerNumber.njk"

  "ContainerNumber Controller" - {

    "must return OK and the correct view for a GET" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val application    = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      val request        = FakeRequest(GET, containerNumberRoute())
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(application, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "form"        -> form,
        "mrn"         -> mrn,
        "mode"        -> NormalMode,
        "onSubmitUrl" -> routes.ContainerNumberController.onSubmit(ref, eventIndex, containerIndex, NormalMode).url
      )

      templateCaptor.getValue mustEqual containerNumberTemplate
      jsonCaptor.getValue must containJson(expectedJson)

      application.stop()
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val userAnswers    = emptyUserAnswers.set(ContainerNumberPage(eventIndex, containerIndex), ContainerDomain("answer")).success.value
      val application    = applicationBuilder(userAnswers = Some(userAnswers)).build()
      val request        = FakeRequest(GET, containerNumberRoute())
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(application, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val filledForm = form.bind(Map("value" -> "answer"))

      val expectedJson = Json.obj(
        "form"        -> filledForm,
        "mrn"         -> mrn,
        "mode"        -> NormalMode,
        "onSubmitUrl" -> routes.ContainerNumberController.onSubmit(ref, eventIndex, containerIndex, NormalMode).url
      )

      templateCaptor.getValue mustEqual containerNumberTemplate
      jsonCaptor.getValue must containJson(expectedJson)

      application.stop()
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      val request =
        FakeRequest(POST, containerNumberRoute())
          .withFormUrlEncodedBody(("value", "answer"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual onwardRoute.url

      application.stop()
    }

    "must redirect to the next page when a value that is the same as the previous is submitted within the same index" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val container   = arbitrary[ContainerDomain].sample.value
      val userAnswers = emptyUserAnswers.set(ContainerNumberPage(eventIndex, containerIndex), container).success.value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      val request =
        FakeRequest(POST, containerNumberRoute())
          .withFormUrlEncodedBody(("value", container.containerNumber))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual onwardRoute.url

      application.stop()
    }

    "must return a Bad Request and errors when an existing container is submitted and index is different to current index" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val container   = arbitrary[ContainerDomain].sample.value
      val userAnswers = emptyUserAnswers.set(ContainerNumberPage(eventIndex, containerIndex), container).success.value
      val nextIndex   = Index(1)

      val application    = applicationBuilder(userAnswers = Some(userAnswers)).build()
      val request        = FakeRequest(POST, containerNumberRoute(nextIndex)).withFormUrlEncodedBody(("value", container.containerNumber))
      val result         = route(application, request).value
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      status(result) mustEqual BAD_REQUEST

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      templateCaptor.getValue mustEqual containerNumberTemplate

      application.stop()
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val application    = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      val request        = FakeRequest(POST, containerNumberRoute()).withFormUrlEncodedBody(("value", ""))
      val boundForm      = form.bind(Map("value" -> ""))
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "form"        -> boundForm,
        "mrn"         -> mrn,
        "mode"        -> NormalMode,
        "onSubmitUrl" -> routes.ContainerNumberController.onSubmit(ref, eventIndex, containerIndex, NormalMode).url
      )

      templateCaptor.getValue mustEqual containerNumberTemplate
      jsonCaptor.getValue must containJson(expectedJson)

      application.stop()
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, containerNumberRoute())

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, containerNumberRoute())
          .withFormUrlEncodedBody(("value", "answer"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }
  }
}
