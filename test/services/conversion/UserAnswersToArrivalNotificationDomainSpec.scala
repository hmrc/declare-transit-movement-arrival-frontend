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

package services.conversion

import base.{AppWithDefaultMockFixtures, SpecBase}
import generators.MessagesModelGenerators
import models.domain._
import models.messages.{ArrivalMovementRequest, InterchangeControlReference}
import models.{EoriNumber, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import java.time.LocalTime

class UserAnswersToArrivalNotificationDomainSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with MessagesModelGenerators {

  "UserAnswersToArrivalNotificationDomain" - {

    "must convert an UserAnswers to ArrivalNotificationDomain" in {

      forAll(arbitrary[ArrivalNotificationDomain], arbitrary[EoriNumber], arbitrary[InterchangeControlReference]) {
        (arrivalNotificationDomain, eori, interchangeControlReference) =>
          val arrivalMovementRequest: ArrivalMovementRequest =
            ArrivalNotificationDomainToArrivalMovementRequestService
              .convertToSubmissionModel(
                arrivalNotificationDomain,
                interchangeControlReference,
                LocalTime.now()
              )

          val userAnswers: UserAnswers = ArrivalMovementRequestToUserAnswersService
            .convertToUserAnswers(
              arrivalMovementRequest,
              eori,
              arrivalNotificationDomain.movementReferenceNumber,
              arrivalNotificationDomain.customsOffice
            )
            .value

          val service = app.injector.instanceOf[UserAnswersToArrivalNotificationDomain]

          val result = service.convertToArrivalNotification(userAnswers).value

          val expectedResult = arrivalNotificationDomain match {
            case normalNotification: NormalNotification => normalNotification.copy(notificationDate = result.notificationDate)
            case simplifiedNotification: SimplifiedNotification =>
              simplifiedNotification.copy(notificationDate = result.notificationDate,
                                          notificationPlace = result.trader.postCode,
                                          authedEori = userAnswers.eoriNumber
              )
          }

          result mustEqual expectedResult
      }
    }

    "must return 'None' from invalid UserAnswers" in {
      val service = app.injector.instanceOf[UserAnswersToArrivalNotificationDomain]
      service.convertToArrivalNotification(emptyUserAnswers) mustBe None
    }
  }
}
