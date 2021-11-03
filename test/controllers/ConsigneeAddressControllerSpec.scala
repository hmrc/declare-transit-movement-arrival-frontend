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
import forms.ConsigneeAddressFormProvider
import matchers.JsonMatchers
import models.NormalMode
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.any
import org.mockito.Mockito.{times, verify, when}
import pages.{ConsigneeAddressPage, ConsigneeNamePage}
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import uk.gov.hmrc.viewmodels.NunjucksSupport

import scala.concurrent.Future

class ConsigneeAddressControllerSpec extends SpecBase with AppWithDefaultMockFixtures with NunjucksSupport with JsonMatchers {

  private val formProvider = new ConsigneeAddressFormProvider()
  private val form         = formProvider(consigneeName)

  private lazy val consigneeAddressRoute = routes.ConsigneeAddressController.onPageLoad(mrn, NormalMode).url

  "ConsigneeAddress Controller" - {

    "must return OK and the correct view for a GET" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))
      val userAnswers = emptyUserAnswers
        .set(ConsigneeNamePage, "foo")
        .success
        .value
      setExistingUserAnswers(userAnswers)

      val request        = FakeRequest(GET, consigneeAddressRoute)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "form" -> form,
        "mrn"  -> mrn,
        "mode" -> NormalMode
      )

      templateCaptor.getValue mustEqual "consigneeAddress.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html(consigneeName)))

      val userAnswers = emptyUserAnswers
        .set(ConsigneeNamePage, consigneeName)
        .success
        .value
        .set(ConsigneeAddressPage, traderAddress)
        .success
        .value

      setExistingUserAnswers(userAnswers)

      val request        = FakeRequest(GET, consigneeAddressRoute)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val filledForm =
        form.bind(
          Map(
            "buildingAndStreet" -> traderAddress.buildingAndStreet,
            "city"              -> traderAddress.city,
            "postcode"          -> traderAddress.postcode
          )
        )

      val expectedJson = Json.obj(
        "form"          -> filledForm,
        "mrn"           -> mrn,
        "mode"          -> NormalMode,
        "consigneeName" -> consigneeName
      )

      templateCaptor.getValue mustEqual "consigneeAddress.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val userAnswers = emptyUserAnswers
        .set(ConsigneeNamePage, traderName)
        .success
        .value

      setExistingUserAnswers(userAnswers)

      val request =
        FakeRequest(POST, consigneeAddressRoute)
          .withFormUrlEncodedBody(("buildingAndStreet", traderAddress.buildingAndStreet), ("city", traderAddress.city), ("postcode", traderAddress.postcode))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val userAnswers = emptyUserAnswers
        .set(ConsigneeNamePage, consigneeName)
        .success
        .value

      setExistingUserAnswers(userAnswers)

      val request        = FakeRequest(POST, consigneeAddressRoute).withFormUrlEncodedBody(("value", ""))
      val boundForm      = form.bind(Map("value" -> ""))
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "form"          -> boundForm,
        "mrn"           -> mrn,
        "mode"          -> NormalMode,
        "consigneeName" -> consigneeName
      )

      templateCaptor.getValue mustEqual "consigneeAddress.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, consigneeAddressRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request =
        FakeRequest(POST, consigneeAddressRoute)
          .withFormUrlEncodedBody(("value", "answer"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }
  }
}
