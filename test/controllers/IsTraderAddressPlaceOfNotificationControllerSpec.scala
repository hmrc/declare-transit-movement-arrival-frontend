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
import forms.IsTraderAddressPlaceOfNotificationFormProvider
import matchers.JsonMatchers
import models.NormalMode
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import pages.{IsTraderAddressPlaceOfNotificationPage, TraderAddressPage, TraderNamePage}
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import uk.gov.hmrc.nunjucks.NunjucksSupport
import uk.gov.hmrc.viewmodels.Radios

import scala.concurrent.Future

class IsTraderAddressPlaceOfNotificationControllerSpec extends SpecBase with AppWithDefaultMockFixtures with NunjucksSupport with JsonMatchers {

  private val formProvider = new IsTraderAddressPlaceOfNotificationFormProvider()
  private val form         = formProvider(traderName)

  lazy val isTraderAddressPlaceOfNotificationRoute = routes.IsTraderAddressPlaceOfNotificationController.onPageLoad(mrn, NormalMode).url

  "IsTraderAddressPlaceOfNotification Controller" - {
    "must return OK and the correct view for a GET" in {
      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val userAnswers = emptyUserAnswers
        .set(TraderAddressPage, traderAddress)
        .success
        .value
      setExistingUserAnswers(userAnswers)

      val request        = FakeRequest(GET, isTraderAddressPlaceOfNotificationRoute)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual OK
      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "form"           -> form,
        "mode"           -> NormalMode,
        "mrn"            -> mrn,
        "traderLine1"    -> traderAddress.buildingAndStreet,
        "traderTown"     -> traderAddress.city,
        "traderPostcode" -> traderAddress.postcode,
        "radios"         -> Radios.yesNo(form("value"))
      )

      templateCaptor.getValue mustEqual "isTraderAddressPlaceOfNotification.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val userAnswers = emptyUserAnswers
        .set(IsTraderAddressPlaceOfNotificationPage, true)
        .success
        .value
        .set(TraderAddressPage, traderAddress)
        .success
        .value
      setExistingUserAnswers(userAnswers)

      val request        = FakeRequest(GET, isTraderAddressPlaceOfNotificationRoute)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual OK
      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val filledForm = form.bind(Map("value" -> "true"))
      val expectedJson = Json.obj(
        "form"           -> filledForm,
        "mode"           -> NormalMode,
        "mrn"            -> mrn,
        "traderLine1"    -> traderAddress.buildingAndStreet,
        "traderTown"     -> traderAddress.city,
        "traderPostcode" -> traderAddress.postcode,
        "radios"         -> Radios.yesNo(filledForm("value"))
      )

      templateCaptor.getValue mustEqual "isTraderAddressPlaceOfNotification.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val userAnswers = emptyUserAnswers.set(TraderAddressPage, traderAddress).success.value
      setExistingUserAnswers(userAnswers)

      val request =
        FakeRequest(POST, isTraderAddressPlaceOfNotificationRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val userAnswers = emptyUserAnswers
        .set(TraderNamePage, traderName)
        .success
        .value
        .set(TraderAddressPage, traderAddress)
        .success
        .value

      setExistingUserAnswers(userAnswers)

      val request        = FakeRequest(POST, isTraderAddressPlaceOfNotificationRoute).withFormUrlEncodedBody(("value", ""))
      val boundForm      = form.bind(Map("value" -> ""))
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST
      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "form"           -> boundForm,
        "mode"           -> NormalMode,
        "mrn"            -> mrn,
        "traderLine1"    -> traderAddress.buildingAndStreet,
        "traderTown"     -> traderAddress.city,
        "traderPostcode" -> traderAddress.postcode,
        "radios"         -> Radios.yesNo(boundForm("value")),
        "traderName"     -> traderName
      )

      templateCaptor.getValue mustEqual "isTraderAddressPlaceOfNotification.njk"

      jsonCaptor.getValue must containJson(expectedJson)
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {
      setNoExistingUserAnswers()
      val request = FakeRequest(GET, isTraderAddressPlaceOfNotificationRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a GET if no traderAddress data is found" in {
      setExistingUserAnswers(emptyUserAnswers)
      val request = FakeRequest(GET, isTraderAddressPlaceOfNotificationRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {
      setNoExistingUserAnswers()

      val request =
        FakeRequest(POST, isTraderAddressPlaceOfNotificationRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no traderAddress data is found" in {
      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(POST, isTraderAddressPlaceOfNotificationRoute)
        .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }
  }
}
