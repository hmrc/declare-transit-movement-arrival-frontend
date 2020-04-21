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
import org.scalacheck.Gen
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

    "must convert NormalNotification to ArrivalMovementRequest for traders with Eori" in {

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
              presentationOfficeId   = arrivalNotificationRequest.customsOfficeOfPresentation.presentationOffice,
              presentationOfficeName = arrivalNotificationRequest.customsOfficeOfPresentation.presentationOffice,
              enRouteEvents          = arrivalNotificationRequest.enRouteEvents
            )
          }

          (arrivalNotificationRequest, normalNotification)
        }
      }

      forAll(notifications) {

        case (arrivalNotificationRequest, normalNotification) =>
          val messageSender               = arrivalNotificationRequest.meta.messageSender
          val interchangeControlReference = arrivalNotificationRequest.meta.interchangeControlReference

          val result = convertToSubmissionModel.convertToSubmissionModel(normalNotification,
                                                                         messageSender,
                                                                         interchangeControlReference,
                                                                         arrivalNotificationRequest.meta.timeOfPreparation)

          result mustBe arrivalNotificationRequest
      }
    }
  }

  "must convert NormalNotification to ArrivalMovementRequest for traders without Eori" in {

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
            presentationOfficeId   = arrivalNotificationRequest.customsOfficeOfPresentation.presentationOffice,
            presentationOfficeName = arrivalNotificationRequest.customsOfficeOfPresentation.presentationOffice,
            enRouteEvents          = arrivalNotificationRequest.enRouteEvents
          )
        }

        (arrivalNotificationRequest, normalNotification)
      }
    }

    forAll(notifications) {

      case (arrivalNotificationRequest, normalNotification) =>
        val messageSender: MessageSender                             = arrivalNotificationRequest.meta.messageSender
        val interchangeControlReference: InterchangeControlReference = arrivalNotificationRequest.meta.interchangeControlReference

        val result = convertToSubmissionModel.convertToSubmissionModel(normalNotification,
                                                                       messageSender,
                                                                       interchangeControlReference,
                                                                       arrivalNotificationRequest.meta.timeOfPreparation)

        result mustBe arrivalNotificationRequest
    }
  }
}
