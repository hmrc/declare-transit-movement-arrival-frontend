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
import forms.events.ConfirmRemoveEventFormProvider
import matchers.JsonMatchers
import models.{Index, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.events.EventPlacePage
import play.api.inject.bind
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import queries.EventQuery
import repositories.SessionRepository
import uk.gov.hmrc.viewmodels.{NunjucksSupport, Radios}

import scala.concurrent.Future

class ConfirmRemoveEventControllerSpec extends SpecBase with MockitoSugar with NunjucksSupport with JsonMatchers {

  private def onwardRoute = Call("GET", "/foo")

  private val formProvider = new ConfirmRemoveEventFormProvider()
  private val form         = formProvider()

  private lazy val confirmRemoveEventRoute = routes.ConfirmRemoveEventController.onPageLoad(mrn, eventIndex, NormalMode).url
  private val confirmRemoveEventTemplate   = "events/confirmRemoveEvent.njk"

  private val eventPlace                = "eventPlace"
  private val userAnswersWithEventPlace = emptyUserAnswers.set(EventPlacePage(eventIndex), eventPlace).success.value

  "ConfirmRemoveEvent Controller" - {

    "must return OK and the correct view for a GET" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val application    = applicationBuilder(userAnswers = Some(userAnswersWithEventPlace)).build()
      val request        = FakeRequest(GET, confirmRemoveEventRoute)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(application, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "form"       -> form,
        "mode"       -> NormalMode,
        "mrn"        -> mrn,
        "eventTitle" -> eventPlace,
        "radios"     -> Radios.yesNo(form("value"))
      )

      templateCaptor.getValue mustEqual confirmRemoveEventTemplate
      jsonCaptor.getValue must containJson(expectedJson)

      application.stop()
    }

    "must return error page when user tries to remove an event that does not exists" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val updatedAnswer  = userAnswersWithEventPlace.remove(EventQuery(eventIndex)).success.value
      val application    = applicationBuilder(userAnswers = Some(updatedAnswer)).build()
      val request        = FakeRequest(GET, confirmRemoveEventRoute)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(application, request).value

      status(result) mustEqual NOT_FOUND

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "pageTitle"    -> msg"concurrent.remove.error.title".withArgs("event"),
        "pageHeading"  -> msg"concurrent.remove.error.heading".withArgs("event"),
        "linkText"     -> msg"concurrent.remove.error.noEvent.link.text",
        "redirectLink" -> controllers.routes.IncidentOnRouteController.onPageLoad(mrn, NormalMode).url
      )

      templateCaptor.getValue mustEqual "concurrentRemoveError.njk"
      jsonCaptor.getValue must containJson(expectedJson)
      application.stop()
    }

    "must return error page when there are multiple events and user tries to remove the last event that is already removed" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val routeWithLastIndex = routes.ConfirmRemoveEventController.onPageLoad(mrn, Index(2), NormalMode).url
      val updatedAnswer      = userAnswersWithEventPlace.set(EventPlacePage(Index(1)), "place").success.value

      val application    = applicationBuilder(userAnswers = Some(updatedAnswer)).build()
      val request        = FakeRequest(GET, routeWithLastIndex)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(application, request).value

      status(result) mustEqual NOT_FOUND

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "pageTitle"    -> msg"concurrent.remove.error.title".withArgs("event"),
        "pageHeading"  -> msg"concurrent.remove.error.heading".withArgs("event"),
        "linkText"     -> msg"concurrent.remove.error.multipleEvent.link.text",
        "redirectLink" -> routes.AddEventController.onPageLoad(mrn, NormalMode).url
      )

      templateCaptor.getValue mustEqual "concurrentRemoveError.njk"
      jsonCaptor.getValue must containJson(expectedJson)
      application.stop()
    }

    "must redirect to the next page when valid data is submitted and call to remove event" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithEventPlace))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      val request =
        FakeRequest(POST, confirmRemoveEventRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url

      val uaRemoveEvent = UserAnswers(userAnswersWithEventPlace.id,
                                      userAnswersWithEventPlace.remove(EventQuery(eventIndex)).success.value.data,
                                      userAnswersWithEventPlace.lastUpdated)

      verify(mockSessionRepository, times(1)).set(uaRemoveEvent)

      application.stop()
    }

    "must redirect to the next page when valid data is submitted and call to remove event is false" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithEventPlace))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      val request =
        FakeRequest(POST, confirmRemoveEventRoute)
          .withFormUrlEncodedBody(("value", "false"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url

      val uaRemoveEvent = UserAnswers(userAnswersWithEventPlace.id,
                                      userAnswersWithEventPlace.remove(EventQuery(eventIndex)).success.value.data,
                                      userAnswersWithEventPlace.lastUpdated)

      verify(mockSessionRepository, times(0)).set(uaRemoveEvent)

      application.stop()
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val application    = applicationBuilder(userAnswers = Some(userAnswersWithEventPlace)).build()
      val request        = FakeRequest(POST, confirmRemoveEventRoute).withFormUrlEncodedBody(("value", ""))
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

      templateCaptor.getValue mustEqual confirmRemoveEventTemplate
      jsonCaptor.getValue must containJson(expectedJson)

      application.stop()
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, confirmRemoveEventRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, confirmRemoveEventRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }
  }
}
