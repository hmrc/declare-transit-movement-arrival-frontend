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

import models.domain.{ArrivalNotificationDomain, NormalNotification, SimplifiedNotification}
import models.messages._
import models.reference.CustomsOffice
import models.{EoriNumber, MovementReferenceNumber, NormalProcedureFlag, SimplifiedProcedureFlag}

object ArrivalMovementRequestToArrivalNotificationService {

  def convertToArrivalNotification(arrivalMovementRequest: ArrivalMovementRequest,
                                   customsOffice: CustomsOffice,
                                   authEoriNumber: EoriNumber
  ): Option[ArrivalNotificationDomain] =
    (
      arrivalMovementRequest.header.procedureTypeFlag,
      MovementReferenceNumber(arrivalMovementRequest.header.movementReferenceNumber),
      arrivalMovementRequest.header.customsSubPlace
    ) match {
      case (NormalProcedureFlag, Some(mrn), Some(customsSubPlace)) =>
        Some(
          NormalNotification(
            movementReferenceNumber = mrn,
            notificationPlace = arrivalMovementRequest.header.arrivalNotificationPlace,
            notificationDate = arrivalMovementRequest.header.notificationDate,
            customsSubPlace = customsSubPlace,
            trader = Trader.messagesTraderToDomainTrader(arrivalMovementRequest.trader),
            customsOffice = customsOffice,
            enRouteEvents = arrivalMovementRequest.enRouteEvents.map(_.map(EnRouteEvent.enRouteEventToDomain))
          )
        )
      case (SimplifiedProcedureFlag, Some(mrn), _) =>
        arrivalMovementRequest.header.arrivalAuthorisedLocationOfGoods.map {
          authLocation =>
            SimplifiedNotification(
              movementReferenceNumber = mrn,
              notificationPlace = arrivalMovementRequest.header.arrivalNotificationPlace,
              notificationDate = arrivalMovementRequest.header.notificationDate,
              authorisedLocation = authLocation,
              trader = Trader.messagesTraderToDomainTrader(arrivalMovementRequest.trader),
              customsOffice = customsOffice,
              enRouteEvents = arrivalMovementRequest.enRouteEvents.map(_.map(EnRouteEvent.enRouteEventToDomain)),
              authedEori = authEoriNumber
            )
        }

      case _ => None
    }
}
