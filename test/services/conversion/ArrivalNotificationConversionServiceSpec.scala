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
import models.domain.messages.NormalNotification
import models.domain.{EnRouteEvent, Endorsement, Incident, TraderWithEori}
import models.{TraderAddress, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._

class ArrivalNotificationConversionServiceSpec extends SpecBase with ScalaCheckPropertyChecks with DomainModelGenerators {

  val service = injector.instanceOf[ArrivalNotificationConversionService]

  "ArrivalNotificationConversionService" - {

    "must return 'Normal Arrival Notification' message from valid userAnswers" in {
      forAll(arbitrary[NormalNotification], generatorTraderWithEoriAllValues, Gen.alphaNumStr) {
        case (arbArrivalNotification, trader, subPlace) =>

          val arrivalNotification: NormalNotification = createArrivalNotification(arbArrivalNotification, trader, subPlace).copy(enRouteEvents = Seq.empty)

          val userAnswers: UserAnswers = createBasicUserAnswers(trader, subPlace, arrivalNotification)

          service.convertToArrivalNotification(userAnswers).value mustEqual arrivalNotification

      }
    }

    "must return 'Normal Arrival Notification' message when there is on incident on route" in {
      forAll(arbitrary[NormalNotification], generatorTraderWithEoriAllValues, Gen.alphaNumStr, arbitrary[EnRouteEvent]) {
        case (arbArrivalNotification, trader, subPlace, enRouteEvent) =>

          val incidentInformation = enRouteEvent.eventDetails match {
            case incident:Incident => incident.information
            case _ => None
          }

          val routeEvent = enRouteEvent
            .copy(seals = Seq.empty)
            .copy(eventDetails = Incident(incidentInformation, Endorsement(None, None, None, None)))

          val arrivalNotification: NormalNotification = createArrivalNotification(arbArrivalNotification, trader, subPlace)
            .copy(enRouteEvents = Seq(routeEvent))

          val userAnswers: UserAnswers = createBasicUserAnswers(trader, subPlace, arrivalNotification, true)
              .set(IsTranshipmentPage, false).success.value
              .set(EventPlacePage, routeEvent.place).success.value
              .set(EventCountryPage, routeEvent.countryCode).success.value
              .set(EventReportedPage, routeEvent.alreadyInNcts).success.value

            val updatedAnswers = incidentInformation.fold[UserAnswers](userAnswers) {_ =>
              userAnswers.set(IncidentInformationPage, incidentInformation.value).success.value
            }

          service.convertToArrivalNotification(updatedAnswers).value mustEqual arrivalNotification
      }
    }

    "must return 'None' from empty userAnswers" in {
      service.convertToArrivalNotification(emptyUserAnswers) mustEqual (None)
    }

    "must return 'None' from a partly filled userAnswers" in {
      forAll(arbitrary[NormalNotification], generatorTraderWithEoriAllValues) {
        case (arrivalNotification, trader) =>

          val userAnswers: UserAnswers =
            emptyUserAnswers
              .set(MovementReferenceNumberPage, arrivalNotification.movementReferenceNumber).success.value
              .set(TraderEoriPage, trader.eori).success.value
              .set(IncidentOnRoutePage, false).success.value

          service.convertToArrivalNotification(userAnswers) mustEqual (None)
      }
    }
  }

  private def createArrivalNotification(arrivalNotification: NormalNotification, trader: TraderWithEori, subPlace: String) = {
    arrivalNotification.copy(movementReferenceNumber = mrn.toString)
      .copy(trader = trader)
      .copy(customsSubPlace = Some(subPlace))
      .copy(notificationDate = LocalDate.now())
      .copy(notificationPlace = "") //TODO
  }

  private def createBasicUserAnswers(trader: TraderWithEori,
                               subPlace: String,
                               arrivalNotification: NormalNotification,
                               isIncidentOnRoute: Boolean = false): UserAnswers = {
      emptyUserAnswers
        .set(MovementReferenceNumberPage, arrivalNotification.movementReferenceNumber).success.value
        .set(GoodsLocationPage, BorderForceOffice).success.value
        .set(PresentationOfficePage, arrivalNotification.presentationOffice).success.value
        .set(CustomsSubPlacePage, subPlace).success.value
        .set(TraderNamePage, trader.name.value).success.value
        .set(TraderAddressPage, TraderAddress(
          buildingAndStreet = trader.streetAndNumber.value,
          city = trader.city.value,
          postcode = trader.postCode.value)
        ).success.value
        .set(TraderEoriPage, trader.eori).success.value
        .set(IncidentOnRoutePage, isIncidentOnRoute).success.value
  }
}
