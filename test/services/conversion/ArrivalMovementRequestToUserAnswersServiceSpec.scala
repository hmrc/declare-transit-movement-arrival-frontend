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
import models.UserAnswers
import models.domain.{ArrivalNotificationDomain, NormalNotification}
import models.messages.{ArrivalMovementRequest, Header}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ArrivalMovementRequestToUserAnswersServiceSpec extends SpecBase with MessagesModelGenerators with ScalaCheckPropertyChecks {

  private val arrivalMovementRequestToUserAnswersService = ArrivalMovementRequestToUserAnswersService

  "when we can go from ArrivalMovementRequest to UserAnswers" in {
    val sampleMovementRequest = arbitrary[ArrivalMovementRequest].sample.value

    val result = arrivalMovementRequestToUserAnswersService.apply(sampleMovementRequest)

    result.value mustBe an[UserAnswers]
  }

  "return None when ArrivalMovementRequest cannot be converted to ArrivalNotification" in {

    val arrivalMovementRequest: ArrivalMovementRequest                 = arbitrary[ArrivalMovementRequest].sample.value
    val header: Header                                                 = arrivalMovementRequest.header.copy(movementReferenceNumber = "Invalid MRN")
    val arrivalMovementRequestWithMalformedMrn: ArrivalMovementRequest = arrivalMovementRequest.copy(header = header)

    val result = arrivalMovementRequestToUserAnswersService.apply(arrivalMovementRequestWithMalformedMrn)

    result must not be defined
  }
}
