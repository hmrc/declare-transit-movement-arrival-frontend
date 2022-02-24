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

package controllers.events.seals

import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.events.seals.AddSealFormProvider
import matchers.JsonMatchers
import models.domain.SealDomain
import models.{Index, Mode, NormalMode}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import pages.events.seals.SealIdentityPage
import play.api.data.Form
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import uk.gov.hmrc.viewmodels.{NunjucksSupport, Radios}
import utils.AddSealHelper

import scala.concurrent.Future

class AddSealControllerSpec extends SpecBase with AppWithDefaultMockFixtures with NunjucksSupport with JsonMatchers {

  val formProvider        = new AddSealFormProvider()
  val form: Form[Boolean] = formProvider(true)
  val mode: Mode          = NormalMode

  lazy val addSealRoute: String = routes.AddSealController.onPageLoad(mrn, eventIndex, mode).url

  "AddSeal Controller" - {

    "must return OK and the correct view for a GET" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val ua = emptyUserAnswers.set(SealIdentityPage(eventIndex, sealIndex), sealDomain).success.value
      setExistingUserAnswers(ua)

      val request        = FakeRequest(GET, addSealRoute)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "form"           -> form,
        "mode"           -> mode,
        "mrn"            -> mrn,
        "pageTitle"      -> "addSeal.title.singular",
        "heading"        -> "addSeal.heading.singular",
        "seals"          -> Json.toJson(Seq(AddSealHelper.apply(ua, mode).sealRow(eventIndex, sealIndex).value)),
        "radios"         -> Radios.yesNo(form("value")),
        "allowMoreSeals" -> true,
        "onSubmitUrl"    -> routes.AddSealController.onSubmit(mrn, eventIndex, mode).url
      )

      templateCaptor.getValue mustEqual "events/seals/addSeal.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "must redirect to the next page when valid data is submitted" in {

      setExistingUserAnswers(emptyUserAnswers)

      val request =
        FakeRequest(POST, addSealRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must redirect to the next page when invalid data but we have the max containers" in {

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val userAnswers = emptyUserAnswers
        .set(SealIdentityPage(Index(0), Index(0)), SealDomain("12345"))
        .success
        .value
        .set(SealIdentityPage(Index(0), Index(1)), SealDomain("12345"))
        .success
        .value
        .set(SealIdentityPage(Index(0), Index(2)), SealDomain("12345"))
        .success
        .value

      setExistingUserAnswers(userAnswers)

      val request =
        FakeRequest(POST, addSealRoute)
          .withFormUrlEncodedBody(("value", ""))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))
      val ua = emptyUserAnswers.set(SealIdentityPage(eventIndex, sealIndex), sealDomain).success.value
      setExistingUserAnswers(ua)

      val request        = FakeRequest(POST, addSealRoute).withFormUrlEncodedBody(("value", ""))
      val boundForm      = form.bind(Map("value" -> ""))
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "form"           -> boundForm,
        "mode"           -> mode,
        "mrn"            -> mrn,
        "pageTitle"      -> "addSeal.title.singular",
        "heading"        -> "addSeal.heading.singular",
        "seals"          -> Json.toJson(Seq(AddSealHelper.apply(ua, mode).sealRow(eventIndex, sealIndex).value)),
        "radios"         -> Radios.yesNo(boundForm("value")),
        "allowMoreSeals" -> true,
        "onSubmitUrl"    -> routes.AddSealController.onSubmit(mrn, eventIndex, mode).url
      )

      templateCaptor.getValue mustEqual "events/seals/addSeal.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, addSealRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request =
        FakeRequest(POST, addSealRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }
  }
}
