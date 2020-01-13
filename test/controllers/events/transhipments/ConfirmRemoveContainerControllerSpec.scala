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
import forms.events.transhipments.ConfirmRemoveContainerFormProvider
import matchers.JsonMatchers
import models.domain.Container
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.events.transhipments.{ConfirmRemoveContainerPage, ContainerNumberPage}
import play.api.inject.bind
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import repositories.SessionRepository
import uk.gov.hmrc.viewmodels.{NunjucksSupport, Radios}

import scala.concurrent.Future

class ConfirmRemoveContainerControllerSpec extends SpecBase with MockitoSugar with NunjucksSupport with JsonMatchers {

  private def onwardRoute = Call("GET", "/foo")

  private val formProvider = new ConfirmRemoveContainerFormProvider()
  private val form         = formProvider()

  private lazy val confirmRemoveContainerRoute    = routes.ConfirmRemoveContainerController.onPageLoad(mrn, eventIndex, containerIndex, NormalMode).url
  private lazy val confirmRemoveContainerTemplate = "events/transhipments/confirmRemoveContainer.njk"

  private val containerNumber = "test"
  private val presetUserAnswers =
    emptyUserAnswers.set(ContainerNumberPage(eventIndex, containerIndex), Container(containerNumber)).success.value

  "ConfirmRemoveContainer Controller" - {

    "must return OK and the correct view for a GET" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val application    = applicationBuilder(userAnswers = Some(presetUserAnswers)).build()
      val request        = FakeRequest(GET, confirmRemoveContainerRoute)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(application, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "form"            -> form,
        "mode"            -> NormalMode,
        "mrn"             -> mrn,
        "containerNumber" -> containerNumber,
        "radios"          -> Radios.yesNo(form("value"))
      )

      templateCaptor.getValue mustEqual confirmRemoveContainerTemplate
      jsonCaptor.getValue must containJson(expectedJson)

      application.stop()
    }

    "must redirect to the next page when valid data is submitted and call to remove data when true" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(presetUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      val request =
        FakeRequest(POST, confirmRemoveContainerRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      val updateAnswers = UserAnswers(
        id          = presetUserAnswers.id,
        data        = presetUserAnswers.remove(ContainerNumberPage(eventIndex, containerIndex)).success.value.data,
        lastUpdated = presetUserAnswers.lastUpdated
      )

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url

      verify(mockSessionRepository, times(1)).set(updateAnswers)

      application.stop()
    }

    "must redirect to the next page when valid data is submitted and not call to remove data when false" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(presetUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      val request =
        FakeRequest(POST, confirmRemoveContainerRoute)
          .withFormUrlEncodedBody(("value", "false"))

      val result = route(application, request).value

      val updateAnswers = UserAnswers(
        id          = presetUserAnswers.id,
        data        = presetUserAnswers.remove(ContainerNumberPage(eventIndex, containerIndex)).success.value.data,
        lastUpdated = presetUserAnswers.lastUpdated
      )

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url

      verify(mockSessionRepository, times(0)).set(updateAnswers)

      application.stop()
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val application    = applicationBuilder(userAnswers = Some(presetUserAnswers)).build()
      val request        = FakeRequest(POST, confirmRemoveContainerRoute).withFormUrlEncodedBody(("value", ""))
      val boundForm      = form.bind(Map("value" -> ""))
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "form"   -> boundForm,
        "mode"   -> NormalMode,
        "mrn"    -> mrn,
        "radios" -> Radios.yesNo(boundForm("value"))
      )

      templateCaptor.getValue mustEqual confirmRemoveContainerTemplate
      jsonCaptor.getValue must containJson(expectedJson)

      application.stop()
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, confirmRemoveContainerRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, confirmRemoveContainerRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }
  }
}
