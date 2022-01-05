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

import base.SpecBase
import generators.MessagesModelGenerators
import models.EoriNumber
import models.domain.{ArrivalNotificationDomain, SimplifiedNotification}
import models.messages.{ArrivalMovementRequest, Header, InterchangeControlReference}
import models.reference.CustomsOffice
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import java.time.LocalTime

class ArrivalMovementRequestToArrivalNotificationServiceSpec extends SpecBase with MessagesModelGenerators with ScalaCheckPropertyChecks {

  private val arrivalMovementRequestConversionService = ArrivalMovementRequestToArrivalNotificationService

  "convertToArrivalNotification" - {

    "must convert ArrivalMovementRequest to NormalNotification" in {

      forAll(
        arbitrary[ArrivalNotificationDomain],
        arbitrary[InterchangeControlReference],
        arbitrary[LocalTime],
        arbitrary[EoriNumber]
      ) {
        (arrivalNotificationDomain, interchangeControlReference, timeOfPresentation, eoriNumber) =>
          val arrivalMovementRequest = ArrivalNotificationDomainToArrivalMovementRequestService
            .convertToSubmissionModel(
              arrivalNotificationDomain,
              interchangeControlReference,
              timeOfPresentation
            )

          val expectedResult: ArrivalNotificationDomain = arrivalNotificationDomain match {
            case simplifiedNotification: SimplifiedNotification => simplifiedNotification.copy(authedEori = eoriNumber)
            case _                                              => arrivalNotificationDomain
          }

          val result = arrivalMovementRequestConversionService
            .convertToArrivalNotification(arrivalMovementRequest, arrivalNotificationDomain.customsOffice, eoriNumber)
            .value

          result mustBe expectedResult
      }
    }

    "must return None if MRN is malformed" in {

      val arrivalMovementRequest: ArrivalMovementRequest                 = arbitrary[ArrivalMovementRequest].sample.value
      val header: Header                                                 = arrivalMovementRequest.header.copy(movementReferenceNumber = "Invalid MRN")
      val arrivalMovementRequestWithMalformedMrn: ArrivalMovementRequest = arrivalMovementRequest.copy(header = header)
      val customsOffice                                                  = arbitrary[CustomsOffice].sample.value
      val eoriNumber                                                     = arbitrary[EoriNumber].sample.value

      arrivalMovementRequestConversionService.convertToArrivalNotification(arrivalMovementRequestWithMalformedMrn, customsOffice, eoriNumber) mustBe None
    }
  }

}
