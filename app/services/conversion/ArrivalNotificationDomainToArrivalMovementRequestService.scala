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

import java.time.LocalTime

import models.domain._
import models.messages._
import models.{NormalProcedureFlag, ProcedureTypeFlag, SimplifiedProcedureFlag}

object ArrivalNotificationDomainToArrivalMovementRequestService {

  def convertToSubmissionModel(
    arrivalNotification: ArrivalNotificationDomain,
    interchangeControlReference: InterchangeControlReference,
    timeOfPresentation: LocalTime
  ): ArrivalMovementRequest =
    arrivalNotification match {
      case normalNotification: NormalNotification =>
        val meta = Meta(
          interchangeControlReference = interchangeControlReference,
          dateOfPreparation = normalNotification.notificationDate,
          timeOfPreparation = timeOfPresentation
        )
        val header            = buildHeader(normalNotification, NormalProcedureFlag)
        val traderDestination = TraderDomain.domainTraderToMessagesTrader(normalNotification.trader)
        val customsOffice     = CustomsOfficeOfPresentation(office = normalNotification.customsOffice.id)
        val enRouteEvents     = normalNotification.enRouteEvents.map(_.map(EnRouteEventDomain.domainEnrouteEventToEnrouteEvent))

        ArrivalMovementRequest(meta, header, traderDestination, customsOffice, enRouteEvents)

      case simplifiedNotification: SimplifiedNotification =>
        val meta = Meta(
          interchangeControlReference = interchangeControlReference,
          dateOfPreparation = simplifiedNotification.notificationDate,
          timeOfPreparation = timeOfPresentation
        )
        val header                                   = buildSimplifiedHeader(simplifiedNotification, SimplifiedProcedureFlag)
        val traderDestination                        = TraderDomain.domainTraderToMessagesTrader(simplifiedNotification.trader)
        val customsOffice                            = CustomsOfficeOfPresentation(office = simplifiedNotification.customsOffice.id)
        val enRouteEvents: Option[Seq[EnRouteEvent]] = simplifiedNotification.enRouteEvents.map(_.map(EnRouteEventDomain.domainEnrouteEventToEnrouteEvent))

        ArrivalMovementRequest(meta, header, traderDestination, customsOffice, enRouteEvents)
    }

  private def buildHeader(arrivalNotification: NormalNotification, procedureTypeFlag: ProcedureTypeFlag): Header =
    Header(
      movementReferenceNumber = arrivalNotification.movementReferenceNumber.toString,
      customsSubPlace = Some(arrivalNotification.customsSubPlace),
      arrivalNotificationPlace = arrivalNotification.notificationPlace,
      procedureTypeFlag = procedureTypeFlag,
      notificationDate = arrivalNotification.notificationDate
    )

  private def buildSimplifiedHeader(arrivalNotification: SimplifiedNotification, procedureTypeFlag: ProcedureTypeFlag): Header =
    Header(
      movementReferenceNumber = arrivalNotification.movementReferenceNumber.toString,
      customsSubPlace = None,
      arrivalNotificationPlace = arrivalNotification.notificationPlace,
      procedureTypeFlag = procedureTypeFlag,
      notificationDate = arrivalNotification.notificationDate,
      arrivalAuthorisedLocationOfGoods = Some(arrivalNotification.authorisedLocation)
    )
}
