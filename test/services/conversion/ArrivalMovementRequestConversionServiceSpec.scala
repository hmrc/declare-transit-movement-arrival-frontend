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
import models.{domain, MovementReferenceNumber}
import models.domain.{EnRouteEventDomain, NormalNotification, Trader}
import models.messages.{ArrivalMovementRequest, EnRouteEvent, Header}
import models.reference.Country
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class ArrivalMovementRequestConversionServiceSpec extends SpecBase with MessagesModelGenerators with ScalaCheckPropertyChecks {

  val arrivalMovementRequestConversionService: ArrivalMovementRequestConversionService = app.injector.instanceOf[ArrivalMovementRequestConversionService]

  "ArrivalMovementRequest" - {

    "must return None if MRN is malformed" in {
      val arrivalMovementRequest: ArrivalMovementRequest = arbitrary[ArrivalMovementRequest].sample.value

      val header: Header = arrivalMovementRequest.header.copy(movementReferenceNumber = "Invalid MRN")

      val arrivalMovementRequestWithMalformedMrn: ArrivalMovementRequest = arrivalMovementRequest.copy(header = header)

      arrivalMovementRequestConversionService.convertToArrivalNotification(arrivalMovementRequestWithMalformedMrn) mustBe None
    }

    "must convert ArrivalMovementRequest to NormalNotification for trader" in {

      val genArrivalNotificationRequest = arbitrary[ArrivalMovementRequest].sample.value
      val arrivalNotificationRequest    = genArrivalNotificationRequest.copy(header = genArrivalNotificationRequest.header.copy(customsSubPlace = Some("")))

      val convertEnRouteEvents = arrivalNotificationRequest.enRouteEvents.map {
        events =>
          events.map {
            event =>
              val country = Country("", event.countryCode, "")
              EnRouteEvent.enRouteEventToDomain(event, country)
          }
      }

      val normalNotification: NormalNotification = {
        domain.NormalNotification(
          movementReferenceNumber = MovementReferenceNumber(arrivalNotificationRequest.header.movementReferenceNumber).get,
          notificationPlace       = arrivalNotificationRequest.header.arrivalNotificationPlace,
          notificationDate        = arrivalNotificationRequest.header.notificationDate,
          customsSubPlace         = arrivalNotificationRequest.header.customsSubPlace.getOrElse(""),
          trader = Trader(
            name            = arrivalNotificationRequest.trader.name,
            city            = arrivalNotificationRequest.trader.city,
            postCode        = arrivalNotificationRequest.trader.postCode,
            countryCode     = arrivalNotificationRequest.trader.countryCode,
            streetAndNumber = arrivalNotificationRequest.trader.streetAndNumber,
            eori            = arrivalNotificationRequest.trader.eori
          ),
          presentationOfficeId   = arrivalNotificationRequest.header.presentationOfficeId,
          presentationOfficeName = arrivalNotificationRequest.header.presentationOfficeName,
          enRouteEvents          = convertEnRouteEvents
        )
      }

      arrivalMovementRequestConversionService.convertToArrivalNotification(arrivalNotificationRequest) mustBe Some(normalNotification)
    }
  }

}
