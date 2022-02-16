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
import forms.events.transhipments.ConfirmRemoveContainerFormProvider
import matchers.JsonMatchers
import models.{Index, NormalMode, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import pages.events.transhipments.ContainerNumberPage
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import uk.gov.hmrc.viewmodels.{NunjucksSupport, Radios}

import scala.concurrent.Future

class ConfirmRemoveContainerControllerSpec extends SpecBase with AppWithDefaultMockFixtures with NunjucksSupport with JsonMatchers {

  private val formProvider = new ConfirmRemoveContainerFormProvider()
  private val form         = formProvider(domainContainer)

  private lazy val confirmRemoveContainerRoute    = routes.ConfirmRemoveContainerController.onPageLoad(mrn, eventIndex, containerIndex, NormalMode).url
  private lazy val confirmRemoveContainerTemplate = "events/transhipments/confirmRemoveContainer.njk"

  private val presetUserAnswers =
    emptyUserAnswers.set(ContainerNumberPage(eventIndex, containerIndex), domainContainer).success.value

  "ConfirmRemoveContainer Controller" - {

    "must return OK and the correct view for a GET" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      setExistingUserAnswers(presetUserAnswers)

      val request        = FakeRequest(GET, confirmRemoveContainerRoute)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "form"            -> form,
        "mode"            -> NormalMode,
        "mrn"             -> mrn,
        "containerNumber" -> container.containerNumber,
        "radios"          -> Radios.yesNo(form("value")),
        "onSubmitUrl"     -> routes.ConfirmRemoveContainerController.onSubmit(mrn, eventIndex, containerIndex, NormalMode).url
      )

      templateCaptor.getValue mustEqual confirmRemoveContainerTemplate
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "must return error page when user tries to remove a container that does not exists" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))
      val updatedAnswer = presetUserAnswers.remove(ContainerNumberPage(eventIndex, containerIndex)).success.value
      setExistingUserAnswers(updatedAnswer)

      val request        = FakeRequest(GET, confirmRemoveContainerRoute)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual NOT_FOUND

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "pageTitle"    -> msg"concurrent.remove.error.title".withArgs("container"),
        "pageHeading"  -> msg"concurrent.remove.error.heading".withArgs("container"),
        "linkText"     -> msg"concurrent.remove.error.noContainer.link.text",
        "redirectLink" -> onwardRoute.url
      )

      templateCaptor.getValue mustEqual "concurrentRemoveError.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "must return error page when there are multiple containers and user tries to remove the last container that is already removed" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))
      val updatedAnswer = presetUserAnswers
        .set(ContainerNumberPage(eventIndex, Index(1)), domainContainer)
        .success
        .value
        .set(ContainerNumberPage(eventIndex, Index(2)), domainContainer)
        .success
        .value
        .remove(ContainerNumberPage(eventIndex, Index(2)))
        .success
        .value

      val removeContainerRoute = routes.ConfirmRemoveContainerController.onPageLoad(mrn, eventIndex, Index(2), NormalMode).url

      setExistingUserAnswers(updatedAnswer)

      val request        = FakeRequest(GET, removeContainerRoute)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual NOT_FOUND

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "pageTitle"    -> msg"concurrent.remove.error.title".withArgs("container"),
        "pageHeading"  -> msg"concurrent.remove.error.heading".withArgs("container"),
        "linkText"     -> msg"concurrent.remove.error.multipleContainer.link.text",
        "redirectLink" -> onwardRoute.url
      )

      templateCaptor.getValue mustEqual "concurrentRemoveError.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "must redirect to the next page when valid data is submitted and call to remove data when true" in {

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      setExistingUserAnswers(presetUserAnswers)

      val request =
        FakeRequest(POST, confirmRemoveContainerRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      val updateAnswers = UserAnswers(
        id = presetUserAnswers.id,
        eoriNumber = presetUserAnswers.eoriNumber,
        data = presetUserAnswers.remove(ContainerNumberPage(eventIndex, containerIndex)).success.value.data,
        lastUpdated = presetUserAnswers.lastUpdated,
        movementReferenceNumber = presetUserAnswers.movementReferenceNumber
      )

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url

      verify(mockSessionRepository, times(1)).set(updateAnswers)
    }

    "must redirect to the next page when valid data is submitted and not call to remove data when false" in {

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      setExistingUserAnswers(presetUserAnswers)

      val request =
        FakeRequest(POST, confirmRemoveContainerRoute)
          .withFormUrlEncodedBody(("value", "false"))

      val result = route(app, request).value

      val updateAnswers = UserAnswers(
        id = presetUserAnswers.id,
        eoriNumber = eoriNumber,
        data = presetUserAnswers.remove(ContainerNumberPage(eventIndex, containerIndex)).success.value.data,
        lastUpdated = presetUserAnswers.lastUpdated,
        movementReferenceNumber = presetUserAnswers.movementReferenceNumber
      )

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url

      verify(mockSessionRepository, times(0)).set(updateAnswers)
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      setExistingUserAnswers(presetUserAnswers)

      val request        = FakeRequest(POST, confirmRemoveContainerRoute).withFormUrlEncodedBody(("value", ""))
      val boundForm      = form.bind(Map("value" -> ""))
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "form"        -> boundForm,
        "mode"        -> NormalMode,
        "mrn"         -> mrn,
        "radios"      -> Radios.yesNo(boundForm("value")),
        "onSubmitUrl" -> routes.ConfirmRemoveContainerController.onSubmit(mrn, eventIndex, containerIndex, NormalMode).url
      )

      templateCaptor.getValue mustEqual confirmRemoveContainerTemplate
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, confirmRemoveContainerRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request =
        FakeRequest(POST, confirmRemoveContainerRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }
  }
}
