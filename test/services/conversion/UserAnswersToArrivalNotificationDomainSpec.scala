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

package services.conversion

import java.time.LocalTime

import base.SpecBase
import generators.MessagesModelGenerators
import models.UserAnswers
import models.domain._
import models.messages.{ArrivalMovementRequest, InterchangeControlReference, MessageSender}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class UserAnswersToArrivalNotificationDomainSpec extends SpecBase with ScalaCheckPropertyChecks with MessagesModelGenerators {

  private val service = injector.instanceOf[UserAnswersToArrivalNotificationDomain]

  "UserAnswersToArrivalNotificationDomain" - {

    "must convert an UnswerAnswers to ArrivalNotificationDomain" in {

      forAll(arbitrary[ArrivalNotificationDomain], arbitrary[MessageSender], arbitrary[InterchangeControlReference]) {
        (arrivalNotificationDomain, messageSender, interchangeControlReference) =>
          val arrivalMovementRequest: ArrivalMovementRequest = SubmissionModelService.convertToSubmissionModel(
            arrivalNotificationDomain,
            messageSender,
            interchangeControlReference,
            LocalTime.now()
          )

          val userAnswers: UserAnswers = ArrivalMovementRequestToUserAnswersService
            .convertToUserAnswers(
              arrivalMovementRequest,
              messageSender.eori,
              arrivalNotificationDomain.movementReferenceNumber
            )
            .value

          val result = service.convertToArrivalNotification(userAnswers).value

          val expectedResult = arrivalNotificationDomain match {
            case normalNotification: NormalNotification         => normalNotification.copy(notificationDate     = result.notificationDate)
            case simplifiedNotification: SimplifiedNotification => simplifiedNotification.copy(notificationDate = result.notificationDate)
          }

          result mustEqual expectedResult
      }
    }

    "must return 'None' from invalid UserAnswers" in {
      service.convertToArrivalNotification(emptyUserAnswers) mustBe None
    }
  }

}
