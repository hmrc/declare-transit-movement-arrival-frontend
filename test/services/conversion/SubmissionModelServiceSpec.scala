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
import models.MovementReferenceNumber
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

      val normalNotification: NormalNotification = {
        NormalNotification(
          movementReferenceNumber = MovementReferenceNumber(arrivalMovementRequest.header.movementReferenceNumber).get,
          notificationPlace       = arrivalMovementRequest.header.arrivalNotificationPlace,
          notificationDate        = arrivalMovementRequest.header.notificationDate,
          customsSubPlace         = arrivalMovementRequest.header.customsSubPlace,
          trader = Trader(
            name            = arrivalMovementRequest.trader.name,
            streetAndNumber = arrivalMovementRequest.trader.streetAndNumber,
            postCode        = arrivalMovementRequest.trader.postCode,
            city            = arrivalMovementRequest.trader.city,
            countryCode     = arrivalMovementRequest.trader.countryCode,
            eori            = arrivalMovementRequest.trader.eori
          ),
          presentationOfficeId   = arrivalMovementRequest.header.presentationOfficeId,
          presentationOfficeName = arrivalMovementRequest.header.presentationOfficeName,
          enRouteEvents          = arrivalMovementRequest.enRouteEvents
        )
      }

      val messageSender               = arrivalMovementRequest.meta.messageSender
      val interchangeControlReference = arrivalMovementRequest.meta.interchangeControlReference

      val result = convertToSubmissionModel.convertToSubmissionModel(normalNotification,
                                                                     messageSender,
                                                                     interchangeControlReference,
                                                                     arrivalMovementRequest.meta.timeOfPreparation)

      result mustBe arrivalMovementRequest
    }
  }
}
