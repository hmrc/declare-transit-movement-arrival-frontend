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

import derivable.DeriveNumberOfEvents
import models.messages._
import models.{Index, TraderAddress, UserAnswers}
import pages._
import pages.events._
import pages.events.transhipments._
import queries.{ContainersQuery, SealsQuery}

class ArrivalNotificationConversionService {

  val countryCode_GB = "GB"

  def convertToArrivalNotification(userAnswers: UserAnswers): Option[ArrivalNotification] =
    for {
      presentationOffice <- userAnswers.get(PresentationOfficePage)
      customsSubPlace    <- userAnswers.get(CustomsSubPlacePage)
      tradersAddress     <- userAnswers.get(TraderAddressPage)
      traderEori         <- userAnswers.get(TraderEoriPage)
      traderName         <- userAnswers.get(TraderNamePage)
      notificationPlace  <- userAnswers.get(PlaceOfNotificationPage) orElse Some(tradersAddress.postcode)
    } yield {
      NormalNotification(
        movementReferenceNumber = userAnswers.id,
        notificationPlace       = notificationPlace,
        notificationDate        = LocalDate.now(),
        customsSubPlace         = Some(customsSubPlace),
        trader                  = traderAddress(tradersAddress, traderEori, traderName),
        presentationOffice      = presentationOffice.id,
        enRouteEvents           = enRouteEvents(userAnswers)
      )
    }

  private def eventDetails(
    incidentInformation: Option[String],
    transportIdentity: Option[String],
    transportCountry: Option[String],
    containers: Option[Seq[Container]]
  ): EventDetails = {
    val endorsement = Endorsement(None, None, None, None) // TODO: Find out where this data comes from

    (incidentInformation, transportIdentity, transportCountry, containers) match {
      case (ii, None, None, None) =>
        Incident(ii, endorsement)
      case (None, Some(ti), Some(tc), _) =>
        VehicularTranshipment(
          transportIdentity = ti,
          transportCountry  = tc,
          endorsement       = endorsement,
          containers        = containers
        )
      case (None, None, None, Some(containers)) =>
        ContainerTranshipment(endorsement, containers)
      case _ => ???
    }
  }

  private def enRouteEvents(userAnswers: UserAnswers): Option[Seq[EnRouteEvent]] =
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
            } yield {
              EnRouteEvent(
                place         = place,
                countryCode   = country.code,
                alreadyInNcts = isReported,
                eventDetails  = eventDetails(incidentInformation, transportIdentity, transportCountry.map(_.code), containers),
                seals         = userAnswers.get(SealsQuery(eventIndex))
              )
            }
        }
    }

  // TODO: Move this to the Trader model as a constructor?
  private def traderAddress(traderAddress: TraderAddress, traderEori: String, traderName: String): TraderWithEori =
    TraderWithEori(
      eori            = traderEori,
      name            = Some(traderName),
      streetAndNumber = Some(traderAddress.buildingAndStreet),
      postCode        = Some(traderAddress.postcode),
      city            = Some(traderAddress.city),
      countryCode     = Some(countryCode_GB)
    )
}
