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
import forms.GoodsLocationFormProvider
import matchers.JsonMatchers
import models.{GoodsLocation, NormalMode}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import pages.GoodsLocationPage
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import uk.gov.hmrc.viewmodels.NunjucksSupport

import scala.concurrent.Future

class GoodsLocationControllerSpec extends SpecBase with AppWithDefaultMockFixtures with NunjucksSupport with JsonMatchers {

  lazy val goodsLocationRoute = routes.GoodsLocationController.onPageLoad(mrn, NormalMode).url

  val formProvider = new GoodsLocationFormProvider()
  val form         = formProvider()

  "GoodsLocation Controller" - {

    "must return OK and the correct view for a GET" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      setExistingUserAnswers(emptyUserAnswers)

      val request        = FakeRequest(GET, goodsLocationRoute)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "form"   -> form,
        "mode"   -> NormalMode,
        "mrn"    -> mrn,
        "radios" -> GoodsLocation.radios(form)
      )

      templateCaptor.getValue mustEqual "goodsLocation.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val userAnswers = emptyUserAnswers.set(GoodsLocationPage, GoodsLocation.values.head).success.value
      setExistingUserAnswers(userAnswers)

      val request        = FakeRequest(GET, goodsLocationRoute)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val filledForm = form.bind(Map("value" -> GoodsLocation.values.head.toString))

      val expectedJson = Json.obj(
        "form"   -> filledForm,
        "mode"   -> NormalMode,
        "mrn"    -> mrn,
        "radios" -> GoodsLocation.radios(filledForm)
      )

      templateCaptor.getValue mustEqual "goodsLocation.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "must redirect to the correct page for Border Force Office when valid data is submitted" in {

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val userAnswers = emptyUserAnswers.set(GoodsLocationPage, GoodsLocation.values.head).success.value
      setExistingUserAnswers(userAnswers)

      val request =
        FakeRequest(POST, goodsLocationRoute)
          .withFormUrlEncodedBody(("value", GoodsLocation.BorderForceOffice.toString))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual s"/manage-transit-movements-arrivals/${emptyUserAnswers.movementReferenceNumber}/customs-approved-location"
    }

    "must redirect to the correct page for Authorised Consignees Location when valid data is submitted" in {

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val userAnswers = emptyUserAnswers.set(GoodsLocationPage, GoodsLocation.values.head).success.value
      setExistingUserAnswers(userAnswers)

      val request =
        FakeRequest(POST, goodsLocationRoute)
          .withFormUrlEncodedBody(("value", GoodsLocation.AuthorisedConsigneesLocation.toString))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual s"/manage-transit-movements-arrivals/${emptyUserAnswers.movementReferenceNumber}/authorised-location-code"
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      setExistingUserAnswers(emptyUserAnswers)

      val request        = FakeRequest(POST, goodsLocationRoute).withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm      = form.bind(Map("value" -> "invalid value"))
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "form"   -> boundForm,
        "mode"   -> NormalMode,
        "mrn"    -> mrn,
        "radios" -> GoodsLocation.radios(boundForm)
      )

      templateCaptor.getValue mustEqual "goodsLocation.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }
  }
}
