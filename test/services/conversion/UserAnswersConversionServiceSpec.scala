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

import java.time.{LocalDate, LocalDateTime}

import base.SpecBase
import generators.MessagesModelGenerators
import models.GoodsLocation.BorderForceOffice
import models.messages._
import models.reference.CustomsOffice
import models.{Address, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._

class UserAnswersConversionServiceSpec extends SpecBase with ScalaCheckPropertyChecks with MessagesModelGenerators {

  val userAnswersConversionService: UserAnswersConversionService = app.injector.instanceOf[UserAnswersConversionService]

  private val arrivalNotificationWithSubplace: Gen[(NormalNotification, Trader)] =
    for {
      base     <- arbitrary[NormalNotification]
      trader   <- arbitrary[Trader]
      subPlace <- stringsWithMaxLength(NormalNotification.Constants.customsSubPlaceLength)
    } yield {

      val expected: NormalNotification = base
        .copy(movementReferenceNumber = mrn)
        .copy(trader = trader)
        .copy(customsSubPlace = Some(subPlace))
        .copy(notificationDate = LocalDate.now())

      (expected, trader)
    }

  private val enRouteEventIncident: Gen[(EnRouteEvent, Incident)] = for {
    enRouteEvent <- arbitrary[EnRouteEvent]
    incident     <- arbitrary[Incident]
  } yield (enRouteEvent.copy(eventDetails = Some(incident)), incident)

  private val enRouteEventVehicularTranshipment: Gen[(EnRouteEvent, VehicularTranshipment)] = for {
    enRouteEvent          <- arbitrary[EnRouteEvent]
    vehicularTranshipment <- arbitrary[VehicularTranshipment]
  } yield (enRouteEvent.copy(eventDetails = Some(vehicularTranshipment)), vehicularTranshipment)

  "UserAnswersConversionService" - {

    "must return 'UserAnswers' message when there are no EventDetails on route" in {
      forAll(arrivalNotificationWithSubplace) {
        case (arbArrivalNotification, trader) =>
          val expectedArrivalNotification: NormalNotification = arbArrivalNotification.copy(enRouteEvents = None)

          val result = userAnswersConversionService.convertToUserAnswers(arbArrivalNotification).value

          val userAnswers: UserAnswers =
            createBasicUserAnswers(trader, expectedArrivalNotification, arbArrivalNotification.enRouteEvents.isDefined, result.lastUpdated)

          result mustBe userAnswers
      }
    }
  }

  private def createBasicUserAnswers(trader: Trader,
                                     arrivalNotification: NormalNotification,
                                     isIncidentOnRoute: Boolean = false,
                                     timeStamp: LocalDateTime): UserAnswers =
    emptyUserAnswers
      .copy(id = arrivalNotification.movementReferenceNumber)
      .copy(lastUpdated = timeStamp)
      .set(GoodsLocationPage, BorderForceOffice)
      .success
      .value
      .set(
        PresentationOfficePage,
        CustomsOffice(id = arrivalNotification.presentationOfficeId, name = arrivalNotification.presentationOfficeName, roles = Seq.empty, None)
      )
      .success
      .value
      .set(CustomsSubPlacePage, arrivalNotification.customsSubPlace.value)
      .success
      .value
      .set(TraderNamePage, trader.name)
      .success
      .value
      .set(TraderAddressPage, Address(buildingAndStreet = trader.streetAndNumber, city = trader.city, postcode = trader.postCode))
      .success
      .value
      .set(TraderEoriPage, trader.eori)
      .success
      .value
      .set(IncidentOnRoutePage, isIncidentOnRoute)
      .success
      .value
      .set(PlaceOfNotificationPage, arrivalNotification.notificationPlace)
      .success
      .value

}
