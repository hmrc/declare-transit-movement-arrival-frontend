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

import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.MovementReferenceNumberFormProvider
import generators.MessagesModelGenerators
import matchers.JsonMatchers
import models.messages.ArrivalMovementRequest
import models.{ArrivalId, MovementReferenceNumber}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito._
import org.scalacheck.Arbitrary.arbitrary
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import services.{ArrivalNotificationMessageService, UserAnswersService}
import uk.gov.hmrc.viewmodels.NunjucksSupport
import viewModels.sections.ViewModelConfig

import scala.concurrent.Future

class UpdateRejectedMRNControllerSpec extends SpecBase with AppWithDefaultMockFixtures with MessagesModelGenerators with NunjucksSupport with JsonMatchers {

  private val formProvider                      = new MovementReferenceNumberFormProvider()
  private val form                              = formProvider()
  private val mockArrivalMovementMessageService = mock[ArrivalNotificationMessageService]
  private val mockUserAnswersService            = mock[UserAnswersService]
  private val arrivalId                         = ArrivalId(1)
  private val viewConfig: ViewModelConfig       = app.injector.instanceOf[ViewModelConfig]
  private lazy val movementReferenceNumberRoute = routes.UpdateRejectedMRNController.onPageLoad(arrivalId).url

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockArrivalMovementMessageService)
    reset(mockUserAnswersService)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[ArrivalNotificationMessageService].toInstance(mockArrivalMovementMessageService),
        bind[UserAnswersService].toInstance(mockUserAnswersService)
      )

  "MovementReferenceNumber Controller" - {

    "must return OK and the correct view with pre-populated MRN for a GET" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))
      val arrivalMovementRequest: ArrivalMovementRequest = arbitrary[ArrivalMovementRequest].sample.value
      when(mockArrivalMovementMessageService.getArrivalNotificationMessage(any())(any(), any()))
        .thenReturn(Future.successful(Some(arrivalMovementRequest)))

      setNoExistingUserAnswers()

      val request        = FakeRequest(GET, movementReferenceNumberRoute)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val filledForm = form.bind(Map("value" -> arrivalMovementRequest.header.movementReferenceNumber))

      val expectedJson = Json.obj(
        "form"      -> filledForm,
        "arrivalId" -> arrivalId.value
      )

      templateCaptor.getValue mustEqual "updateMovementReferenceNumber.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "must render to TechnicalDifficulties page when getArrivalNotification returns None" in {
      when(mockRenderer.render(any(), any())(any())).thenReturn(Future.successful(Html("")))
      when(mockArrivalMovementMessageService.getArrivalNotificationMessage(any())(any(), any()))
        .thenReturn(Future.successful(None))

      setNoExistingUserAnswers()
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val request = FakeRequest(GET, movementReferenceNumberRoute)

      val result = route(app, request).value

      status(result) mustEqual INTERNAL_SERVER_ERROR
      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "contactUrl" -> viewConfig.nctsEnquiriesUrl
      )

      templateCaptor.getValue mustEqual "technicalDifficulties.njk"
      jsonCaptor.getValue must containJson(expectedJson)

    }

    "must redirect to the next page when valid data is submitted" in {

      val mrn = "99IT9876AB88901209"

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockUserAnswersService.getUserAnswers(any(), any())(any())) thenReturn Future.successful(Some(emptyUserAnswers))

      setNoExistingUserAnswers()

      val request =
        FakeRequest(POST, movementReferenceNumberRoute)
          .withFormUrlEncodedBody(("value", mrn))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual onwardRoute.url
      verify(mockUserAnswersService, times(1)).getUserAnswers(any(), any())(any())
      verify(mockSessionRepository, times(1)).set(meq(emptyUserAnswers.copy(MovementReferenceNumber(mrn).get, arrivalId = Some(arrivalId))))
    }

    "must render to TechnicalDifficulties page when UserAnswersService return 'none'" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockUserAnswersService.getUserAnswers(any(), any())(any())) thenReturn Future.successful(None)

      setNoExistingUserAnswers()
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val request =
        FakeRequest(POST, movementReferenceNumberRoute)
          .withFormUrlEncodedBody(("value", "99IT9876AB88901209"))

      val result = route(app, request).value

      status(result) mustEqual INTERNAL_SERVER_ERROR
      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "contactUrl" -> viewConfig.nctsEnquiriesUrl
      )

      templateCaptor.getValue mustEqual "technicalDifficulties.njk"
      jsonCaptor.getValue must containJson(expectedJson)
      verify(mockUserAnswersService, times(1)).getUserAnswers(any(), any())(any())
      verify(mockSessionRepository, never()).set(any())
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      setExistingUserAnswers(emptyUserAnswers)

      val request        = FakeRequest(POST, movementReferenceNumberRoute).withFormUrlEncodedBody(("value", ""))
      val boundForm      = form.bind(Map("value" -> ""))
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "form"      -> boundForm,
        "arrivalId" -> arrivalId.value
      )

      templateCaptor.getValue mustEqual "updateMovementReferenceNumber.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }
  }
}
