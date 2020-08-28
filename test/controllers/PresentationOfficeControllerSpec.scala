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
import connectors.ReferenceDataConnector
import forms.PresentationOfficeFormProvider
import matchers.JsonMatchers
import models.reference.CustomsOffice
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.{ArgumentCaptor, Mockito}
import org.mockito.Matchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.{CustomsSubPlacePage, PresentationOfficePage}
import play.api.data.Form
import play.api.inject.bind
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Call, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import repositories.SessionRepository
import uk.gov.hmrc.viewmodels.NunjucksSupport

import scala.concurrent.Future

class PresentationOfficeControllerSpec extends SpecBase with MockitoSugar with NunjucksSupport with JsonMatchers {

  def onwardRoute: Call = Call("GET", "/foo")

  val formProvider              = new PresentationOfficeFormProvider()
  val customsOffices            = Seq(CustomsOffice("id", "name", Seq.empty, None), CustomsOffice("officeId", "someName", Seq.empty, None))
  val form: Form[CustomsOffice] = formProvider("sub place", customsOffices)

  lazy val presentationOfficeRoute: String = routes.PresentationOfficeController.onPageLoad(ref, NormalMode).url

  private val mockRefDataConnector: ReferenceDataConnector = mock[ReferenceDataConnector]
  val templateCaptor: ArgumentCaptor[String]               = ArgumentCaptor.forClass(classOf[String])
  val jsonCaptor: ArgumentCaptor[JsObject]                 = ArgumentCaptor.forClass(classOf[JsObject])

  override def beforeEach(): Unit = {
    super.beforeEach()
    Mockito.reset(mockRefDataConnector)
  }

  "PresentationOffice Controller" - {

    "must return OK and the correct view for a GET" in {
      verifyOnLoadPage(emptyUserAnswers.set(CustomsSubPlacePage, "sub place").success.value, form)
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val officeId   = "officeId"
      val officeName = "someName"
      val userAnswers = emptyUserAnswers
        .set(PresentationOfficePage, CustomsOffice(officeId, officeName, Seq.empty, None))
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

      when(mockRefDataConnector.getCustomsOffices()(any(), any())).thenReturn(Future.successful(customsOffices))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[ReferenceDataConnector].toInstance(mockRefDataConnector))
        .build()
      val request = FakeRequest(GET, presentationOfficeRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "must redirect to the next page when valid data is submitted" in {
      val mockSessionRepository = mock[SessionRepository]

      when(mockRefDataConnector.getCustomsOffices()(any(), any())).thenReturn(Future.successful(customsOffices))
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val userAnswers = emptyUserAnswers.set(CustomsSubPlacePage, "sub place").success.value
      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[ReferenceDataConnector].toInstance(mockRefDataConnector)
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
      verifyBadRequestOnSubmit("someOfficeId")
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      verifyBadRequestOnSubmit("")
    }

    "must redirect to session expired page when invalid data is submitted and user hasn't answered the customs sub-place page question" in {

      when(mockRefDataConnector.getCustomsOffices()(any(), any())).thenReturn(Future.successful(customsOffices))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[ReferenceDataConnector].toInstance(mockRefDataConnector))
        .build()
      val request = FakeRequest(POST, presentationOfficeRoute).withFormUrlEncodedBody(("value", ""))

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

  private def verifyBadRequestOnSubmit(formValue: String): Future[_] = {
    val customsOfficeJson = Seq(
      Json.obj("value" -> "", "text"         -> ""),
      Json.obj("value" -> "id", "text"       -> "name (id)", "selected" -> false),
      Json.obj("value" -> "officeId", "text" -> "someName (officeId)", "selected" -> false)
    )

    when(mockRenderer.render(any(), any())(any()))
      .thenReturn(Future.successful(Html("")))

    when(mockRefDataConnector.getCustomsOffices()(any(), any())).thenReturn(Future.successful(customsOffices))

    val userAnswers = emptyUserAnswers.set(CustomsSubPlacePage, "sub place").success.value
    val application =
      applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
          bind[ReferenceDataConnector].toInstance(mockRefDataConnector)
        )
        .build()
    val request   = FakeRequest(POST, presentationOfficeRoute).withFormUrlEncodedBody(("value", formValue))
    val boundForm = form.bind(Map("value" -> formValue))

    val result = route(application, request).value

    verifyStatusAndContent(customsOfficeJson, boundForm, result, BAD_REQUEST)

    application.stop()
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

    templateCaptor.getValue mustEqual "presentationOffice.njk"
    jsonCaptor.getValue must containJson(expectedJson)
  }

  private def verifyOnLoadPage(userAnswers: UserAnswers, form: Form[CustomsOffice], preSelectOfficeId: Boolean = false): Future[_] = {

    val expectedCustomsOfficeJson = Seq(
      Json.obj("value" -> "", "text"         -> ""),
      Json.obj("value" -> "id", "text"       -> "name (id)", "selected" -> false),
      Json.obj("value" -> "officeId", "text" -> "someName (officeId)", "selected" -> preSelectOfficeId)
    )

    when(mockRenderer.render(any(), any())(any()))
      .thenReturn(Future.successful(Html("")))
    when(mockRefDataConnector.getCustomsOffices()(any(), any())).thenReturn(Future.successful(customsOffices))

    val application = applicationBuilder(userAnswers = Some(userAnswers))
      .overrides {
        bind[ReferenceDataConnector].toInstance(mockRefDataConnector)
      }
      .build()
    val request = FakeRequest(GET, presentationOfficeRoute)

    val result = route(application, request).value

    verifyStatusAndContent(expectedCustomsOfficeJson, form, result, OK)

    application.stop()
  }
}
