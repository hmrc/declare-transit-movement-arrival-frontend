/*
 * Copyright 2021 HM Revenue & Customs
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
import org.mockito.Matchers.any
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.when
import org.scalacheck.Gen
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.JsObject
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import services.ArrivalSubmissionService
import uk.gov.hmrc.http.HttpResponse

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

      when(mockService.submit(any())(any())).thenReturn(Future.successful(Some(HttpResponse(ACCEPTED))))

      val request = FakeRequest(POST, routes.CheckYourAnswersController.onPost(mrn).url)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.ConfirmationController.onPageLoad(mrn).url
    }

    "must fail with bad request error on invalid submission" in {

      setExistingUserAnswers(emptyUserAnswers)

      when(mockRenderer.render(any(), any())(any())).thenReturn(Future.successful(Html("")))
      when(mockService.submit(any())(any())).thenReturn(Future.successful(Some(HttpResponse(BAD_REQUEST))))

      val request = FakeRequest(POST, routes.CheckYourAnswersController.onPost(mrn).url)

      val result = route(app, request).value

      val templateCaptor = ArgumentCaptor.forClass(classOf[String])

      status(result) mustEqual BAD_REQUEST

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), any())(any())

      templateCaptor.getValue mustEqual "badRequest.njk"
    }

    "must fail with an Unauthorised error when backend returns 401" in {

      setExistingUserAnswers(emptyUserAnswers)

      when(mockRenderer.render(any(), any())(any())).thenReturn(Future.successful(Html("")))
      when(mockService.submit(any())(any())).thenReturn(Future.successful(Some(HttpResponse(UNAUTHORIZED))))

      val request = FakeRequest(POST, routes.CheckYourAnswersController.onPost(mrn).url)

      val result = route(app, request).value

      val templateCaptor = ArgumentCaptor.forClass(classOf[String])

      status(result) mustEqual UNAUTHORIZED

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), any())(any())

      templateCaptor.getValue mustEqual "unauthorised.njk"
    }

    "must redirected to TechnicalDifficulties page when there is a server side error" in {

      val genServerError = Gen.chooseNum(500, 599).sample.value

      setExistingUserAnswers(emptyUserAnswers)

      when(mockService.submit(any())(any())).thenReturn(Future.successful(Some(HttpResponse(genServerError))))

      val request = FakeRequest(POST, routes.CheckYourAnswersController.onPost(mrn).url)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
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
