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

package services

import base.SpecBase
import generators.MessagesModelGenerators
import models.messages.ArrivalMovementRequest
import models.{ArrivalId, UserAnswers}
import org.mockito.Matchers.any
import org.mockito.Mockito._
import org.scalacheck.Arbitrary.arbitrary
import play.api.inject.bind
import services.conversion.ArrivalMovementRequestToUserAnswersService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserAnswersServiceSpec extends SpecBase with MessagesModelGenerators {

  val mockArrivalNotificationMessageService          = mock[ArrivalNotificationMessageService]
  val mockArrivalMovementRequestToUserAnswersService = mock[ArrivalMovementRequestToUserAnswersService]

  override def beforeEach: Unit = {
    super.beforeEach()
    reset(mockArrivalNotificationMessageService)
    reset(mockArrivalMovementRequestToUserAnswersService)
  }

  "UserAnswers" - {
    "must return user answers for valid input" in {

      val arrivalMovementRequest = arbitrary[ArrivalMovementRequest].sample.value

      when(mockArrivalNotificationMessageService.getArrivalNotificationMessage(any())(any(), any()))
        .thenReturn(Future.successful(Some(arrivalMovementRequest)))

      when(mockArrivalMovementRequestToUserAnswersService.apply(any())(any(), any()))
        .thenReturn(Future.successful(Some(emptyUserAnswers)))

      val application = applicationBuilder(Some(emptyUserAnswers))
        .overrides(bind[ArrivalNotificationMessageService].toInstance(mockArrivalNotificationMessageService))
        .overrides(bind[ArrivalMovementRequestToUserAnswersService].toInstance(mockArrivalMovementRequestToUserAnswersService))
        .build()

      val userAnswersService = application.injector.instanceOf[UserAnswersService]

      userAnswersService.getUserAnswers(ArrivalId(1)).futureValue.value mustBe a[UserAnswers]
    }

    "must return None for invalid request" in {
      when(mockArrivalNotificationMessageService.getArrivalNotificationMessage(any())(any(), any()))
        .thenReturn(Future.successful(None))

      val application = applicationBuilder(Some(emptyUserAnswers))
        .overrides(bind[ArrivalNotificationMessageService].toInstance(mockArrivalNotificationMessageService))
        .build()
      val userAnswersService = application.injector.instanceOf[UserAnswersService]
      userAnswersService.getUserAnswers(ArrivalId(1)).futureValue mustBe None
    }
  }
}
