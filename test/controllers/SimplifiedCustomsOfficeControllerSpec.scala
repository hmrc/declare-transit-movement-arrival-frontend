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
import forms.SimplifiedCustomsOfficeFormProvider
import matchers.JsonMatchers
import models.reference.CustomsOffice
import models.{CustomsOfficeList, NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.mockito.{ArgumentCaptor, Mockito}
import pages.{ConsigneeNamePage, CustomsOfficePage, CustomsSubPlacePage}
import play.api.data.Form
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import services.CustomsOfficesService
import uk.gov.hmrc.viewmodels.NunjucksSupport

import scala.concurrent.Future

class SimplifiedCustomsOfficeControllerSpec extends SpecBase with AppWithDefaultMockFixtures with NunjucksSupport with JsonMatchers {

  val formProvider                                             = new SimplifiedCustomsOfficeFormProvider()
  val customsOffices                                           = CustomsOfficeList(Seq(CustomsOffice("id", Some("name"), None), CustomsOffice("officeId", Some("someName"), None)))
  val form: Form[CustomsOffice]                                = formProvider(consigneeName, customsOffices)
  val country: String                                          = "GB"
  private val mockCustomsOfficesService: CustomsOfficesService = mock[CustomsOfficesService]

  lazy val simplifiedCustomsOfficeRoute: String = routes.SimplifiedCustomsOfficeController.onPageLoad(mrn, NormalMode).url

  val templateCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
  val jsonCaptor: ArgumentCaptor[JsObject]   = ArgumentCaptor.forClass(classOf[JsObject])

  override def beforeEach(): Unit = {
    super.beforeEach()
    Mockito.reset(mockCustomsOfficesService)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[CustomsOfficesService].toInstance(mockCustomsOfficesService))

  "SimplifiedCustomsOffice Controller" - {

    "must return OK and the correct view for a GET" in {
      verifyOnLoadPage(emptyUserAnswers.set(CustomsSubPlacePage, "sub place").success.value, form)
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val officeId   = "officeId"
      val officeName = "someName"
      val userAnswers = emptyUserAnswers
        .set(CustomsOfficePage, CustomsOffice(officeId, Some(officeName), None))
        .success
        .value
        .set(CustomsSubPlacePage, "subs place")
        .success
        .value

      val filledForm = form.bind(Map("value" -> officeId))

      verifyOnLoadPage(userAnswers, filledForm, preSelectOfficeId = true)
    }

    "must redirect to session expired page when user hasn't answered the customs sub place question" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      when(mockCustomsOfficesService.getCustomsOfficesOfArrival(any())).thenReturn(Future.successful(customsOffices))

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, simplifiedCustomsOfficeRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockCustomsOfficesService.getCustomsOfficesOfArrival(any())).thenReturn(Future.successful(customsOffices))
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val userAnswers = emptyUserAnswers.set(CustomsSubPlacePage, "sub place").success.value
      setExistingUserAnswers(userAnswers)

      val request =
        FakeRequest(POST, simplifiedCustomsOfficeRoute)
          .withFormUrlEncodedBody(("value", "id"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual onwardRoute.url
      verify(mockCustomsOfficesService, times(1)).getCustomsOfficesOfArrival(any())
    }

    "must return Bad Request and error when user entered data does not exist in reference data customs office list" in {
      verifyBadRequestOnSubmit("someOfficeId")
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      verifyBadRequestOnSubmit("")
    }

    "must redirect to session expired page when invalid data is submitted and user hasn't answered the customs sub-place page question" in {

      when(mockCustomsOfficesService.getCustomsOfficesOfArrival(any())).thenReturn(Future.successful(customsOffices))

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(POST, simplifiedCustomsOfficeRoute).withFormUrlEncodedBody(("value", ""))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
      verify(mockCustomsOfficesService, times(1)).getCustomsOfficesOfArrival(any())
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, simplifiedCustomsOfficeRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request =
        FakeRequest(POST, simplifiedCustomsOfficeRoute)
          .withFormUrlEncodedBody(("value", "answer"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }
  }

  private def verifyBadRequestOnSubmit(formValue: String) = {
    val customsOfficeJson = Seq(
      Json.obj("value" -> "", "text"         -> "Select a customs office"),
      Json.obj("value" -> "id", "text"       -> "name (id)", "selected"           -> false),
      Json.obj("value" -> "officeId", "text" -> "someName (officeId)", "selected" -> false)
    )

    when(mockRenderer.render(any(), any())(any())).thenReturn(Future.successful(Html("")))
    when(mockCustomsOfficesService.getCustomsOfficesOfArrival(any())).thenReturn(Future.successful(customsOffices))

    val userAnswers = emptyUserAnswers
      .set(ConsigneeNamePage, consigneeName)
      .success
      .value

    setExistingUserAnswers(userAnswers)

    val request   = FakeRequest(POST, simplifiedCustomsOfficeRoute).withFormUrlEncodedBody(("value", formValue))
    val boundForm = form.bind(Map("value" -> formValue))

    val result = route(app, request).value

    status(result) mustEqual BAD_REQUEST

    verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())
    verify(mockCustomsOfficesService, times(1)).getCustomsOfficesOfArrival(any())

    val expectedJson = Json.obj(
      "form"           -> boundForm,
      "mrn"            -> mrn,
      "mode"           -> NormalMode,
      "customsOffices" -> customsOfficeJson,
      "consigneeName"  -> consigneeName
    )

    templateCaptor.getValue mustEqual "customsOfficeSimplified.njk"
    jsonCaptor.getValue must containJson(expectedJson)
  }

  private def verifyStatusAndContent(customsOfficeJson: Seq[JsObject], boundForm: Form[CustomsOffice], result: Future[Result], expectedStatus: Int): Any = {
    status(result) mustEqual expectedStatus

    verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

    val expectedJson = Json.obj(
      "form"           -> boundForm,
      "mrn"            -> mrn,
      "mode"           -> NormalMode,
      "customsOffices" -> customsOfficeJson
    )

    templateCaptor.getValue mustEqual "customsOfficeSimplified.njk"
    jsonCaptor.getValue must containJson(expectedJson)
  }

  private def verifyOnLoadPage(userAnswers: UserAnswers, form: Form[CustomsOffice], preSelectOfficeId: Boolean = false) = {

    val expectedCustomsOfficeJson = Seq(
      Json.obj("value" -> "", "text"         -> "Select a customs office"),
      Json.obj("value" -> "id", "text"       -> "name (id)", "selected"           -> false),
      Json.obj("value" -> "officeId", "text" -> "someName (officeId)", "selected" -> preSelectOfficeId)
    )

    when(mockRenderer.render(any(), any())(any())).thenReturn(Future.successful(Html("")))
    when(mockCustomsOfficesService.getCustomsOfficesOfArrival(any())).thenReturn(Future.successful(customsOffices))

    setExistingUserAnswers(userAnswers)

    val request = FakeRequest(GET, simplifiedCustomsOfficeRoute)

    val result = route(app, request).value

    verifyStatusAndContent(expectedCustomsOfficeJson, form, result, OK)
  }
}
