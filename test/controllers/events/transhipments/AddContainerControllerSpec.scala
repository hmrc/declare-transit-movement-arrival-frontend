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

package controllers.events.transhipments

import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.events.transhipments.AddContainerFormProvider
import generators.MessagesModelGenerators
import matchers.JsonMatchers
import models.domain.ContainerDomain
import models.{Index, Mode, NormalMode}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import pages.events.transhipments.ContainerNumberPage
import play.api.data.Form
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import uk.gov.hmrc.viewmodels.{NunjucksSupport, Radios}
import utils.AddContainerHelper
import viewModels.sections.Section

import scala.concurrent.Future

class AddContainerControllerSpec extends SpecBase with AppWithDefaultMockFixtures with NunjucksSupport with JsonMatchers with MessagesModelGenerators {

  private val formProvider        = new AddContainerFormProvider()
  private val form: Form[Boolean] = formProvider(true)
  val mode: Mode                  = NormalMode

  private lazy val addContainerRoute: String = routes.AddContainerController.onPageLoad(mrn, eventIndex, mode).url
  private lazy val addContainerTemplate      = "events/transhipments/addContainer.njk"

  "AddContainer Controller" - {

    "must return OK and the correct view for a GET" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val containerNumber = arbitrary[ContainerDomain].sample.value
      val ua              = emptyUserAnswers.set(ContainerNumberPage(eventIndex, containerIndex), containerNumber).success.value
      setExistingUserAnswers(ua)

      val request        = FakeRequest(GET, addContainerRoute)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "form"                -> form,
        "mode"                -> mode,
        "mrn"                 -> mrn,
        "radios"              -> Radios.yesNo(form("value")),
        "containers"          -> Section(Seq(AddContainerHelper(ua, mode).containerRow(eventIndex, containerIndex).value)),
        "allowMoreContainers" -> true,
        "onSubmitUrl"         -> routes.AddContainerController.onSubmit(mrn, eventIndex, mode).url
      )

      templateCaptor.getValue mustEqual addContainerTemplate
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      setExistingUserAnswers(emptyUserAnswers)

      val request =
        FakeRequest(POST, addContainerRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must redirect to the next page when invalid data but we have the max containers" in {

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val userAnswers = emptyUserAnswers
        .set(ContainerNumberPage(Index(0), Index(0)), ContainerDomain("12345"))
        .success
        .value
        .set(ContainerNumberPage(Index(0), Index(1)), ContainerDomain("12345"))
        .success
        .value
        .set(ContainerNumberPage(Index(0), Index(2)), ContainerDomain("12345"))
        .success
        .value

      setExistingUserAnswers(userAnswers)

      val request =
        FakeRequest(POST, addContainerRoute)
          .withFormUrlEncodedBody(("value", ""))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      setExistingUserAnswers(emptyUserAnswers)

      val request        = FakeRequest(POST, addContainerRoute).withFormUrlEncodedBody(("value", ""))
      val boundForm      = form.bind(Map("value" -> ""))
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "form"                -> boundForm,
        "mode"                -> mode,
        "mrn"                 -> mrn,
        "radios"              -> Radios.yesNo(boundForm("value")),
        "allowMoreContainers" -> true,
        "onSubmitUrl"         -> routes.AddContainerController.onSubmit(mrn, eventIndex, mode).url
      )

      templateCaptor.getValue mustEqual addContainerTemplate
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, addContainerRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request =
        FakeRequest(POST, addContainerRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }
  }
}
