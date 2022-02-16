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
import matchers.JsonMatchers
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalacheck.Gen
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import services.ArrivalSubmissionService
import uk.gov.hmrc.http.HttpResponse
import viewModels.sections.ViewModelConfig

import scala.concurrent.Future

class CheckYourAnswersControllerSpec extends SpecBase with AppWithDefaultMockFixtures with JsonMatchers {

  val mockService: ArrivalSubmissionService = mock[ArrivalSubmissionService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[ArrivalSubmissionService].toInstance(mockService))

  "Check Your Answers Controller" - {

    "must return OK and the correct view for a GET" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad(mrn).url)

      val result = route(app, request).value

      status(result) mustEqual OK

      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      templateCaptor.getValue mustEqual "check-your-answers.njk"
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad(mrn).url)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to 'Application Complete' page on valid submission" in {

      setExistingUserAnswers(emptyUserAnswers)

      when(mockService.submit(any())(any())).thenReturn(Future.successful(Some(HttpResponse(ACCEPTED, ""))))

      val request = FakeRequest(POST, routes.CheckYourAnswersController.onPost(mrn).url)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.ConfirmationController.onPageLoad(mrn).url
    }

    "must fail with bad request error on invalid submission" in {

      setExistingUserAnswers(emptyUserAnswers)

      when(mockRenderer.render(any(), any())(any())).thenReturn(Future.successful(Html("")))
      when(mockService.submit(any())(any())).thenReturn(Future.successful(Some(HttpResponse(BAD_REQUEST, ""))))

      val request = FakeRequest(POST, routes.CheckYourAnswersController.onPost(mrn).url)

      val result = route(app, request).value

      val templateCaptor = ArgumentCaptor.forClass(classOf[String])

      status(result) mustEqual BAD_REQUEST

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), any())(any())

      templateCaptor.getValue mustEqual "badRequest.njk"
    }

    "must redirected to TechnicalDifficulties page when there is a server side error" in {

      val genServerError: Int = Gen.chooseNum(500, 599).sample.value
      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))
      val viewConfig = app.injector.instanceOf[ViewModelConfig]

      setExistingUserAnswers(emptyUserAnswers)

      when(mockService.submit(any())(any())).thenReturn(Future.successful(Some(HttpResponse.apply(genServerError, ""))))

      val request = FakeRequest(POST, routes.CheckYourAnswersController.onPost(mrn).url)

      val result = route(app, request).value

      status(result) mustEqual INTERNAL_SERVER_ERROR
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())
      val expectedJson = Json.obj(
        "contactUrl" -> viewConfig.nctsEnquiriesUrl
      )

      templateCaptor.getValue mustEqual "technicalDifficulties.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "must fail with internal server error when service fails" in {

      setExistingUserAnswers(emptyUserAnswers)

      when(mockRenderer.render(any(), any())(any())).thenReturn(Future.successful(Html("")))
      when(mockService.submit(any())(any())).thenReturn(Future.successful(None))

      val request = FakeRequest(POST, routes.CheckYourAnswersController.onPost(mrn).url)

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST
    }
  }
}
