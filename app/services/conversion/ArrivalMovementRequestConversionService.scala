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

import models.MovementReferenceNumber
import models.domain.{ArrivalNotificationDomain, EnRouteEventDomain, NormalNotification}
import models.messages.{ArrivalMovementRequest, EnRouteEvent, Trader}
import models.reference.Country

object ArrivalMovementRequestConversionService {

  def convertToArrivalNotification(arrivalMovementRequest: ArrivalMovementRequest): Option[ArrivalNotificationDomain] =
    MovementReferenceNumber(arrivalMovementRequest.header.movementReferenceNumber) map {
      mrn =>
        // TODO How do we handle the call to the connector here???
        val buildEnrouteEvents: Option[Seq[EnRouteEventDomain]] = arrivalMovementRequest.enRouteEvents.map {
          events =>
            events.map {
              event =>
                val country = Country("", event.countryCode, "")
                EnRouteEvent.enRouteEventToDomain(event, country)
            }
        }

        NormalNotification(
          mrn,
          arrivalMovementRequest.header.arrivalNotificationPlace,
          arrivalMovementRequest.header.notificationDate,
          arrivalMovementRequest.header.customsSubPlace.get, // TODO need to address the case when there is no subsplace
          Trader.messagesTraderToDomainTrader(arrivalMovementRequest.trader),
          arrivalMovementRequest.header.presentationOfficeId,
          arrivalMovementRequest.header.presentationOfficeName,
          buildEnrouteEvents
        )
    }
}
