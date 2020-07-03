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

import base.SpecBase
import forms.MovementReferenceNumberFormProvider
import generators.MessagesModelGenerators
import matchers.JsonMatchers
import models.{ArrivalId, MovementReferenceNumber}
import models.messages.ArrivalMovementRequest
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.{any, eq => meq}
import org.mockito.Mockito._
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import repositories.SessionRepository
import services.{ArrivalNotificationMessageService, UserAnswersService}
import uk.gov.hmrc.viewmodels.NunjucksSupport

import scala.concurrent.Future

class UpdateRejectedMRNControllerSpec extends SpecBase with MessagesModelGenerators with MockitoSugar with NunjucksSupport with JsonMatchers {

  def onwardRoute = Call("GET", "/foo")

  val formProvider                              = new MovementReferenceNumberFormProvider()
  val form                                      = formProvider()
  private val mockArrivalMovementMessageService = mock[ArrivalNotificationMessageService]
  private val arrivalId                         = ArrivalId(1)

  lazy val movementReferenceNumberRoute = routes.UpdateRejectedMRNController.onPageLoad(arrivalId).url

  override def beforeEach: Unit = {
    super.beforeEach()
    reset(mockArrivalMovementMessageService)
  }

  "MovementReferenceNumber Controller" - {

    "must return OK and the correct view with pre-populated MRN for a GET" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))
      val arrivalMovementRequest: ArrivalMovementRequest = arbitrary[ArrivalMovementRequest].sample.value
      when(mockArrivalMovementMessageService.getArrivalNotificationMessage(any())(any(), any()))
        .thenReturn(Future.successful(Some(arrivalMovementRequest)))

      val application =
        applicationBuilder(userAnswers = None)
          .overrides(
            bind[ArrivalNotificationMessageService].toInstance(mockArrivalMovementMessageService)
          )
          .build()
      val request        = FakeRequest(GET, movementReferenceNumberRoute)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(application, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val filledForm = form.bind(Map("value" -> arrivalMovementRequest.header.movementReferenceNumber))

      val expectedJson = Json.obj(
        "form"      -> filledForm,
        "arrivalId" -> arrivalId.value
      )

      templateCaptor.getValue mustEqual "updateMovementReferenceNumber.njk"
      jsonCaptor.getValue must containJson(expectedJson)

      application.stop()
    }

    "must redirect to TechnicalDifficulties page when getArrivalNotification returns None" in {

      when(mockArrivalMovementMessageService.getArrivalNotificationMessage(any())(any(), any()))
        .thenReturn(Future.successful(None))

      val application =
        applicationBuilder(userAnswers = None)
          .overrides(
            bind[ArrivalNotificationMessageService].toInstance(mockArrivalMovementMessageService)
          )
          .build()
      val request = FakeRequest(GET, movementReferenceNumberRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      application.stop()
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository  = mock[SessionRepository]
      val mockUserAnswersService = mock[UserAnswersService]
      val mrn                    = "99IT9876AB88901209"

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockUserAnswersService.getUserAnswers(any(), any())(any(), any())) thenReturn Future.successful(Some(emptyUserAnswers))

      val application =
        applicationBuilder(userAnswers = None)
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[UserAnswersService].toInstance(mockUserAnswersService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      val request =
        FakeRequest(POST, movementReferenceNumberRoute)
          .withFormUrlEncodedBody(("value", mrn))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual onwardRoute.url
      verify(mockUserAnswersService, times(1)).getUserAnswers(any(), any())(any(), any())
      verify(mockSessionRepository, times(1)).set(meq(emptyUserAnswers.copy(id = MovementReferenceNumber(mrn).get, arrivalId = Some(arrivalId))))

      application.stop()
    }

    "must redirect to to TechnicalDifficulties page when UserAnswersService return 'none'" in {

      val mockSessionRepository  = mock[SessionRepository]
      val mockUserAnswersService = mock[UserAnswersService]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockUserAnswersService.getUserAnswers(any(), any())(any(), any())) thenReturn Future.successful(None)

      val application =
        applicationBuilder(userAnswers = None)
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[UserAnswersService].toInstance(mockUserAnswersService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      val request =
        FakeRequest(POST, movementReferenceNumberRoute)
          .withFormUrlEncodedBody(("value", "99IT9876AB88901209"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.TechnicalDifficultiesController.onPageLoad().url

      verify(mockUserAnswersService, times(1)).getUserAnswers(any(), any())(any(), any())
      verify(mockSessionRepository, never()).set(any())

      application.stop()
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val application    = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      val request        = FakeRequest(POST, movementReferenceNumberRoute).withFormUrlEncodedBody(("value", ""))
      val boundForm      = form.bind(Map("value" -> ""))
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "form" -> boundForm
      )

      templateCaptor.getValue mustEqual "updateMovementReferenceNumber.njk"
      jsonCaptor.getValue must containJson(expectedJson)

      application.stop()
    }
  }
}
