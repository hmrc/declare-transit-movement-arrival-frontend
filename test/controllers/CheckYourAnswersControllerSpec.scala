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

package controllers

import base.SpecBase
import matchers.JsonMatchers
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.any
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.when
import org.scalacheck.Gen
import play.api.inject.bind
import play.api.libs.json.JsObject
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import services.ArrivalNotificationService
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.Future

class CheckYourAnswersControllerSpec extends SpecBase with JsonMatchers {

  val mockService: ArrivalNotificationService = mock[ArrivalNotificationService]

  "Check Your Answers Controller" - {

    "must return OK and the correct view for a GET" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad(mrn).url)

      val result = route(application, request).value

      status(result) mustEqual OK

      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      templateCaptor.getValue mustEqual "check-your-answers.njk"

      application.stop()
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad(mrn).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "must redirect to 'Application Complete' page on valid submission" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[ArrivalNotificationService].toInstance(mockService))
        .build()

      when(mockService.submit(any(), any())(any())).thenReturn(Future.successful(Some(HttpResponse(ACCEPTED))))

      val request = FakeRequest(POST, routes.CheckYourAnswersController.onPost(mrn).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.ConfirmationController.onPageLoad(mrn).url

      application.stop()
    }

    "must fail with bad request error on invalid submission" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[ArrivalNotificationService].toInstance(mockService))
        .build()

      when(mockRenderer.render(any(), any())(any())).thenReturn(Future.successful(Html("")))
      when(mockService.submit(any(), any())(any())).thenReturn(Future.successful(Some(HttpResponse(BAD_REQUEST))))

      val request = FakeRequest(POST, routes.CheckYourAnswersController.onPost(mrn).url)

      val result = route(application, request).value

      val templateCaptor = ArgumentCaptor.forClass(classOf[String])

      status(result) mustEqual BAD_REQUEST

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), any())(any())

      templateCaptor.getValue mustEqual "badRequest.njk"

      application.stop()
    }

    "must fail with an Unauthorised error when backend returns 401" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[ArrivalNotificationService].toInstance(mockService))
        .build()

      when(mockRenderer.render(any(), any())(any())).thenReturn(Future.successful(Html("")))
      when(mockService.submit(any(), any())(any())).thenReturn(Future.successful(Some(HttpResponse(UNAUTHORIZED))))

      val request = FakeRequest(POST, routes.CheckYourAnswersController.onPost(mrn).url)

      val result = route(application, request).value

      val templateCaptor = ArgumentCaptor.forClass(classOf[String])

      status(result) mustEqual UNAUTHORIZED

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), any())(any())

      templateCaptor.getValue mustEqual "unauthorised.njk"

      application.stop()
    }

    "must redirected to TechnicalDifficulties page when there is a server side error" in {

      val genServerError = Gen.chooseNum(500, 599).sample.value

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[ArrivalNotificationService].toInstance(mockService))
        .build()

      when(mockService.submit(any(), any())(any())).thenReturn(Future.successful(Some(HttpResponse(genServerError))))

      val request = FakeRequest(POST, routes.CheckYourAnswersController.onPost(mrn).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      application.stop()
    }

    "must fail with internal server error when service fails" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[ArrivalNotificationService].toInstance(mockService))
        .build()

      when(mockRenderer.render(any(), any())(any())).thenReturn(Future.successful(Html("")))
      when(mockService.submit(any(), any())(any())).thenReturn(Future.successful(None))

      val request = FakeRequest(POST, routes.CheckYourAnswersController.onPost(mrn).url)

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      application.stop()
    }
  }
}
