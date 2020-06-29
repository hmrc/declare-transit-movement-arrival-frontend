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
import models.domain.{EnRouteEventDomain, NormalNotification, SimplifiedNotification, TraderDomain}
import models.messages.{ArrivalMovementRequest, EnRouteEvent}
import models.reference.Country
import models.{domain, MovementReferenceNumber, NormalProcedureFlag}
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

  val convertToSubmissionModel: SubmissionModelService.type = SubmissionModelService

  "SubmissionModelService" - {

    "must convert NormalNotification to ArrivalMovementRequest for traders" in {

      val arrivalMovementRequest = arbitrary[ArrivalMovementRequest].sample.value
      val setNormalTypeFlag = arrivalMovementRequest.header.copy(
        procedureTypeFlag = NormalProcedureFlag,
        customsSubPlace   = Some("")
      )

      val updatedArrivalMovementRequest = arrivalMovementRequest.copy(header = setNormalTypeFlag)
      val enRouteEventsDomain = updatedArrivalMovementRequest.enRouteEvents.map(_.map {
        event =>
          val country = Country("", event.countryCode, "")
          EnRouteEvent.enRouteEventToDomain(event, country)
      })

      val normalNotification = {
        NormalNotification(
          movementReferenceNumber = MovementReferenceNumber(updatedArrivalMovementRequest.header.movementReferenceNumber).get,
          notificationPlace       = updatedArrivalMovementRequest.header.arrivalNotificationPlace,
          notificationDate        = updatedArrivalMovementRequest.header.notificationDate,
          customsSubPlace         = updatedArrivalMovementRequest.header.customsSubPlace.get,
          trader = TraderDomain(
            name            = updatedArrivalMovementRequest.trader.name,
            streetAndNumber = updatedArrivalMovementRequest.trader.streetAndNumber,
            postCode        = updatedArrivalMovementRequest.trader.postCode,
            city            = updatedArrivalMovementRequest.trader.city,
            countryCode     = updatedArrivalMovementRequest.trader.countryCode,
            eori            = updatedArrivalMovementRequest.trader.eori
          ),
          presentationOfficeId   = updatedArrivalMovementRequest.header.presentationOfficeId,
          presentationOfficeName = updatedArrivalMovementRequest.header.presentationOfficeName,
          enRouteEvents          = enRouteEventsDomain
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
      val setFlag: Header                                = arrivalMovementRequest.header.copy(procedureTypeFlag = SimplifiedProcedureFlag, customsSubPlace = None)
      val updatedArrivalMovementRequest                  = arrivalMovementRequest.copy(header = setFlag)

      val enRouteEventsDomain = updatedArrivalMovementRequest.enRouteEvents.map(_.map {
        event =>
          val country = Country("", event.countryCode, "")
          EnRouteEvent.enRouteEventToDomain(event, country)
      })

      val simplifiedNotification: SimplifiedNotification = {
        SimplifiedNotification(
          movementReferenceNumber = MovementReferenceNumber(updatedArrivalMovementRequest.header.movementReferenceNumber).get,
          notificationPlace       = updatedArrivalMovementRequest.header.arrivalNotificationPlace, //TODO: Is this required
          notificationDate        = updatedArrivalMovementRequest.header.notificationDate,
          approvedLocation        = Some(updatedArrivalMovementRequest.header.arrivalNotificationPlace),
          trader = TraderDomain(
            name            = updatedArrivalMovementRequest.trader.name,
            streetAndNumber = updatedArrivalMovementRequest.trader.streetAndNumber,
            postCode        = updatedArrivalMovementRequest.trader.postCode,
            city            = updatedArrivalMovementRequest.trader.city,
            countryCode     = updatedArrivalMovementRequest.trader.countryCode,
            eori            = updatedArrivalMovementRequest.trader.eori
          ),
          presentationOfficeId   = updatedArrivalMovementRequest.header.presentationOfficeId,
          presentationOfficeName = updatedArrivalMovementRequest.header.presentationOfficeName,
          enRouteEvents          = enRouteEventsDomain
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
