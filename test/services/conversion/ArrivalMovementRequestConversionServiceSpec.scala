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
import models.MovementReferenceNumber
import models.messages.{ArrivalMovementRequest, Header, NormalNotification, TraderWithEori, TraderWithoutEori}
import org.scalacheck.Gen
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class ArrivalMovementRequestConversionServiceSpec extends SpecBase with MessagesModelGenerators with ScalaCheckPropertyChecks {

  val arrivalMovementRequestConversionService: ArrivalMovementRequestConversionService = app.injector.instanceOf[ArrivalMovementRequestConversionService]

  "ArrivalMovementRequest" - {

    "must return None if MRN is malformed" in {
      val arrivalMovementRequest: ArrivalMovementRequest = arbitrary[ArrivalMovementRequest].sample.value

      val header: Header = arrivalMovementRequest.header.copy(movementReferenceNumber = "FRANK")

      val arrivalMovementRequestWithMalformedMrn: ArrivalMovementRequest = arrivalMovementRequest.copy(header = header)

      arrivalMovementRequestConversionService.convertToArrivalNotification(arrivalMovementRequestWithMalformedMrn) mustBe None
    }

    "must convert ArrivalMovementRequest to NormalNotification for traders with Eori" in {
      val notifications: Gen[(ArrivalMovementRequest, NormalNotification)] = {
        for {
          arrivalNotificationRequest <- arbitraryArrivalMovementRequestWithEori
        } yield {

          val normalNotification: NormalNotification = {
            NormalNotification(
              movementReferenceNumber = MovementReferenceNumber(arrivalNotificationRequest.header.movementReferenceNumber).get,
              notificationPlace       = arrivalNotificationRequest.header.arrivalNotificationPlace,
              notificationDate        = arrivalNotificationRequest.header.notificationDate,
              customsSubPlace         = arrivalNotificationRequest.header.customsSubPlace,
              trader = TraderWithEori(
                name            = arrivalNotificationRequest.traderDestination.name,
                streetAndNumber = arrivalNotificationRequest.traderDestination.streetAndNumber,
                postCode        = arrivalNotificationRequest.traderDestination.postCode,
                city            = arrivalNotificationRequest.traderDestination.city,
                countryCode     = arrivalNotificationRequest.traderDestination.countryCode,
                eori            = arrivalNotificationRequest.traderDestination.eori.value
              ),
              presentationOfficeId   = arrivalNotificationRequest.header.presentationOfficeId,
              presentationOfficeName = arrivalNotificationRequest.header.presentationOfficeName,
              enRouteEvents          = arrivalNotificationRequest.enRouteEvents
            )
          }

          (arrivalNotificationRequest, normalNotification)
        }
      }

      forAll(notifications) {

        case (arrivalNotificationRequest, normalNotification) =>
          arrivalMovementRequestConversionService.convertToArrivalNotification(arrivalNotificationRequest) mustBe Some(normalNotification)
      }
    }

    "must convert ArrivalMovementRequest to NormalNotification for traders without Eori" in {
      val notifications: Gen[(ArrivalMovementRequest, NormalNotification)] = {
        for {
          arrivalNotificationRequest <- arbitraryArrivalMovementRequestWithoutEori
        } yield {

          val normalNotification: NormalNotification = {
            NormalNotification(
              movementReferenceNumber = MovementReferenceNumber(arrivalNotificationRequest.header.movementReferenceNumber).get,
              notificationPlace       = arrivalNotificationRequest.header.arrivalNotificationPlace,
              notificationDate        = arrivalNotificationRequest.header.notificationDate,
              customsSubPlace         = arrivalNotificationRequest.header.customsSubPlace,
              trader = TraderWithoutEori(
                name            = arrivalNotificationRequest.traderDestination.name.value,
                streetAndNumber = arrivalNotificationRequest.traderDestination.streetAndNumber.value,
                postCode        = arrivalNotificationRequest.traderDestination.postCode.value,
                city            = arrivalNotificationRequest.traderDestination.city.value,
                countryCode     = arrivalNotificationRequest.traderDestination.countryCode.value
              ),
              presentationOfficeId   = arrivalNotificationRequest.header.presentationOfficeId,
              presentationOfficeName = arrivalNotificationRequest.header.presentationOfficeName,
              enRouteEvents          = arrivalNotificationRequest.enRouteEvents
            )
          }

          (arrivalNotificationRequest, normalNotification)
        }
      }

      forAll(notifications) {

        case (arrivalNotificationRequest, normalNotification) =>
          arrivalMovementRequestConversionService.convertToArrivalNotification(arrivalNotificationRequest) mustBe Some(normalNotification)
      }
    }
  }

}
