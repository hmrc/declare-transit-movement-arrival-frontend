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

import generators.MessagesModelGenerators
import models.{MovementReferenceNumber, NormalProcedureFlag, SimplifiedProcedureFlag}
import models.messages._
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.{FreeSpec, MustMatchers, OptionValues}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import play.api.inject.Injector

class SubmissionModelServiceSpec
    extends FreeSpec
    with MustMatchers
    with GuiceOneAppPerSuite
    with MessagesModelGenerators
    with ScalaCheckDrivenPropertyChecks
    with OptionValues {

  def injector: Injector = app.injector

  val convertToSubmissionModel: SubmissionModelService = injector.instanceOf[SubmissionModelService]

  "SubmissionModelService" - {

    "must convert NormalNotification to ArrivalMovementRequest for traders" in {

      val arrivalMovementRequest: ArrivalMovementRequest = arbitrary[ArrivalMovementRequest].sample.value
      val setNormalTypeFlag: Header                      = arrivalMovementRequest.header.copy(procedureTypeFlag = NormalProcedureFlag)
      val updatedArrivalMovementRequest                  = arrivalMovementRequest.copy(header = setNormalTypeFlag)

      val normalNotification: NormalNotification = {
        NormalNotification(
          movementReferenceNumber = MovementReferenceNumber(updatedArrivalMovementRequest.header.movementReferenceNumber).get,
          notificationPlace       = updatedArrivalMovementRequest.header.arrivalNotificationPlace,
          notificationDate        = updatedArrivalMovementRequest.header.notificationDate,
          customsSubPlace         = updatedArrivalMovementRequest.header.customsSubPlace,
          trader = Trader(
            name            = updatedArrivalMovementRequest.trader.name,
            streetAndNumber = updatedArrivalMovementRequest.trader.streetAndNumber,
            postCode        = updatedArrivalMovementRequest.trader.postCode,
            city            = updatedArrivalMovementRequest.trader.city,
            countryCode     = updatedArrivalMovementRequest.trader.countryCode,
            eori            = updatedArrivalMovementRequest.trader.eori
          ),
          presentationOfficeId   = updatedArrivalMovementRequest.header.presentationOfficeId,
          presentationOfficeName = updatedArrivalMovementRequest.header.presentationOfficeName,
          enRouteEvents          = updatedArrivalMovementRequest.enRouteEvents
        )
      }

      val messageSender               = updatedArrivalMovementRequest.meta.messageSender
      val interchangeControlReference = updatedArrivalMovementRequest.meta.interchangeControlReference

      val result = convertToSubmissionModel.convertToSubmissionModel(normalNotification,
                                                                     messageSender,
                                                                     interchangeControlReference,
                                                                     updatedArrivalMovementRequest.meta.timeOfPreparation)

      result mustBe updatedArrivalMovementRequest
    }

    "must convert SimplifiedNotification to ArrivalMovementRequest for traders" in {

      val arrivalMovementRequest: ArrivalMovementRequest = arbitrary[ArrivalMovementRequest].sample.value
      val setFlag: Header                                = arrivalMovementRequest.header.copy(procedureTypeFlag = SimplifiedProcedureFlag)
      val updatedArrivalMovementRequest                  = arrivalMovementRequest.copy(header = setFlag)

      val simplifiedNotification: SimplifiedNotification = {
        SimplifiedNotification(
          movementReferenceNumber = MovementReferenceNumber(updatedArrivalMovementRequest.header.movementReferenceNumber).get,
          notificationPlace       = updatedArrivalMovementRequest.header.arrivalNotificationPlace,
          notificationDate        = updatedArrivalMovementRequest.header.notificationDate,
          approvedLocation        = updatedArrivalMovementRequest.header.customsSubPlace,
          trader = Trader(
            name            = updatedArrivalMovementRequest.trader.name,
            streetAndNumber = updatedArrivalMovementRequest.trader.streetAndNumber,
            postCode        = updatedArrivalMovementRequest.trader.postCode,
            city            = updatedArrivalMovementRequest.trader.city,
            countryCode     = updatedArrivalMovementRequest.trader.countryCode,
            eori            = updatedArrivalMovementRequest.trader.eori
          ),
          presentationOfficeId   = updatedArrivalMovementRequest.header.presentationOfficeId,
          presentationOfficeName = updatedArrivalMovementRequest.header.presentationOfficeName,
          enRouteEvents          = updatedArrivalMovementRequest.enRouteEvents
        )
      }

      val messageSender               = updatedArrivalMovementRequest.meta.messageSender
      val interchangeControlReference = updatedArrivalMovementRequest.meta.interchangeControlReference

      val result = convertToSubmissionModel.convertToSubmissionModel(simplifiedNotification,
                                                                     messageSender,
                                                                     interchangeControlReference,
                                                                     updatedArrivalMovementRequest.meta.timeOfPreparation)

      result mustBe updatedArrivalMovementRequest
    }
  }
}
