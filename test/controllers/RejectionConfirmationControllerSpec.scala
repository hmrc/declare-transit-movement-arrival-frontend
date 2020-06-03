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
import models.reference.CustomsOffice
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.PresentationOfficePage
import play.api.inject.bind
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import repositories.SessionRepository
import uk.gov.hmrc.viewmodels.{NunjucksSupport, Text}

import scala.concurrent.Future

class RejectionConfirmationControllerSpec extends SpecBase with MockitoSugar with JsonMatchers with NunjucksSupport {

  "Rejection Confirmation Controller" - {

    "return OK and the correct view when there is no phone number for a GET then remove data" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val mockSessionRepository = mock[SessionRepository]
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .build()
      val request        = FakeRequest(GET, routes.RejectionConfirmationController.onPageLoad(mrn).url)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])
      val result         = route(application, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())
      verify(mockSessionRepository, times(1)).remove(mrn.toString)

      val expectedJson =
        Json.obj("mrn" -> mrn, "manageTransitMovementsUrl" -> frontendAppConfig.manageTransitMovementsUrl)

      templateCaptor.getValue mustEqual "rejectedArrivalComplete.njk"
      jsonCaptor.getValue must containJson(expectedJson)

      application.stop()
    }

  }

}
