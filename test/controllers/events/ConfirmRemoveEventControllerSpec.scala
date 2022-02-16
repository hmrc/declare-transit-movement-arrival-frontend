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
import forms.events.ConfirmRemoveEventFormProvider
import matchers.JsonMatchers
import models.{Index, NormalMode, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import pages.events.EventPlacePage
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import queries.EventQuery
import uk.gov.hmrc.viewmodels.{NunjucksSupport, Radios}

import scala.concurrent.Future

class ConfirmRemoveEventControllerSpec extends SpecBase with AppWithDefaultMockFixtures with NunjucksSupport with JsonMatchers {

  private val eventTitle   = "eventTitle"
  private val formProvider = new ConfirmRemoveEventFormProvider()
  private val form         = formProvider(eventTitle)

  private lazy val confirmRemoveEventRoute = routes.ConfirmRemoveEventController.onPageLoad(mrn, eventIndex, NormalMode).url
  private val confirmRemoveEventTemplate   = "events/confirmRemoveEvent.njk"

  private val userAnswersWithEventPlace = emptyUserAnswers.set(EventPlacePage(eventIndex), eventTitle).success.value

  "ConfirmRemoveEvent Controller" - {

    "must return OK and the correct view for a GET" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      setExistingUserAnswers(userAnswersWithEventPlace)

      val request        = FakeRequest(GET, confirmRemoveEventRoute)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "form"        -> form,
        "mode"        -> NormalMode,
        "mrn"         -> mrn,
        "eventTitle"  -> eventTitle,
        "radios"      -> Radios.yesNo(form("value")),
        "onSubmitUrl" -> routes.ConfirmRemoveEventController.onSubmit(mrn, eventIndex, NormalMode).url
      )

      templateCaptor.getValue mustEqual confirmRemoveEventTemplate
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "must return error page when user tries to remove an event that does not exists" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val updatedAnswer = userAnswersWithEventPlace.remove(EventQuery(eventIndex)).success.value
      setExistingUserAnswers(updatedAnswer)

      val request        = FakeRequest(GET, confirmRemoveEventRoute)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual NOT_FOUND

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "pageTitle"    -> msg"concurrent.remove.error.title".withArgs("event"),
        "pageHeading"  -> msg"concurrent.remove.error.heading".withArgs("event"),
        "linkText"     -> msg"concurrent.remove.error.noEvent.link.text",
        "redirectLink" -> onwardRoute.url
      )

      templateCaptor.getValue mustEqual "concurrentRemoveError.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "must return error page when there are multiple events and user tries to remove the last event that is already removed" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val routeWithLastIndex = routes.ConfirmRemoveEventController.onPageLoad(mrn, Index(2), NormalMode).url
      val updatedAnswer      = userAnswersWithEventPlace.set(EventPlacePage(Index(1)), "place").success.value

      setExistingUserAnswers(updatedAnswer)

      val request        = FakeRequest(GET, routeWithLastIndex)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual NOT_FOUND

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "pageTitle"    -> msg"concurrent.remove.error.title".withArgs("event"),
        "pageHeading"  -> msg"concurrent.remove.error.heading".withArgs("event"),
        "linkText"     -> msg"concurrent.remove.error.multipleEvent.link.text",
        "redirectLink" -> onwardRoute.url
      )

      templateCaptor.getValue mustEqual "concurrentRemoveError.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "must redirect to the next page when valid data is submitted and call to remove event" in {

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      setExistingUserAnswers(userAnswersWithEventPlace)

      val request =
        FakeRequest(POST, confirmRemoveEventRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url

      val uaRemoveEvent = UserAnswers(
        userAnswersWithEventPlace.movementReferenceNumber,
        userAnswersWithEventPlace.eoriNumber,
        userAnswersWithEventPlace.remove(EventQuery(eventIndex)).success.value.data,
        userAnswersWithEventPlace.lastUpdated,
        id = userAnswersWithEventPlace.id
      )

      verify(mockSessionRepository, times(1)).set(uaRemoveEvent)
    }

    "must redirect to the next page when valid data is submitted and call to remove event is false" in {

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      setExistingUserAnswers(userAnswersWithEventPlace)

      val request =
        FakeRequest(POST, confirmRemoveEventRoute)
          .withFormUrlEncodedBody(("value", "false"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url

      val uaRemoveEvent = UserAnswers(
        userAnswersWithEventPlace.movementReferenceNumber,
        userAnswersWithEventPlace.eoriNumber,
        userAnswersWithEventPlace.remove(EventQuery(eventIndex)).success.value.data,
        userAnswersWithEventPlace.lastUpdated
      )

      verify(mockSessionRepository, times(0)).set(uaRemoveEvent)
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      setExistingUserAnswers(userAnswersWithEventPlace)

      val request        = FakeRequest(POST, confirmRemoveEventRoute).withFormUrlEncodedBody(("value", ""))
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
        "onSubmitUrl" -> routes.ConfirmRemoveEventController.onSubmit(mrn, eventIndex, NormalMode).url
      )

      templateCaptor.getValue mustEqual confirmRemoveEventTemplate
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, confirmRemoveEventRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request =
        FakeRequest(POST, confirmRemoveEventRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }
  }
}
