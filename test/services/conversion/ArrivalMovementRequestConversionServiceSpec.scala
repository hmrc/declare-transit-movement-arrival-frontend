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
import connectors.ReferenceDataConnector
import generators.MessagesModelGenerators
import models.domain.NormalNotification
import models.messages.{ArrivalMovementRequest, Header}
import models.reference.Country
import org.mockito.Matchers.any
import org.mockito.Mockito._
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind

import scala.concurrent.Future

class ArrivalMovementRequestConversionServiceSpec extends SpecBase with MessagesModelGenerators with ScalaCheckPropertyChecks {

  private val arrivalMovementRequestConversionService = ArrivalMovementRequestConversionService

  "convertToArrivalNotification" - {

    "must return None if MRN is malformed" in {

      val arrivalMovementRequest: ArrivalMovementRequest                 = arbitrary[ArrivalMovementRequest].sample.value
      val header: Header                                                 = arrivalMovementRequest.header.copy(movementReferenceNumber = "Invalid MRN")
      val arrivalMovementRequestWithMalformedMrn: ArrivalMovementRequest = arrivalMovementRequest.copy(header = header)

      arrivalMovementRequestConversionService.convertToArrivalNotification(arrivalMovementRequestWithMalformedMrn) mustBe None
    }

    "must return None when the CustomsSubPlace is not defined" in {

      val arrivalMovementRequest: ArrivalMovementRequest                = arbitrary[ArrivalMovementRequest].sample.value
      val header: Header                                                = arrivalMovementRequest.header.copy(customsSubPlace = None)
      val arrivalMovementRequestWithoutSubplace: ArrivalMovementRequest = arrivalMovementRequest.copy(header = header)

      arrivalMovementRequestConversionService.convertToArrivalNotification(arrivalMovementRequestWithoutSubplace) mustBe None
    }

    "must convert ArrivalMovementRequest to NormalNotification for trader" in {

      val genArrivalNotificationRequest = arbitrary[ArrivalMovementRequest].sample.value

      arrivalMovementRequestConversionService.convertToArrivalNotification(genArrivalNotificationRequest).value mustBe an[NormalNotification]
    }
  }

}
