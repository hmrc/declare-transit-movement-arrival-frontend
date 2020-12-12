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

import base.{AppWithDefaultMockFixtures, SpecBase}
import config.FrontendAppConfig
import matchers.JsonMatchers
import models.reference.CustomsOffice
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.any
import org.mockito.Mockito.{times, verify, when}
import pages.CustomsOfficePage
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import uk.gov.hmrc.viewmodels.{NunjucksSupport, Text}

import scala.concurrent.Future

class ConfirmationControllerSpec extends SpecBase with AppWithDefaultMockFixtures with JsonMatchers with NunjucksSupport {

  "Confirmation Controller" - {

    "return OK and the correct view when there is no phone number for a GET then remove data" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val customsOffice = CustomsOffice("id", Some("name"), Seq.empty, None)
      val userAnswers   = emptyUserAnswers.set(CustomsOfficePage, customsOffice).success.value
      setExistingUserAnswers(userAnswers)

      val frontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
      val request           = FakeRequest(GET, routes.ConfirmationController.onPageLoad(mrn).url)
      val templateCaptor    = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor        = ArgumentCaptor.forClass(classOf[JsObject])
      val result            = route(app, request).value

      val contactUsMessage: Text.Message = msg"arrivalComplete.para2".withArgs(customsOffice.name)

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())
      verify(mockSessionRepository, times(1)).remove(mrn.toString)

      val expectedJson =
        Json.obj("mrn" -> mrn, "contactUs" -> contactUsMessage, "manageTransitMovementsUrl" -> frontendAppConfig.manageTransitMovementsUrl)

      templateCaptor.getValue mustEqual "arrivalComplete.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "return OK and the correct view when there is phone number for a GET the data" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val customsOffice = CustomsOffice("id", Some("name"), Seq.empty, Some("phoneNumber"))
      val userAnswers   = emptyUserAnswers.set(CustomsOfficePage, customsOffice).success.value
      setExistingUserAnswers(userAnswers)

      val request        = FakeRequest(GET, routes.ConfirmationController.onPageLoad(mrn).url)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])
      val result         = route(app, request).value

      val contactUsMessage: Text.Message = msg"arrivalComplete.para2.withPhoneNumber".withArgs(customsOffice.name, customsOffice.phoneNumber.get)

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())
      verify(mockSessionRepository, times(1)).remove(mrn.toString)

      val expectedJson = Json.obj("mrn" -> mrn, "contactUs" -> contactUsMessage)

      templateCaptor.getValue mustEqual "arrivalComplete.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }
  }
}
