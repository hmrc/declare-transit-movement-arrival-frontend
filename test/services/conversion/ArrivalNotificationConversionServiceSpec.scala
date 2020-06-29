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

import base.SpecBase
import generators.MessagesModelGenerators
import models.GoodsLocation.BorderForceOffice
import models.domain.{ContainerTranshipmentDomain, EnRouteEventDomain, NormalNotification, SealDomain, TraderDomain}
import models.messages.{ContainerTranshipment, EnRouteEvent, Seal}
import models.reference.{Country, CustomsOffice}
import models.{Address, Index, UserAnswers}
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

  "ArrivalNotificationConversionService" - {
    "must return 'Normal Arrival Notification' message when there are no EventDetails on route" in {
      forAll(arrivalNotificationWithSubplace) {
        case (arbArrivalNotification, trader) =>
          val expectedArrivalNotification: NormalNotification = arbArrivalNotification.copy(enRouteEvents = None)

          val userAnswers: UserAnswers = createBasicUserAnswers(trader, expectedArrivalNotification)

          service.convertToArrivalNotification(userAnswers).value mustEqual expectedArrivalNotification
      }
    }

    "must return 'Normal Arrival Notification' with trader address postcode as notification place when no notification place is set" in {
      forAll(arrivalNotificationWithSubplace) {
        case (arbArrivalNotification, trader) =>
          val expectedArrivalNotification: NormalNotification = arbArrivalNotification
            .copy(enRouteEvents = None)
            .copy(notificationPlace = trader.postCode)

          val userAnswers: UserAnswers = createBasicUserAnswers(trader, expectedArrivalNotification)
            .remove(PlaceOfNotificationPage)
            .success
            .value

          service.convertToArrivalNotification(userAnswers).value mustEqual expectedArrivalNotification
      }
    }

    "must return 'Normal Arrival Notification' message when there is one incident on route" in {
      forAll(arrivalNotificationWithSubplace, enRouteEventIncident) {
        case ((arbArrivalNotification, trader), (enRouteEvent, incident)) =>
          val routeEvent: EnRouteEventDomain = enRouteEvent
            .copy(seals = None)
            .copy(eventDetails = Some(incident))

          val arrivalNotification: NormalNotification = arbArrivalNotification.copy(enRouteEvents = Some(Seq(routeEvent)))

          val userAnswers: UserAnswers = createBasicUserAnswers(trader, arrivalNotification, isIncidentOnRoute = true)
            .set(IsTranshipmentPage(eventIndex), false).success.value
            .set(EventPlacePage(eventIndex), routeEvent.place).success.value
            .set(EventCountryPage(eventIndex), routeEvent.country).success.value
            .set(EventReportedPage(eventIndex), routeEvent.alreadyInNcts).success.value

          val updatedAnswers = incident.incidentInformation.fold[UserAnswers](userAnswers) {
            _ =>
              userAnswers.set(IncidentInformationPage(eventIndex), incident.incidentInformation.value).success.value
          }

          service.convertToArrivalNotification(updatedAnswers).value mustEqual arrivalNotification
      }
    }

    "must return 'Normal Arrival Notification' message when there is one vehicle transhipment on route" in {
      forAll(arrivalNotificationWithSubplace, enRouteEventVehicularTranshipment) {
        case ((arbArrivalNotification, trader), (enRouteEvent, vehicularTranshipment)) =>
          val routeEvent = enRouteEvent
            .copy(seals = Some(Seq(SealDomain("seal 1"), SealDomain("seal 2"))))
            .copy(eventDetails = Some(vehicularTranshipment.copy(containers = None)))

          val arrivalNotification: NormalNotification = arbArrivalNotification.copy(enRouteEvents = Some(Seq(routeEvent)))
          val userAnswers: UserAnswers = createBasicUserAnswers(trader, arrivalNotification, isIncidentOnRoute = true)
            .set(IsTranshipmentPage(eventIndex), true).success.value
            .set(EventPlacePage(eventIndex), routeEvent.place).success.value
            .set(EventCountryPage(eventIndex), routeEvent.country).success.value
            .set(EventReportedPage(eventIndex), routeEvent.alreadyInNcts).success.value
            .set(TransportIdentityPage(eventIndex), vehicularTranshipment.transportIdentity).success.value
            .set(TransportNationalityPage(eventIndex), vehicularTranshipment.transportCountry).success.value
            .set(SealIdentityPage(eventIndex, Index(0)), SealDomain("seal 1")).success.value
            .set(SealIdentityPage(eventIndex, Index(1)), SealDomain("seal 2")).success.value

          service.convertToArrivalNotification(userAnswers).value mustEqual arrivalNotification
      }
    }

    val enRouteEventContainerTranshipment: Gen[(EnRouteEventDomain, ContainerTranshipmentDomain)] = for {
      generatedEnRouteEvent <- arbitrary[EnRouteEventDomain]
      containerTranshipment <- arbitrary[ContainerTranshipmentDomain]
    } yield {
      val enRouteEvent = generatedEnRouteEvent.copy(eventDetails = Some(containerTranshipment), seals = None)

      (enRouteEvent, containerTranshipment)
    }

    "must return 'Normal Arrival Notification' message when there is one container transhipment on route" in {
      forAll(arrivalNotificationWithSubplace, enRouteEventContainerTranshipment) {
        case ((arbArrivalNotification, trader), (enRouteEvent, containerTranshipment)) =>

          val expectedArrivalNotification = arbArrivalNotification.copy(enRouteEvents = Some(Seq(enRouteEvent)))

          val containers = containerTranshipment.containers

          val userAnswers: UserAnswers = emptyUserAnswers
            .copy(id = expectedArrivalNotification.movementReferenceNumber)
            .set(GoodsLocationPage, BorderForceOffice).success.value
            .set(PresentationOfficePage, CustomsOffice(id = expectedArrivalNotification.presentationOfficeId, name = expectedArrivalNotification.presentationOfficeName, roles = Seq.empty, None)).success.value
            .set(CustomsSubPlacePage, expectedArrivalNotification.customsSubPlace).success.value
            .set(TraderNamePage, trader.name).success.value
            .set(TraderAddressPage, Address(buildingAndStreet = trader.streetAndNumber, city = trader.city, postcode = trader.postCode)).success.value
            .set(TraderEoriPage, trader.eori).success.value
            .set(IncidentOnRoutePage, true).success.value
            .set(PlaceOfNotificationPage, expectedArrivalNotification.notificationPlace).success.value
            .set(IsTranshipmentPage(eventIndex), true).success.value
            .set(EventPlacePage(eventIndex), enRouteEvent.place).success.value
            .set(EventCountryPage(eventIndex), enRouteEvent.country).success.value
            .set(EventReportedPage(eventIndex), enRouteEvent.alreadyInNcts).success.value
            .set(ContainersQuery(eventIndex), containers).success.value

          service.convertToArrivalNotification(userAnswers).value mustEqual expectedArrivalNotification
      }
    }

    "must return 'Normal Arrival Notification' message when there multiple incidents on route" in {
      forAll(arrivalNotificationWithSubplace, enRouteEventIncident, enRouteEventIncident) {
        case ((arbArrivalNotification, trader), (enRouteEvent1, incident1), (enRouteEvent2, incident2)) =>
          val routeEvent1: EnRouteEventDomain = enRouteEvent1
            .copy(seals = None)
            .copy(eventDetails = Some(incident1))

          val routeEvent2: EnRouteEventDomain = enRouteEvent2
            .copy(seals = None)
            .copy(eventDetails = Some(incident2))

          val arrivalNotification: NormalNotification = arbArrivalNotification.copy(enRouteEvents = Some(Seq(routeEvent1, routeEvent2)))

          val eventIndex2 = Index(1)

          val userAnswers: UserAnswers = createBasicUserAnswers(trader, arrivalNotification, isIncidentOnRoute = true)
            .set(IsTranshipmentPage(eventIndex), false).success.value
            .set(EventPlacePage(eventIndex), routeEvent1.place).success.value
            .set(EventCountryPage(eventIndex), routeEvent1.country).success.value
            .set(EventReportedPage(eventIndex), routeEvent1.alreadyInNcts).success.value
            .set(IsTranshipmentPage(eventIndex2), false).success.value
            .set(EventPlacePage(eventIndex2), routeEvent2.place).success.value
            .set(EventCountryPage(eventIndex2), routeEvent2.country).success.value
            .set(EventReportedPage(eventIndex2), routeEvent2.alreadyInNcts).success.value

          val updatedAnswers1 = incident1.incidentInformation.fold[UserAnswers](userAnswers) {
            _ =>
              userAnswers.set(IncidentInformationPage(eventIndex), incident1.incidentInformation.value).success.value
          }

          val updatedAnswers = incident2.incidentInformation.fold[UserAnswers](updatedAnswers1) {
            _ =>
              updatedAnswers1.set(IncidentInformationPage(eventIndex2), incident2.incidentInformation.value).success.value
          }

          service.convertToArrivalNotification(updatedAnswers).value mustEqual arrivalNotification
      }
    }

    "must return 'None' from empty userAnswers" in {
      service.convertToArrivalNotification(emptyUserAnswers) mustNot be(defined)
    }

    "must return 'None' from a partly filled userAnswers" in {
      forAll(arbitrary[NormalNotification], arbitrary[TraderDomain]) {
        case (arrivalNotification, trader) =>
          val userAnswers: UserAnswers =
            emptyUserAnswers
              .copy(id = arrivalNotification.movementReferenceNumber)
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

  private def createBasicUserAnswers(trader: TraderDomain, arrivalNotification: NormalNotification, isIncidentOnRoute: Boolean = false): UserAnswers =
    emptyUserAnswers
      .copy(id = arrivalNotification.movementReferenceNumber)
      .set(GoodsLocationPage, BorderForceOffice).success.value
      .set(PresentationOfficePage, CustomsOffice(id = arrivalNotification.presentationOfficeId, name = arrivalNotification.presentationOfficeName, roles = Seq.empty, None)).success.value
      .set(CustomsSubPlacePage, arrivalNotification.customsSubPlace).success.value
      .set(TraderNamePage, trader.name).success.value
      .set(TraderAddressPage, Address(buildingAndStreet = trader.streetAndNumber, city = trader.city, postcode = trader.postCode)).success.value
      .set(TraderEoriPage, trader.eori).success.value
      .set(IncidentOnRoutePage, isIncidentOnRoute).success.value
      .set(PlaceOfNotificationPage, arrivalNotification.notificationPlace).success.value
  // format: on
}
