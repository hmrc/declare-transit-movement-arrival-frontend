/*
 * Copyright 2019 HM Revenue & Customs
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

import java.time.LocalDate

import base.SpecBase
import generators.DomainModelGenerators
import models.GoodsLocation.BorderForceOffice
import models.{TraderAddress, UserAnswers}
import models.domain.{EnRouteEvent, Endorsement, Incident, TraderWithEori}
import models.domain.messages.NormalNotification
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._

class ArrivalNotificationConversionServiceSpec extends SpecBase with ScalaCheckPropertyChecks with DomainModelGenerators {

  private val service = injector.instanceOf[ArrivalNotificationConversionService]

  "ArrivalNotificationConversionService" - {
    "must return 'Normal Arrival Notification' message when there are no EventDetails on route" in {
      forAll(normalNotificationWithTraderWithEoriWithSubplace) {
        case (arbArrivalNotification, trader) =>
          val expectedArrivalNotification: NormalNotification = arbArrivalNotification.copy(enRouteEvents = None)

          val userAnswers: UserAnswers = createBasicUserAnswers(trader, expectedArrivalNotification)

          service.convertToArrivalNotification(userAnswers).value mustEqual expectedArrivalNotification
      }
    }

    "must return 'Normal Arrival Notification' with trader address postcode as notification place when no notification place is set" in {
      forAll(normalNotificationWithTraderWithEoriWithSubplace) {
        case (arbArrivalNotification, trader) =>
          val expectedArrivalNotification: NormalNotification = arbArrivalNotification
            .copy(enRouteEvents = None)
            .copy(notificationPlace = trader.postCode.get)

          val userAnswers: UserAnswers = createBasicUserAnswers(trader, expectedArrivalNotification)
            .remove(PlaceOfNotificationPage)
            .success
            .value

          service.convertToArrivalNotification(userAnswers).value mustEqual expectedArrivalNotification
      }
    }

    "must return 'Normal Arrival Notification' message when there is on incident on route" in {
      forAll(normalNotificationWithTraderWithEoriWithSubplace, enRouteEventIncident) {
        case ((arbArrivalNotification, trader), (enRouteEvent, incident)) =>
          val routeEvent: EnRouteEvent = enRouteEvent
            .copy(seals = None)
            .copy(eventDetails = incident.copy(endorsement = Endorsement(None, None, None, None)))

          val arrivalNotification: NormalNotification = arbArrivalNotification.copy(enRouteEvents = Some(Seq(routeEvent)))

          val userAnswers: UserAnswers = createBasicUserAnswers(trader, arrivalNotification, true)
            .set(IsTranshipmentPage, false)
            .success
            .value
            .set(EventPlacePage, routeEvent.place)
            .success
            .value
            .set(EventCountryPage, routeEvent.countryCode)
            .success
            .value
            .set(EventReportedPage, routeEvent.alreadyInNcts)
            .success
            .value

          val updatedAnswers = incident.information.fold[UserAnswers](userAnswers) {
            _ =>
              userAnswers.set(IncidentInformationPage, incident.information.value).success.value
          }

          service.convertToArrivalNotification(updatedAnswers).value mustEqual arrivalNotification
      }
    }

    "must return 'None' from empty userAnswers" in {
      service.convertToArrivalNotification(emptyUserAnswers) mustNot be(defined)
    }

    "must return 'None' from a partly filled userAnswers" in {
      forAll(arbitrary[NormalNotification], generatorTraderWithEoriAllValues) {
        case (arrivalNotification, trader) =>
          val userAnswers: UserAnswers =
            emptyUserAnswers
              .set(MovementReferenceNumberPage, arrivalNotification.movementReferenceNumber)
              .success
              .value
              .set(TraderEoriPage, trader.eori)
              .success
              .value
              .set(IncidentOnRoutePage, false)
              .success
              .value

          service.convertToArrivalNotification(userAnswers) mustEqual (None)
      }
    }
  }

  private def createBasicUserAnswers(trader: TraderWithEori, arrivalNotification: NormalNotification, isIncidentOnRoute: Boolean = false): UserAnswers =
    emptyUserAnswers
      .set(MovementReferenceNumberPage, arrivalNotification.movementReferenceNumber)
      .success
      .value
      .set(GoodsLocationPage, BorderForceOffice)
      .success
      .value
      .set(PresentationOfficePage, arrivalNotification.presentationOffice)
      .success
      .value
      .set(CustomsSubPlacePage, arrivalNotification.customsSubPlace.value)
      .success
      .value
      .set(TraderNamePage, trader.name.value)
      .success
      .value
      .set(TraderAddressPage, TraderAddress(buildingAndStreet = trader.streetAndNumber.value, city = trader.city.value, postcode = trader.postCode.value))
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

  private val normalNotificationWithTraderWithEoriWithSubplace =
    for {
      base     <- arbitrary[NormalNotification]
      trader   <- generatorTraderWithEoriAllValues
      subPlace <- stringsWithMaxLength(17)
    } yield {

      val expected: NormalNotification = base
        .copy(movementReferenceNumber = mrn.toString)
        .copy(trader = trader)
        .copy(customsSubPlace = Some(subPlace))
        .copy(notificationDate = LocalDate.now())

      (expected, trader)
    }

  private val enRouteEventIncident: Gen[(EnRouteEvent, Incident)] = for {
    enRouteEvent <- arbitrary[EnRouteEvent]
    incident     <- arbitrary[Incident]
  } yield (enRouteEvent.copy(eventDetails = incident), incident)
}
