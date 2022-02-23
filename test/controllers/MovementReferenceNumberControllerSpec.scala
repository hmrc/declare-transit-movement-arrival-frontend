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

package controllers

import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.MovementReferenceNumberFormProvider
import matchers.JsonMatchers
import models.UserAnswers
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import services.UserAnswersService
import uk.gov.hmrc.viewmodels.NunjucksSupport

import scala.concurrent.Future

class MovementReferenceNumberControllerSpec extends SpecBase with AppWithDefaultMockFixtures with NunjucksSupport with JsonMatchers {

  val formProvider = new MovementReferenceNumberFormProvider()
  val form         = formProvider()

  private lazy val movementReferenceNumberRoute = routes.MovementReferenceNumberController.onPageLoad().url

  private val mockUserAnswersService = mock[UserAnswersService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[UserAnswersService].toInstance(mockUserAnswersService))

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockUserAnswersService)
  }

  "MovementReferenceNumber Controller" - {

    "must return OK and the correct view for a GET" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      setNoExistingUserAnswers()

      val request        = FakeRequest(GET, movementReferenceNumberRoute)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "form" -> form
      )

      templateCaptor.getValue mustEqual "movementReferenceNumber.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "must redirect to the next page when valid data is submitted and sessionRepository returns UserAnswers value as Some(_)" in {

      val userAnswersCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockUserAnswersService.getOrCreateUserAnswers(any(), any())) thenReturn Future.successful(emptyUserAnswers)

      setNoExistingUserAnswers()

      val request =
        FakeRequest(POST, movementReferenceNumberRoute)
          .withFormUrlEncodedBody(("value", mrn.toString))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual onwardRoute.url
      verify(mockSessionRepository).set(userAnswersCaptor.capture())

      userAnswersCaptor.getValue.movementReferenceNumber mustBe mrn
      userAnswersCaptor.getValue.eoriNumber mustBe eoriNumber
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      setExistingUserAnswers(emptyUserAnswers)

      val request        = FakeRequest(POST, movementReferenceNumberRoute).withFormUrlEncodedBody(("value", ""))
      val boundForm      = form.bind(Map("value" -> ""))
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "form" -> boundForm
      )

      templateCaptor.getValue mustEqual "movementReferenceNumber.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }
  }
}
