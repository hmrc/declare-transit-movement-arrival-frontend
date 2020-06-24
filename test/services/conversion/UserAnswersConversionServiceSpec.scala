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
import models.reference.{Country, CustomsOffice}
import models.{Address, Index, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._
import pages.events.transhipments.{TransportIdentityPage, TransportNationalityPage}
import pages.events._
import queries.{ContainersQuery, SealsQuery}

class UserAnswersConversionServiceSpec extends SpecBase with ScalaCheckPropertyChecks with MessagesModelGenerators {

  val userAnswersConversionService: UserAnswersConversionService = app.injector.instanceOf[UserAnswersConversionService]

  private val lastUpdated = LocalDateTime.now()

  private val arrivalNotificationWithSubplace: Gen[(NormalNotification, Trader)] =
    for {
      base              <- arbitrary[NormalNotification]
      trader            <- arbitrary[Trader]
      subPlace          <- stringsWithMaxLength(NormalNotification.Constants.customsSubPlaceLength)
      notificationPlace <- stringsWithMaxLength(NormalNotification.Constants.notificationPlaceLength)

    } yield {

      val expected: NormalNotification = base
        .copy(movementReferenceNumber = mrn)
        .copy(trader = trader)
        .copy(customsSubPlace = Some(subPlace))
        .copy(notificationDate = LocalDate.now())
        .copy(notificationPlace = Some(notificationPlace))
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

  private val enRouteEventContainerTranshipment: Gen[(EnRouteEvent, ContainerTranshipment)] = for {
    generatedEnRouteEvent <- arbitrary[EnRouteEvent]
    containerTranshipment <- arbitrary[ContainerTranshipment]
  } yield (generatedEnRouteEvent.copy(eventDetails = Some(containerTranshipment)), containerTranshipment)

  "UserAnswersConversionService" - {

    "must return 'User Answers' message when there are no EventDetails on route" in {
      forAll(arrivalNotificationWithSubplace) {
        case (arbArrivalNotification, trader) =>
          val arrivalNotification: NormalNotification = arbArrivalNotification.copy(enRouteEvents = None)

          val result = userAnswersConversionService.convertToUserAnswers(arrivalNotification).value

          val userAnswers: UserAnswers =
            createBasicUserAnswers(trader, arrivalNotification, arrivalNotification.enRouteEvents.isDefined, result.lastUpdated)

          result mustBe userAnswers
      }
    }

    "must return 'User Answers' message when there is one incident on route with seals" in {
      forAll(arrivalNotificationWithSubplace, enRouteEventIncident, arbitrary[Seal]) {
        case ((arbArrivalNotification, trader), (enRouteEvent, incident), seal) =>
          val routeEvent: EnRouteEvent = enRouteEvent
            .copy(seals = Some(Seq(seal)))
            .copy(eventDetails = Some(incident.copy(date = None, authority = None, place = None, country = None)))

          val arrivalNotification: NormalNotification = arbArrivalNotification.copy(enRouteEvents = Some(Seq(routeEvent)))

          // format: off
          val userAnswers: UserAnswers = createBasicUserAnswers(trader, arrivalNotification, isIncidentOnRoute = true, lastUpdated)
            .set(IsTranshipmentPage(eventIndex), false).success.value
            .set(EventPlacePage(eventIndex), routeEvent.place).success.value
            .set(EventCountryPage(eventIndex), Country("active", routeEvent.countryCode, "United Kingdom")).success.value
            .set(EventReportedPage(eventIndex), routeEvent.alreadyInNcts).success.value
            .set(IncidentInformationPage(eventIndex), incident.information.getOrElse("")).success.value
            .set(SealsQuery(eventIndex), Seq(seal)).success.value
          // format: on

          val result = userAnswersConversionService.convertToUserAnswers(arrivalNotification).value.copy(lastUpdated = lastUpdated)
          result mustBe userAnswers
      }
    }

    "must return 'User Answers' when there is one vehicle transhipment on route with seals" in {
      forAll(arrivalNotificationWithSubplace, enRouteEventVehicularTranshipment, arbitrary[Seal]) {
        case ((arbArrivalNotification, trader), (enRouteEvent, vehicularTranshipment), seal) =>
          val routeEvent: EnRouteEvent = enRouteEvent
            .copy(seals = Some(Seq(seal)))
            .copy(eventDetails = Some(vehicularTranshipment.copy(date = None, authority = None, place = None, country = None, containers = None)))

          val arrivalNotification: NormalNotification = arbArrivalNotification.copy(enRouteEvents = Some(Seq(routeEvent)))

          // format: off
          val userAnswers: UserAnswers = createBasicUserAnswers(trader, arrivalNotification, isIncidentOnRoute = true, lastUpdated)
            .set(IsTranshipmentPage(eventIndex), true).success.value
            .set(EventPlacePage(eventIndex), routeEvent.place).success.value
            .set(EventCountryPage(eventIndex), Country("active", routeEvent.countryCode, "United Kingdom")).success.value
            .set(EventReportedPage(eventIndex), routeEvent.alreadyInNcts).success.value
            .set(TransportIdentityPage(eventIndex), vehicularTranshipment.transportIdentity).success.value
            .set(TransportNationalityPage(eventIndex), Country("active", vehicularTranshipment.transportCountry, "United Kingdom")).success.value
            .set(SealsQuery(eventIndex), Seq(seal)).success.value
          // format: on

          val result = userAnswersConversionService.convertToUserAnswers(arrivalNotification).value.copy(lastUpdated = lastUpdated)
          result mustBe userAnswers
      }
    }

    "must return 'User Answers' when there is one container transhipment on route with seals" in {
      forAll(arrivalNotificationWithSubplace, enRouteEventContainerTranshipment, arbitrary[Container], arbitrary[Seal]) {
        case ((arbArrivalNotification, trader), (enRouteEvent, containerTranshipment), container, seal) =>
          val routeEvent: EnRouteEvent = enRouteEvent
            .copy(seals = Some(Seq(seal)))
            .copy(eventDetails = Some(containerTranshipment.copy(date = None, authority = None, place = None, country = None, containers = Seq(container))))

          val arrivalNotification: NormalNotification = arbArrivalNotification.copy(enRouteEvents = Some(Seq(routeEvent)))

          // format: off
          val userAnswers: UserAnswers = createBasicUserAnswers(trader, arrivalNotification, isIncidentOnRoute = true, lastUpdated)
            .set(IsTranshipmentPage(eventIndex), true).success.value
            .set(EventPlacePage(eventIndex), enRouteEvent.place).success.value
            .set(EventCountryPage(eventIndex), Country("active", enRouteEvent.countryCode, "United Kingdom")).success.value
            .set(EventReportedPage(eventIndex), enRouteEvent.alreadyInNcts).success.value
            .set(ContainersQuery(eventIndex), Seq(container)).success.value
            .set(SealsQuery(eventIndex), Seq(seal)).success.value
          // format: on

          val result = userAnswersConversionService.convertToUserAnswers(arrivalNotification).value.copy(lastUpdated = lastUpdated)
          result mustBe userAnswers
      }
    }

    "must return 'User Answers' when there multiple incidents on route" in {
      forAll(arrivalNotificationWithSubplace, enRouteEventIncident, enRouteEventIncident) {
        case ((arbArrivalNotification, trader), (enRouteEvent1, incident1), (enRouteEvent2, incident2)) =>
          val routeEvent1: EnRouteEvent = enRouteEvent1
            .copy(seals = None)
            .copy(eventDetails = Some(incident1.copy(date = None, authority = None, place = None, country = None)))

          val routeEvent2: EnRouteEvent = enRouteEvent2
            .copy(seals = None)
            .copy(eventDetails = Some(incident2.copy(date = None, authority = None, place = None, country = None)))

          val arrivalNotification: NormalNotification = arbArrivalNotification.copy(enRouteEvents = Some(Seq(routeEvent1, routeEvent2)))
          val eventIndex2                             = Index(1)

          // format: off
          val userAnswers: UserAnswers = createBasicUserAnswers(trader, arrivalNotification, isIncidentOnRoute = true, lastUpdated)
            .set(IsTranshipmentPage(eventIndex), false).success.value
            .set(EventPlacePage(eventIndex), routeEvent1.place).success.value
            .set(EventCountryPage(eventIndex), Country("active", routeEvent1.countryCode, "United Kingdom")).success.value
            .set(EventReportedPage(eventIndex), routeEvent1.alreadyInNcts).success.value
            .set(IsTranshipmentPage(eventIndex2), false).success.value
            .set(EventPlacePage(eventIndex2), routeEvent2.place).success.value
            .set(EventCountryPage(eventIndex2), Country("active", routeEvent2.countryCode, "United Kingdom")).success.value
            .set(EventReportedPage(eventIndex2), routeEvent2.alreadyInNcts).success.value
            .set(IncidentInformationPage(eventIndex), incident1.information.getOrElse("")).success.value
            .set(IncidentInformationPage(eventIndex2), incident2.information.getOrElse("")).success.value
          // format: on

          val result = userAnswersConversionService.convertToUserAnswers(arrivalNotification).value.copy(lastUpdated = lastUpdated)
          result mustBe userAnswers
      }
    }

    "must return 'User Answers' when there multiple vehicular transhipment incidents on route with seals" in {
      forAll(arrivalNotificationWithSubplace, enRouteEventVehicularTranshipment, enRouteEventVehicularTranshipment, arbitrary[Seal]) {
        case ((arbArrivalNotification, trader), (enRouteEvent1, vehicularTranshipment1), (enRouteEvent2, vehicularTranshipment2), seal) =>
          val routeEvent1: EnRouteEvent = enRouteEvent1
            .copy(seals = Some(Seq(seal)))
            .copy(eventDetails = Some(vehicularTranshipment1.copy(date = None, authority = None, place = None, country = None, containers = None)))

          val routeEvent2: EnRouteEvent = enRouteEvent2
            .copy(seals = Some(Seq(seal)))
            .copy(eventDetails = Some(vehicularTranshipment2.copy(date = None, authority = None, place = None, country = None, containers = None)))

          val eventIndex2 = Index(1)

          val arrivalNotification: NormalNotification = arbArrivalNotification.copy(enRouteEvents = Some(Seq(routeEvent1, routeEvent2)))

          // format: off
          val userAnswers: UserAnswers = createBasicUserAnswers(trader, arrivalNotification, isIncidentOnRoute = true, lastUpdated)
            .set(IsTranshipmentPage(eventIndex), true).success.value
            .set(EventPlacePage(eventIndex), routeEvent1.place).success.value
            .set(EventCountryPage(eventIndex), Country("active", routeEvent1.countryCode, "United Kingdom")).success.value
            .set(EventReportedPage(eventIndex), routeEvent1.alreadyInNcts).success.value
            .set(TransportIdentityPage(eventIndex), vehicularTranshipment1.transportIdentity).success.value
            .set(TransportNationalityPage(eventIndex), Country("active", vehicularTranshipment1.transportCountry, "United Kingdom")).success.value
            .set(SealsQuery(eventIndex), Seq(seal)).success.value
            .set(IsTranshipmentPage(eventIndex2), true).success.value
            .set(EventPlacePage(eventIndex2), routeEvent2.place).success.value
            .set(EventCountryPage(eventIndex2), Country("active", routeEvent2.countryCode, "United Kingdom")).success.value
            .set(EventReportedPage(eventIndex2), routeEvent2.alreadyInNcts).success.value
            .set(TransportIdentityPage(eventIndex2), vehicularTranshipment2.transportIdentity).success.value
            .set(TransportNationalityPage(eventIndex2), Country("active", vehicularTranshipment2.transportCountry, "United Kingdom")).success.value
            .set(SealsQuery(eventIndex2), Seq(seal)).success.value
          // format: on

          val result = userAnswersConversionService.convertToUserAnswers(arrivalNotification).value.copy(lastUpdated = lastUpdated)
          result mustBe userAnswers
      }
    }

    "must return 'User Answers' when there multiple container transhipment incidents on route with seals" in {
      forAll(arrivalNotificationWithSubplace, enRouteEventContainerTranshipment, enRouteEventContainerTranshipment, arbitrary[Container], arbitrary[Seal]) {
        case ((arbArrivalNotification, trader), (enRouteEvent1, containerTranshipment1), (enRouteEvent2, containerTranshipment2), container, seal) =>
          val eventIndex2 = Index(1)

          val routeEvent1: EnRouteEvent = enRouteEvent1
            .copy(seals = Some(Seq(seal)))
            .copy(eventDetails = Some(containerTranshipment1.copy(date = None, authority = None, place = None, country = None, containers = Seq(container))))

          val routeEvent2: EnRouteEvent = enRouteEvent2
            .copy(seals = Some(Seq(seal)))
            .copy(eventDetails = Some(containerTranshipment2.copy(date = None, authority = None, place = None, country = None, containers = Seq(container))))

          val arrivalNotification: NormalNotification = arbArrivalNotification.copy(enRouteEvents = Some(Seq(routeEvent1, routeEvent2)))

          // format: off
          val userAnswers: UserAnswers = createBasicUserAnswers(trader, arrivalNotification, isIncidentOnRoute = true, lastUpdated)
            .set(IsTranshipmentPage(eventIndex), true).success.value
            .set(EventPlacePage(eventIndex), enRouteEvent1.place).success.value
            .set(EventCountryPage(eventIndex), Country("active", enRouteEvent1.countryCode, "United Kingdom")).success.value
            .set(EventReportedPage(eventIndex), enRouteEvent1.alreadyInNcts).success.value
            .set(ContainersQuery(eventIndex), Seq(container)).success.value
            .set(SealsQuery(eventIndex), Seq(seal)).success.value
            .set(IsTranshipmentPage(eventIndex2), true).success.value
            .set(EventPlacePage(eventIndex2), enRouteEvent2.place).success.value
            .set(EventCountryPage(eventIndex2), Country("active", enRouteEvent2.countryCode, "United Kingdom")).success.value
            .set(EventReportedPage(eventIndex2), enRouteEvent2.alreadyInNcts).success.value
            .set(ContainersQuery(eventIndex2), Seq(container)).success.value
            .set(SealsQuery(eventIndex2), Seq(seal)).success.value
          // format: on

          val result = userAnswersConversionService.convertToUserAnswers(arrivalNotification).value.copy(lastUpdated = lastUpdated)
          result mustBe userAnswers
      }
    }
  }

  private def createBasicUserAnswers(trader: Trader,
                                     arrivalNotification: NormalNotification,
                                     isIncidentOnRoute: Boolean = false,
                                     timeStamp: LocalDateTime): UserAnswers =
    // format: off
    emptyUserAnswers
      .copy(id = arrivalNotification.movementReferenceNumber)
      .copy(lastUpdated = timeStamp)
      .set(GoodsLocationPage, BorderForceOffice).success.value
//      .set(
//        PresentationOfficePage,
//        CustomsOffice(id = arrivalNotification.presentationOfficeId, name = arrivalNotification.presentationOfficeName, roles = Seq.empty, None)
//      ).success.value
      .set(CustomsSubPlacePage, arrivalNotification.customsSubPlace.value).success.value
      .set(TraderNamePage, trader.name).success.value
      .set(TraderAddressPage, Address(buildingAndStreet = trader.streetAndNumber, city = trader.city, postcode = trader.postCode)).success.value
      .set(TraderEoriPage, trader.eori).success.value
      .set(IncidentOnRoutePage, isIncidentOnRoute).success.value
      .set(PlaceOfNotificationPage, arrivalNotification.notificationPlace.value).success.value
  // format: on
}
