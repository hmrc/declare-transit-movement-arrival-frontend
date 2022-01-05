/*
 * Copyright 2022 HM Revenue & Customs
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

import derivable.DeriveNumberOfEvents
import models.GoodsLocation.{AuthorisedConsigneesLocation, BorderForceOffice}
import models.domain._
import models.reference.CountryCode
import models.{Index, UserAnswers}
import pages._
import pages.events._
import pages.events.transhipments._
import queries.{ContainersQuery, SealsQuery}

import java.time.LocalDate

class UserAnswersToArrivalNotificationDomain {

  val countryCode_GB = "GB"

  def convertToArrivalNotification(userAnswers: UserAnswers): Option[ArrivalNotificationDomain] =
    userAnswers.get(GoodsLocationPage) match {
      case Some(BorderForceOffice)            => createNormalNotification(userAnswers)
      case Some(AuthorisedConsigneesLocation) => createSimplifiedNotification(userAnswers)
      case _                                  => None
    }

  private def createSimplifiedNotification(userAnswers: UserAnswers): Option[SimplifiedNotification] =
    for {
      customsOffice      <- userAnswers.get(CustomsOfficePage)
      authorisedLocation <- userAnswers.get(AuthorisedLocationPage)
      tradersAddress     <- userAnswers.get(ConsigneeAddressPage)
      traderEori         <- userAnswers.get(ConsigneeEoriNumberPage)
      traderName         <- userAnswers.get(ConsigneeNamePage)
    } yield SimplifiedNotification(
      movementReferenceNumber = userAnswers.movementReferenceNumber,
      notificationPlace = tradersAddress.postcode,
      notificationDate = LocalDate.now(),
      authorisedLocation = authorisedLocation,
      trader = TraderDomain(
        eori = traderEori,
        name = traderName,
        streetAndNumber = tradersAddress.buildingAndStreet,
        postCode = tradersAddress.postcode,
        city = tradersAddress.city,
        countryCode = countryCode_GB
      ),
      customsOffice = customsOffice,
      enRouteEvents = enRouteEvents(userAnswers),
      authedEori = userAnswers.eoriNumber
    )

  private def createNormalNotification(userAnswers: UserAnswers): Option[NormalNotification] =
    for {
      customsOffice     <- userAnswers.get(CustomsOfficePage)
      customsSubPlace   <- userAnswers.get(CustomsSubPlacePage)
      tradersAddress    <- userAnswers.get(TraderAddressPage)
      traderEori        <- userAnswers.get(TraderEoriPage)
      traderName        <- userAnswers.get(TraderNamePage)
      notificationPlace <- userAnswers.get(PlaceOfNotificationPage) orElse Some(tradersAddress.postcode)
    } yield NormalNotification(
      movementReferenceNumber = userAnswers.movementReferenceNumber,
      notificationPlace = notificationPlace,
      notificationDate = LocalDate.now(),
      customsSubPlace = customsSubPlace,
      trader = TraderDomain(
        eori = traderEori,
        name = traderName,
        streetAndNumber = tradersAddress.buildingAndStreet,
        postCode = tradersAddress.postcode,
        city = tradersAddress.city,
        countryCode = countryCode_GB
      ),
      customsOffice = customsOffice,
      enRouteEvents = enRouteEvents(userAnswers)
    )

  private def eventDetails(
    incidentInformation: Option[String],
    transportIdentity: Option[String],
    transportCountry: Option[CountryCode],
    containers: Option[Seq[ContainerDomain]]
  ): EventDetailsDomain =
    (incidentInformation, transportIdentity, transportCountry, containers) match {
      case (None, Some(transportIdentity), Some(transportCountry), containers) =>
        VehicularTranshipmentDomain(
          transportIdentity = transportIdentity,
          transportCountry = transportCountry,
          containers = containers
        )
      case (None, None, None, Some(containers)) =>
        ContainerTranshipmentDomain(containers = containers)
      case (Some(incidentInformation), None, None, None) =>
        IncidentWithInformationDomain(incidentInformation)
      case _ => IncidentWithoutInformationDomain
    }

  private def enRouteEvents(userAnswers: UserAnswers): Option[Seq[EnRouteEventDomain]] =
    userAnswers.get(DeriveNumberOfEvents).map {
      numberOfEvents =>
        val listOfEvents = List.range(0, numberOfEvents).map(Index(_))
        listOfEvents.flatMap {
          eventIndex =>
            for {
              place      <- userAnswers.get(EventPlacePage(eventIndex))
              country    <- userAnswers.get(EventCountryPage(eventIndex))
              isReported <- userAnswers.get(EventReportedPage(eventIndex))
              incidentInformation = userAnswers.get(IncidentInformationPage(eventIndex))
              transportIdentity   = userAnswers.get(TransportIdentityPage(eventIndex))
              transportCountry    = userAnswers.get(TransportNationalityPage(eventIndex))
              containers          = userAnswers.get(ContainersQuery(eventIndex))
            } yield EnRouteEventDomain(
              place = place,
              country = country,
              alreadyInNcts = isReported,
              eventDetails = eventDetails(incidentInformation, transportIdentity, transportCountry, containers),
              seals = userAnswers.get(SealsQuery(eventIndex))
            )
        }
    }
}
