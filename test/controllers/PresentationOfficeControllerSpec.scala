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
import forms.PresentationOfficeFormProvider
import matchers.JsonMatchers
import models.{CustomsOffice, NormalMode, UserAnswers}
import navigation.FakeNavigator
import navigation.Navigator
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.any
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.CustomsSubPlacePage
import pages.PresentationOfficePage
import play.api.data.Form
import play.api.inject.bind
import play.api.libs.json.JsObject
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import repositories.SessionRepository
import services.ReferenceDataService
import uk.gov.hmrc.viewmodels.NunjucksSupport

import scala.concurrent.Future

class PresentationOfficeControllerSpec extends SpecBase with MockitoSugar with NunjucksSupport with JsonMatchers {

  def onwardRoute: Call = Call("GET", "/foo")

  val formProvider       = new PresentationOfficeFormProvider()
  val form: Form[String] = formProvider("sub place")

  lazy val presentationOfficeRoute: String = routes.PresentationOfficeController.onPageLoad(mrn, NormalMode).url
  val mockRefDataService                   = mock[ReferenceDataService]

  "PresentationOffice Controller" - {

    "must return OK and the correct view for a GET" in {
      val expectedCustomsOfficeJson = Json.obj("value" -> "id", "text" -> "name (id)", "selected" -> false)
      verifyOnLoadPage(emptyUserAnswers.set(CustomsSubPlacePage, "sub place").success.value, form, expectedCustomsOfficeJson)
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val officeId   = "officeId"
      val officeName = "officeId"
      val userAnswers = UserAnswers(mrn)
        .set(PresentationOfficePage, CustomsOffice(officeId, officeName, Seq.empty))
        .success
        .value
        .set(CustomsSubPlacePage, "subs place")
        .success
        .value

      val filledForm        = form.bind(Map("value" -> officeId))
      val customsOfficeJson = Json.obj("value" -> officeId, "text" -> s"$officeName ($officeId)", "selected" -> true)

      verifyOnLoadPage(userAnswers, filledForm, customsOfficeJson)
    }

    "must redirect to session expired page when user hasn't answered the customs sub place question" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      val request     = FakeRequest(GET, presentationOfficeRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "must redirect to the next page when valid data is submitted" in {
      val mockSessionRepository = mock[SessionRepository]

      when(mockRefDataService.getCustomsOffice(any())(any()))
        .thenReturn(Future.successful(Some(CustomsOffice("id", "name", Seq.empty))))

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val userAnswers = emptyUserAnswers.set(CustomsSubPlacePage, "sub place").success.value
      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[ReferenceDataService].toInstance(mockRefDataService)
          )
          .build()

      val request =
        FakeRequest(POST, presentationOfficeRoute)
          .withFormUrlEncodedBody(("value", "id"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual onwardRoute.url

      application.stop()
    }

    "must return Bad Request and error when user entered data does not exist in reference data customs office list" in {
      val jsCustomsOffice = Json.obj("value" -> "id", "text" -> "name (id)", "selected" -> false)

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      when(mockRefDataService.getCustomsOffice(any())(any()))
        .thenReturn(Future.successful(None))

      when(mockRefDataService.getCustomsOfficesAsJson(any())(any()))
        .thenReturn(Future.successful(Seq(jsCustomsOffice)))

      val userAnswers = emptyUserAnswers.set(CustomsSubPlacePage, "sub place").success.value
      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[ReferenceDataService].toInstance(mockRefDataService)
          )
          .build()

      val request =
        FakeRequest(POST, presentationOfficeRoute)
          .withFormUrlEncodedBody(("value", "abcd"))

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      application.stop()

    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))
      val userAnswers    = emptyUserAnswers.set(CustomsSubPlacePage, "sub place").success.value
      val application    = applicationBuilder(userAnswers = Some(userAnswers)).build()
      val request        = FakeRequest(POST, presentationOfficeRoute).withFormUrlEncodedBody(("value", ""))
      val boundForm      = form.bind(Map("value" -> ""))
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "form" -> boundForm,
        "mrn"  -> mrn,
        "mode" -> NormalMode
      )

      templateCaptor.getValue mustEqual "presentationOffice.njk"
      jsonCaptor.getValue must containJson(expectedJson)

      application.stop()
    }

    "must redirect to session expired page when invalid data is submitted and user hasn't answered the customs sub-place page question" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      val request     = FakeRequest(POST, presentationOfficeRoute).withFormUrlEncodedBody(("value", ""))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, presentationOfficeRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, presentationOfficeRoute)
          .withFormUrlEncodedBody(("value", "answer"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }
  }

  private def verifyOnLoadPage(userAnswers: UserAnswers, form: Form[String], customsOfficeJson: JsObject): Future[_] = {
    when(mockRenderer.render(any(), any())(any()))
      .thenReturn(Future.successful(Html("")))

    when(mockRefDataService.getCustomsOfficesAsJson(any())(any()))
      .thenReturn(Future.successful(Seq(customsOfficeJson)))

    val application = applicationBuilder(userAnswers = Some(userAnswers))
      .overrides {
        bind[ReferenceDataService].toInstance(mockRefDataService)
      }
      .build()
    val request        = FakeRequest(GET, presentationOfficeRoute)
    val templateCaptor = ArgumentCaptor.forClass(classOf[String])
    val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

    val result = route(application, request).value

    status(result) mustEqual OK

    verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

    val expectedJson = Json.obj(
      "form"           -> form,
      "mrn"            -> mrn,
      "mode"           -> NormalMode,
      "customsOffices" -> Json.arr(customsOfficeJson)
    )

    templateCaptor.getValue mustEqual "presentationOffice.njk"
    jsonCaptor.getValue must containJson(expectedJson)

    application.stop()
  }
}
