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

import java.time.LocalTime

import com.google.inject.Inject
import models.messages._
import models.{NormalProcedureFlag, ProcedureTypeFlag, SimplifiedProcedureFlag}

class SubmissionModelService @Inject()() {

  def convertToSubmissionModel(
    arrivalNotification: ArrivalNotification,
    messageSender: MessageSender,
    interchangeControlReference: InterchangeControlReference,
    timeOfPresentation: LocalTime
  ): ArrivalMovementRequest =
    arrivalNotification match {
      case normalNotification: NormalNotification =>
        val meta = Meta(
          messageSender               = messageSender,
          interchangeControlReference = interchangeControlReference,
          dateOfPreparation           = normalNotification.notificationDate,
          timeOfPreparation           = timeOfPresentation
        )
        val header                                   = buildHeader(normalNotification, NormalProcedureFlag)
        val traderDestination                        = normalNotification.trader
        val customsOffice                            = CustomsOfficeOfPresentation(presentationOffice = normalNotification.presentationOfficeId)
        val enRouteEvents: Option[Seq[EnRouteEvent]] = normalNotification.enRouteEvents

        ArrivalMovementRequest(meta, header, traderDestination, customsOffice, enRouteEvents)

      case simplifiedNotification: SimplifiedNotification =>
        val meta = Meta(
          messageSender               = messageSender,
          interchangeControlReference = interchangeControlReference,
          dateOfPreparation           = simplifiedNotification.notificationDate,
          timeOfPreparation           = timeOfPresentation
        )
        val header                                   = buildSimplifiedHeader(simplifiedNotification, SimplifiedProcedureFlag)
        val traderDestination                        = simplifiedNotification.trader
        val customsOffice                            = CustomsOfficeOfPresentation(presentationOffice = simplifiedNotification.presentationOfficeId)
        val enRouteEvents: Option[Seq[EnRouteEvent]] = simplifiedNotification.enRouteEvents

        ArrivalMovementRequest(meta, header, traderDestination, customsOffice, enRouteEvents)
      case _ => ??? //todo: what do we do here?
    }

  private def buildHeader(arrivalNotification: NormalNotification, procedureTypeFlag: ProcedureTypeFlag): Header =
    Header(
      movementReferenceNumber  = arrivalNotification.movementReferenceNumber.toString,
      customsSubPlace          = arrivalNotification.customsSubPlace,
      arrivalNotificationPlace = arrivalNotification.notificationPlace,
      presentationOfficeId     = arrivalNotification.presentationOfficeId,
      presentationOfficeName   = arrivalNotification.presentationOfficeName,
      procedureTypeFlag        = procedureTypeFlag,
      notificationDate         = arrivalNotification.notificationDate
    )

  private def buildSimplifiedHeader(arrivalNotification: SimplifiedNotification, procedureTypeFlag: ProcedureTypeFlag): Header =
    Header(
      movementReferenceNumber  = arrivalNotification.movementReferenceNumber.toString,
      customsSubPlace          = None,
      arrivalNotificationPlace = arrivalNotification.approvedLocation.getOrElse(""),
      presentationOfficeId     = arrivalNotification.presentationOfficeId,
      presentationOfficeName   = arrivalNotification.presentationOfficeName,
      procedureTypeFlag        = procedureTypeFlag,
      notificationDate         = arrivalNotification.notificationDate
    )
}

sealed trait ModelConversionError

object FailedToConvertModel extends ModelConversionError
