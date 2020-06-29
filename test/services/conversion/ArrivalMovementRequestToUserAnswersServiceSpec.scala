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

import base.SpecBase
import generators.MessagesModelGenerators
import models.{MovementReferenceNumber, UserAnswers}
import models.messages.{ArrivalMovementRequest, Header, NormalNotification, Trader}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class ArrivalMovementRequestToUserAnswersServiceSpec extends SpecBase with MessagesModelGenerators with ScalaCheckPropertyChecks {

  "when we can go from ArrivalMovementRequest to UserAnswers" in {

    forAll(arbitrary[ArrivalMovementRequest]) {
      arrivalMovementRequest =>
        val result = ArrivalMovementRequestToUserAnswersService.apply(arrivalMovementRequest)

        result must be(defined)
        result.value mustBe an[UserAnswers]
    }
  }

  "when we cannot go from ArrivalMovementRequest to NormalNotification we get a None" in {
    val failingArrivalMovementRequest = arbitrary[ArrivalMovementRequest].map {
      amr =>
        amr.copy(header = amr.header.copy(movementReferenceNumber = "asdf"))
    }

    forAll(failingArrivalMovementRequest) {
      arrivalMovementRequest =>
        val result = ArrivalMovementRequestToUserAnswersService.apply(arrivalMovementRequest)

        result must not be (defined)
    }
  }

}
