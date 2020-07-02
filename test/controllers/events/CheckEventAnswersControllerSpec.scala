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
import connectors.ReferenceDataConnector
import generators.MessagesModelGenerators
import matchers.JsonMatchers
import models.{CountryList, NormalMode}
import models.reference.Country
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.any
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.when
import play.api.libs.json.JsObject
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import org.scalacheck.Arbitrary.arbitrary
import play.api.inject.bind

import scala.concurrent.Future

class CheckEventAnswersControllerSpec extends SpecBase with JsonMatchers with MessagesModelGenerators {

  "Check Event Answers Controller" - {

    "must return OK and the correct view for a GET" in {

      val sampleCountryList          = arbitrary[Seq[Country]].sample.value
      val mockReferenceDataConnector = mock[ReferenceDataConnector]

      when(mockReferenceDataConnector.getCountryList()(any(), any()))
        .thenReturn(Future.successful(CountryList(sampleCountryList)))

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[ReferenceDataConnector].toInstance(mockReferenceDataConnector))
        .build()

      val request = FakeRequest(GET, routes.CheckEventAnswersController.onPageLoad(mrn, eventIndex).url)

      val result = route(application, request).value

      status(result) mustEqual OK

      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      templateCaptor.getValue mustEqual "events/check-event-answers.njk"

      application.stop()
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, routes.CheckEventAnswersController.onPageLoad(mrn, eventIndex).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "must redirect to Add event page" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .build()

      when(mockRenderer.render(any(), any())(any())).thenReturn(Future.successful(Html("")))

      val request = FakeRequest(POST, controllers.events.routes.CheckEventAnswersController.onSubmit(mrn, eventIndex).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.events.routes.AddEventController.onPageLoad(mrn, NormalMode).url

      application.stop()

    }
  }
}
