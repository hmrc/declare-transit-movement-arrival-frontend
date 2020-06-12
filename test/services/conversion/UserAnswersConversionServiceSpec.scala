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
import pages.events.seals.SealIdentityPage
import pages.events.transhipments.{TransportIdentityPage, TransportNationalityPage}
import pages.events.{EventCountryPage, EventPlacePage, EventReportedPage, IncidentInformationPage, IsTranshipmentPage}
import queries.ContainersQuery

class UserAnswersConversionServiceSpec extends SpecBase with ScalaCheckPropertyChecks with MessagesModelGenerators {

  val userAnswersConversionService: UserAnswersConversionService = app.injector.instanceOf[UserAnswersConversionService]

  private val lastUpdated = LocalDateTime.now()

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
          val arrivalNotification: NormalNotification = arbArrivalNotification.copy(enRouteEvents = None)

          val result = userAnswersConversionService.convertToUserAnswers(arrivalNotification).value

          val userAnswers: UserAnswers =
            createBasicUserAnswers(trader, arrivalNotification, arrivalNotification.enRouteEvents.isDefined, result.lastUpdated)

          result mustBe userAnswers
      }
    }

    "must return 'UserAnswers' message when there is one incident on route" in {
      forAll(arrivalNotificationWithSubplace, enRouteEventIncident) {
        case ((arbArrivalNotification, trader), (enRouteEvent, incident)) =>
          val routeEvent: EnRouteEvent = enRouteEvent
            .copy(seals = None)
            .copy(eventDetails = Some(incident.copy(date = None, authority = None, place = None, country = None)))

          val arrivalNotification: NormalNotification = arbArrivalNotification.copy(enRouteEvents = Some(Seq(routeEvent)))

          val userAnswers: UserAnswers = createBasicUserAnswers(trader, arrivalNotification, isIncidentOnRoute = true, lastUpdated)
            .set(IsTranshipmentPage(eventIndex), false)
            .success
            .value
            .set(EventPlacePage(eventIndex), routeEvent.place)
            .success
            .value
            .set(EventCountryPage(eventIndex), Country("active", routeEvent.countryCode, "United Kingdom"))
            .success
            .value
            .set(EventReportedPage(eventIndex), routeEvent.alreadyInNcts)
            .success
            .value
            .set(IncidentInformationPage(eventIndex), incident.information.getOrElse(""))
            .success
            .value

          val result = userAnswersConversionService.convertToUserAnswers(arrivalNotification).value.copy(lastUpdated = lastUpdated)
          result mustBe userAnswers
      }
    }

    "must return 'UserAnswers' when there is one vehicle transhipment on route" in {
      forAll(arrivalNotificationWithSubplace, enRouteEventVehicularTranshipment) {
        case ((arbArrivalNotification, trader), (enRouteEvent, vehicularTranshipment)) =>
          val routeEvent: EnRouteEvent = enRouteEvent
            .copy(seals = None)
            .copy(eventDetails = Some(vehicularTranshipment.copy(date = None, authority = None, place = None, country = None, containers = None)))

          val arrivalNotification: NormalNotification = arbArrivalNotification.copy(enRouteEvents = Some(Seq(routeEvent)))
          val userAnswers: UserAnswers = createBasicUserAnswers(trader, arrivalNotification, isIncidentOnRoute = true, lastUpdated)
            .set(IsTranshipmentPage(eventIndex), true)
            .success
            .value
            .set(EventPlacePage(eventIndex), routeEvent.place)
            .success
            .value
            .set(EventCountryPage(eventIndex), Country("active", routeEvent.countryCode, "United Kingdom"))
            .success
            .value
            .set(EventReportedPage(eventIndex), routeEvent.alreadyInNcts)
            .success
            .value
            .set(TransportIdentityPage(eventIndex), vehicularTranshipment.transportIdentity)
            .success
            .value
            .set(TransportNationalityPage(eventIndex), Country("active", vehicularTranshipment.transportCountry, "United Kingdom"))
            .success
            .value

          val result = userAnswersConversionService.convertToUserAnswers(arrivalNotification).value.copy(lastUpdated = lastUpdated)
          result mustBe userAnswers
      }
    }

    "must return User Answers when there is one container transhipment on route" in {
      val enRouteEventContainerTranshipment: Gen[(EnRouteEvent, ContainerTranshipment)] = for {
        generatedEnRouteEvent <- arbitrary[EnRouteEvent]
        ct                    <- arbitrary[ContainerTranshipment]
      } yield {
        val containerTranshipment = ct.copy(date = None, authority = None, place = None, country = None)

        val enRouteEvent = generatedEnRouteEvent.copy(eventDetails = Some(containerTranshipment), seals = None)

        (enRouteEvent, containerTranshipment)
      }

      forAll(arrivalNotificationWithSubplace, enRouteEventContainerTranshipment) {
        case ((arbArrivalNotification, trader), (enRouteEvent, containerTranshipment)) =>
          val arrivalNotification: NormalNotification = arbArrivalNotification.copy(enRouteEvents = Some(Seq(enRouteEvent)))

          val containers: Seq[Container] = containerTranshipment.containers

          val userAnswers: UserAnswers = emptyUserAnswers
            .copy(lastUpdated = lastUpdated)
            .copy(id = arrivalNotification.movementReferenceNumber)
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
            .set(IncidentOnRoutePage, true)
            .success
            .value
            .set(PlaceOfNotificationPage, arrivalNotification.notificationPlace)
            .success
            .value
            .set(IsTranshipmentPage(eventIndex), true)
            .success
            .value
            .set(EventPlacePage(eventIndex), enRouteEvent.place)
            .success
            .value
            .set(EventCountryPage(eventIndex), Country("active", enRouteEvent.countryCode, "United Kingdom"))
            .success
            .value
            .set(EventReportedPage(eventIndex), enRouteEvent.alreadyInNcts)
            .success
            .value
            .set(ContainersQuery(eventIndex), containers)
            .success
            .value

          val result = userAnswersConversionService.convertToUserAnswers(arrivalNotification).value.copy(lastUpdated = lastUpdated)
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
