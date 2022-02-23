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
import forms.events.seals.ConfirmRemoveSealFormProvider
import matchers.JsonMatchers
import models.{Index, NormalMode, UserAnswers}
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

import scala.concurrent.Future

class ConfirmRemoveSealControllerSpec extends SpecBase with AppWithDefaultMockFixtures with NunjucksSupport with JsonMatchers {

  private val formProvider        = new ConfirmRemoveSealFormProvider()
  private val form: Form[Boolean] = formProvider(sealDomain)

  private lazy val removeSealRoute: String = routes.ConfirmRemoveSealController.onPageLoad(mrn, eventIndex, sealIndex, NormalMode).url
  private val userAnswersWithSeal          = emptyUserAnswers.set(SealIdentityPage(eventIndex, sealIndex), sealDomain).success.value
  private val confirmRemoveSealTemplate    = "events/seals/confirmRemoveSeal.njk"

  "ConfirmRemoveSealController" - {

    "must return OK and the correct view for a GET" in {

      setExistingUserAnswers(userAnswersWithSeal)

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val request        = FakeRequest(GET, removeSealRoute)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "form"        -> form,
        "mode"        -> NormalMode,
        "mrn"         -> mrn,
        "sealNumber"  -> seal.numberOrMark,
        "radios"      -> Radios.yesNo(form("value")),
        "onSubmitUrl" -> routes.ConfirmRemoveSealController.onSubmit(mrn, eventIndex, sealIndex, NormalMode).url
      )

      templateCaptor.getValue mustEqual confirmRemoveSealTemplate
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "must return error page when user tries to remove a seal that does not exists" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))
      val updatedAnswer = userAnswersWithSeal.remove(SealIdentityPage(eventIndex, sealIndex)).success.value
      setExistingUserAnswers(updatedAnswer)

      val request        = FakeRequest(GET, removeSealRoute)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual NOT_FOUND

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "pageTitle"    -> msg"concurrent.remove.error.title".withArgs("seal"),
        "pageHeading"  -> msg"concurrent.remove.error.heading".withArgs("seal"),
        "linkText"     -> msg"concurrent.remove.error.noSeal.link.text",
        "redirectLink" -> onwardRoute.url
      )

      templateCaptor.getValue mustEqual "concurrentRemoveError.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "must return error page when there are multiple seals and user tries to remove the last seal that is already removed" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))
      val updatedAnswer = userAnswersWithSeal
        .set(SealIdentityPage(eventIndex, Index(1)), sealDomain)
        .success
        .value
        .set(SealIdentityPage(eventIndex, Index(2)), sealDomain)
        .success
        .value
        .remove(SealIdentityPage(eventIndex, Index(2)))
        .success
        .value

      val sealRoute: String = routes.ConfirmRemoveSealController.onPageLoad(mrn, eventIndex, Index(2), NormalMode).url

      setExistingUserAnswers(updatedAnswer)

      val request        = FakeRequest(GET, sealRoute)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual NOT_FOUND

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "pageTitle"    -> msg"concurrent.remove.error.title".withArgs("seal"),
        "pageHeading"  -> msg"concurrent.remove.error.heading".withArgs("seal"),
        "linkText"     -> msg"concurrent.remove.error.multipleSeal.link.text",
        "redirectLink" -> onwardRoute.url
      )

      templateCaptor.getValue mustEqual "concurrentRemoveError.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "must redirect to the next page when valid data is submitted and seal is removed" in {

      setExistingUserAnswers(userAnswersWithSeal)

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val request =
        FakeRequest(POST, removeSealRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url

      val newUserAnswers = UserAnswers(
        movementReferenceNumber = userAnswersWithSeal.movementReferenceNumber,
        eoriNumber = userAnswersWithSeal.eoriNumber,
        userAnswersWithSeal.remove(SealIdentityPage(eventIndex, sealIndex)).success.value.data,
        userAnswersWithSeal.lastUpdated,
        id = userAnswersWithSeal.id
      )

      verify(mockSessionRepository, times(1)).set(newUserAnswers)
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      setExistingUserAnswers(userAnswersWithSeal)

      val request        = FakeRequest(POST, removeSealRoute).withFormUrlEncodedBody(("value", ""))
      val boundForm      = form.bind(Map("value" -> ""))
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "form"        -> boundForm,
        "mode"        -> NormalMode,
        "mrn"         -> mrn,
        "sealNumber"  -> seal.numberOrMark,
        "radios"      -> Radios.yesNo(boundForm("value")),
        "onSubmitUrl" -> routes.ConfirmRemoveSealController.onSubmit(mrn, eventIndex, sealIndex, NormalMode).url
      )

      templateCaptor.getValue mustEqual confirmRemoveSealTemplate
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, removeSealRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request =
        FakeRequest(POST, removeSealRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
    }
  }
}
