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
import generators.MessagesModelGenerators
import matchers.JsonMatchers
import models.ArrivalId
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.any
import org.mockito.Mockito.{times, verify, when}
import play.api.inject.bind
import play.api.libs.json.JsObject
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import services.ArrivalNotificationService
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.Future

class CheckYourAnswersRejectionsControllerSpec extends SpecBase with JsonMatchers with MessagesModelGenerators {

  val mockArrivalNotificationService = mock[ArrivalNotificationService]

  "Check Your Answers Rejections Controller" - {

    "must return OK and the correct view for a GET" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, routes.CheckYourAnswersRejectionsController.onPageLoad(mrn, ArrivalId(1)).url)

      val result = route(application, request).value

      status(result) mustEqual OK

      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      templateCaptor.getValue mustEqual "check-your-answers-rejections.njk"

      application.stop()
    }

    "must redirect to 'Application Complete' page on valid submission" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[ArrivalNotificationService].toInstance(mockArrivalNotificationService))
        .build()

      when(mockArrivalNotificationService.update(any(), any())(any()))
        .thenReturn(Future.successful(Some(HttpResponse(ACCEPTED))))

      val request = FakeRequest(POST, routes.CheckYourAnswersRejectionsController.onPost(mrn, ArrivalId(1)).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.ConfirmationController.onPageLoad(mrn).url

      application.stop()
    }

    "must redirect to 'Technical Difficulties' page for any service failures" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[ArrivalNotificationService].toInstance(mockArrivalNotificationService))
        .build()

      when(mockArrivalNotificationService.update(any(), any())(any()))
        .thenReturn(Future.successful(None))

      val request = FakeRequest(POST, routes.CheckYourAnswersRejectionsController.onPost(mrn, ArrivalId(1)).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.TechnicalDifficultiesController.onPageLoad().url

      application.stop()
    }

    "must redirect to 'Technical Difficulties' page on internal server error" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[ArrivalNotificationService].toInstance(mockArrivalNotificationService))
        .build()

      when(mockArrivalNotificationService.update(any(), any())(any()))
        .thenReturn(Future.successful(Some(HttpResponse(INTERNAL_SERVER_ERROR))))

      val request = FakeRequest(POST, routes.CheckYourAnswersRejectionsController.onPost(mrn, ArrivalId(1)).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.TechnicalDifficultiesController.onPageLoad().url

      application.stop()
    }
  }
}
