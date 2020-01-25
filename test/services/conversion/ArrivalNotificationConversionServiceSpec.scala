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

import java.time.LocalDate

import base.SpecBase
import generators.MessagesModelGenerators
import models.GoodsLocation.BorderForceOffice
import models.messages.{NormalNotification, _}
import models.reference.{Country, CustomsOffice}
import models.{Index, TraderAddress, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._
import pages.events._
import pages.events.seals.SealIdentityPage
import pages.events.transhipments._
import queries.ContainersQuery

class ArrivalNotificationConversionServiceSpec extends SpecBase with ScalaCheckPropertyChecks with MessagesModelGenerators {
  // format: off
  private val service = injector.instanceOf[ArrivalNotificationConversionService]

  private val normalNotificationWithTraderWithEoriWithSubplace: Gen[(NormalNotification, TraderWithEori)] =
    for {
      base     <- arbitrary[NormalNotification]
      trader   <- generatorTraderWithEoriAllValues
      subPlace <- stringsWithMaxLength(NormalNotification.Constants.customsSubPlaceLength)
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


  private val enRouteEventVehicularTranshipment: Gen[(EnRouteEvent, VehicularTranshipment)] = for {
    enRouteEvent <- arbitrary[EnRouteEvent]
    vehicularTranshipment     <- arbitrary[VehicularTranshipment]
  } yield (enRouteEvent.copy(eventDetails = vehicularTranshipment), vehicularTranshipment)

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

    "must return 'Normal Arrival Notification' message when there is one incident on route" in {
      forAll(normalNotificationWithTraderWithEoriWithSubplace, enRouteEventIncident) {
        case ((arbArrivalNotification, trader), (enRouteEvent, incident)) =>
          val routeEvent: EnRouteEvent = enRouteEvent
            .copy(seals = None)
            .copy(eventDetails = incident.copy(endorsement = Endorsement(None, None, None, None)))

          val arrivalNotification: NormalNotification = arbArrivalNotification.copy(enRouteEvents = Some(Seq(routeEvent)))

          val userAnswers: UserAnswers = createBasicUserAnswers(trader, arrivalNotification, isIncidentOnRoute = true)
            .set(IsTranshipmentPage(eventIndex), false).success.value
            .set(EventPlacePage(eventIndex), routeEvent.place).success.value
            .set(EventCountryPage(eventIndex), Country("Valid", routeEvent.countryCode, "country name")).success.value
            .set(EventReportedPage(eventIndex), routeEvent.alreadyInNcts).success.value

          val updatedAnswers = incident.information.fold[UserAnswers](userAnswers) {
            _ =>
              userAnswers.set(IncidentInformationPage(eventIndex), incident.information.value).success.value
          }

          service.convertToArrivalNotification(updatedAnswers).value mustEqual arrivalNotification
      }
    }

    "must return 'Normal Arrival Notification' message when there is one vehicle transhipment on route" in {
      forAll(normalNotificationWithTraderWithEoriWithSubplace, enRouteEventVehicularTranshipment) {
        case ((arbArrivalNotification, trader), (enRouteEvent, vehicularTranshipment)) =>
          val routeEvent: EnRouteEvent = enRouteEvent
            .copy(seals = Some(Seq(Seal("seal 1"), Seal("seal 2"))))
            .copy(eventDetails = vehicularTranshipment.copy(
              endorsement = Endorsement(None, None, None, None),
              containers = None
            ))

          val arrivalNotification: NormalNotification = arbArrivalNotification.copy(enRouteEvents = Some(Seq(routeEvent)))
          val userAnswers: UserAnswers = createBasicUserAnswers(trader, arrivalNotification, isIncidentOnRoute = true)
            .set(IsTranshipmentPage(eventIndex), true).success.value
            .set(EventPlacePage(eventIndex), routeEvent.place).success.value
            .set(EventCountryPage(eventIndex), Country("Valid", routeEvent.countryCode, "country name")).success.value
            .set(EventReportedPage(eventIndex), routeEvent.alreadyInNcts).success.value
            .set(TransportIdentityPage(eventIndex), vehicularTranshipment.transportIdentity).success.value
            .set(TransportNationalityPage(eventIndex), Country("Valid",vehicularTranshipment.transportCountry, "country name")).success.value
            .set(SealIdentityPage(eventIndex, Index(0)), "seal 1").success.value
            .set(SealIdentityPage(eventIndex, Index(1)), "seal 2").success.value

          service.convertToArrivalNotification(userAnswers).value mustEqual arrivalNotification
      }
    }

    val enRouteEventContainerTranshipment: Gen[(EnRouteEvent, ContainerTranshipment)] = for {
      generatedEnRouteEvent <- arbitrary[EnRouteEvent]
      ct <- arbitrary[ContainerTranshipment]
    } yield {
      val containerTranshipment = ct.copy(endorsement = Endorsement(None, None, None, None))

      val enRouteEvent = generatedEnRouteEvent.copy(eventDetails = containerTranshipment, seals = None)

      (enRouteEvent, containerTranshipment)
    }

    "must return 'Normal Arrival Notification' message when there is one container transhipment on route" in {
      forAll(normalNotificationWithTraderWithEoriWithSubplace, enRouteEventContainerTranshipment) {
        case ((arbArrivalNotification, trader), (enRouteEvent, containerTranshipment)) =>

          val expectedArrivalNotification: NormalNotification = arbArrivalNotification.copy(enRouteEvents = Some(Seq(enRouteEvent)))

          val containers: Seq[Container] = containerTranshipment.containers

          val userAnswers: UserAnswers = emptyUserAnswers
            .set(MovementReferenceNumberPage, expectedArrivalNotification.movementReferenceNumber).success.value
            .set(GoodsLocationPage, BorderForceOffice).success.value
            .set(PresentationOfficePage, CustomsOffice(id = expectedArrivalNotification.presentationOffice, name = "name", roles = Seq.empty)).success.value
            .set(CustomsSubPlacePage, expectedArrivalNotification.customsSubPlace.value).success.value
            .set(TraderNamePage, trader.name.value).success.value
            .set(TraderAddressPage, TraderAddress(buildingAndStreet = trader.streetAndNumber.value, city = trader.city.value, postcode = trader.postCode.value)).success.value
            .set(TraderEoriPage, trader.eori).success.value
            .set(IncidentOnRoutePage, true).success.value
            .set(PlaceOfNotificationPage, expectedArrivalNotification.notificationPlace).success.value
            .set(IsTranshipmentPage(eventIndex), true).success.value
            .set(EventPlacePage(eventIndex), enRouteEvent.place).success.value
            .set(EventCountryPage(eventIndex), Country("Valid", enRouteEvent.countryCode, "country name")).success.value
            .set(EventReportedPage(eventIndex), enRouteEvent.alreadyInNcts).success.value
            .set(ContainersQuery(eventIndex), containers).success.value

          service.convertToArrivalNotification(userAnswers).value mustEqual expectedArrivalNotification
      }
    }

    "must return 'Normal Arrival Notification' message when there multiple incidents on route" in {
      forAll(normalNotificationWithTraderWithEoriWithSubplace, enRouteEventIncident, enRouteEventIncident) {
        case ((arbArrivalNotification, trader), (enRouteEvent1, incident1), (enRouteEvent2, incident2)) =>
          val routeEvent1: EnRouteEvent = enRouteEvent1
            .copy(seals = None)
            .copy(eventDetails = incident1.copy(endorsement = Endorsement(None, None, None, None)))

          val routeEvent2: EnRouteEvent = enRouteEvent2
            .copy(seals = None)
            .copy(eventDetails = incident2.copy(endorsement = Endorsement(None, None, None, None)))

          val arrivalNotification: NormalNotification = arbArrivalNotification.copy(enRouteEvents = Some(Seq(routeEvent1, routeEvent2)))

          val eventIndex2 = Index(1)

          val userAnswers: UserAnswers = createBasicUserAnswers(trader, arrivalNotification, isIncidentOnRoute = true)
            .set(IsTranshipmentPage(eventIndex), false).success.value
            .set(EventPlacePage(eventIndex), routeEvent1.place).success.value
            .set(EventCountryPage(eventIndex), Country("Valid", routeEvent1.countryCode, "country name")).success.value
            .set(EventReportedPage(eventIndex), routeEvent1.alreadyInNcts).success.value
            .set(IsTranshipmentPage(eventIndex2), false).success.value
            .set(EventPlacePage(eventIndex2), routeEvent2.place).success.value
            .set(EventCountryPage(eventIndex2), Country("Valid", routeEvent2.countryCode, "country name")).success.value
            .set(EventReportedPage(eventIndex2), routeEvent2.alreadyInNcts).success.value

          val updatedAnswers1 = incident1.information.fold[UserAnswers](userAnswers) {
            _ =>
              userAnswers.set(IncidentInformationPage(eventIndex), incident1.information.value).success.value
          }

          val updatedAnswers = incident2.information.fold[UserAnswers](updatedAnswers1) {
            _ =>
              updatedAnswers1.set(IncidentInformationPage(eventIndex2), incident2.information.value).success.value
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
      .set(MovementReferenceNumberPage, arrivalNotification.movementReferenceNumber).success.value
      .set(GoodsLocationPage, BorderForceOffice).success.value
      .set(PresentationOfficePage, CustomsOffice(id = arrivalNotification.presentationOffice, name = "name", roles = Seq.empty)).success.value
      .set(CustomsSubPlacePage, arrivalNotification.customsSubPlace.value).success.value
      .set(TraderNamePage, trader.name.value).success.value
      .set(TraderAddressPage, TraderAddress(buildingAndStreet = trader.streetAndNumber.value, city = trader.city.value, postcode = trader.postCode.value)).success.value
      .set(TraderEoriPage, trader.eori).success.value
      .set(IncidentOnRoutePage, isIncidentOnRoute).success.value
      .set(PlaceOfNotificationPage, arrivalNotification.notificationPlace).success.value
  // format: on
}
