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

package generators

import java.time.LocalDate

import models.MovementReferenceNumber
import models.domain._
import models.domain.messages.ArrivalNotification
import models.domain.messages.NormalNotification
import models.domain.messages.SimplifiedNotification
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Arbitrary
import org.scalacheck.Gen

trait DomainModelGenerators extends Generators {

  private val maxNumberOfSeals = 99

  implicit lazy val arbitraryProcedureType: Arbitrary[ProcedureType] =
    Arbitrary {
      Gen.oneOf(ProcedureType.Normal, ProcedureType.Simplified)
    }

  implicit lazy val arbitraryTraderWithEori: Arbitrary[TraderWithEori] =
    Arbitrary {

      for {
        eori            <- stringsWithMaxLength(TraderWithEori.Constants.eoriLength)
        name            <- Gen.option(stringsWithMaxLength(TraderWithEori.Constants.nameLength))
        streetAndNumber <- Gen.option(stringsWithMaxLength(TraderWithEori.Constants.streetAndNumberLength))
        postCode        <- Gen.option(stringsWithMaxLength(TraderWithEori.Constants.postCodeLength))
        city            <- Gen.option(stringsWithMaxLength(TraderWithEori.Constants.cityLength))
        countryCode     <- Gen.option(stringsWithMaxLength(TraderWithEori.Constants.countryCodeLength))
      } yield TraderWithEori(eori, name, streetAndNumber, postCode, city, countryCode)
    }

  implicit lazy val arbitraryTraderWithoutEori: Arbitrary[TraderWithoutEori] =
    Arbitrary {

      for {
        name            <- stringsWithMaxLength(TraderWithoutEori.Constants.nameLength)
        streetAndNumber <- stringsWithMaxLength(TraderWithoutEori.Constants.streetAndNumberLength)
        postCode        <- stringsWithMaxLength(TraderWithoutEori.Constants.postCodeLength)
        city            <- stringsWithMaxLength(TraderWithoutEori.Constants.cityLength)
        countryCode     <- stringsWithMaxLength(TraderWithoutEori.Constants.countryCodeLength)
      } yield TraderWithoutEori(name, streetAndNumber, postCode, city, countryCode)
    }

  implicit lazy val arbitraryTrader: Arbitrary[Trader] =
    Arbitrary {
      Gen.oneOf(arbitrary[TraderWithEori], arbitrary[TraderWithoutEori])
    }

  private val localDateGen: Gen[LocalDate] =
    datesBetween(LocalDate.of(1900, 1, 1), LocalDate.now)

  implicit lazy val arbitraryEndorsement: Arbitrary[Endorsement] =
    Arbitrary {

      for {
        date      <- Gen.option(localDateGen)
        authority <- Gen.option(stringsWithMaxLength(Endorsement.Constants.authorityLength))
        place     <- Gen.option(stringsWithMaxLength(Endorsement.Constants.placeLength))
        country   <- Gen.option(stringsWithMaxLength(Endorsement.Constants.countryLength))
      } yield Endorsement(date, authority, place, country)
    }

  implicit lazy val arbitraryIncident: Arbitrary[Incident] =
    Arbitrary {

      for {
        information <- Gen.option(stringsWithMaxLength(Incident.Constants.informationLength))
        endorsement <- arbitrary[Endorsement]
      } yield Incident(information, endorsement)
    }

  implicit lazy val arbitraryVehicularTranshipment: Arbitrary[VehicularTranshipment] =
    Arbitrary {

      for {

        transportIdentity <- stringsWithMaxLength(VehicularTranshipment.Constants.transportIdentityLength)
        transportCountry  <- stringsWithMaxLength(VehicularTranshipment.Constants.transportCountryLength)
        endorsement       <- arbitrary[Endorsement]
        containers        <- Gen.option(listWithMaxLength[Container](Transhipment.Constants.maxContainers))
      } yield VehicularTranshipment(transportIdentity, transportCountry, endorsement, containers)
    }

  implicit lazy val arbitraryContainer: Arbitrary[Container] =
    Arbitrary {
      for {
        container <- stringsWithMaxLength(Transhipment.Constants.containerLength).suchThat(_.length > 0)
      } yield Container(container)
    }

  implicit lazy val arbitraryContainers: Arbitrary[Seq[Container]] =
    Arbitrary(listWithMaxLength[Container](Transhipment.Constants.maxContainers))

  implicit lazy val arbitraryContainerTranshipment: Arbitrary[ContainerTranshipment] =
    Arbitrary {
      for {
        endorsement <- arbitrary[Endorsement]
        containers  <- listWithMaxLength[Container](Transhipment.Constants.maxContainers)
      } yield ContainerTranshipment(endorsement, containers)
    }

  implicit lazy val arbitraryTranshipment: Arbitrary[Transhipment] =
    Arbitrary {
      Gen.oneOf[Transhipment](
        arbitrary[VehicularTranshipment],
        arbitrary[ContainerTranshipment]
      )
    }

  implicit lazy val arbitraryEventDetails: Arbitrary[EventDetails] =
    Arbitrary {
      Gen.oneOf[EventDetails](
        arbitrary[Incident],
        arbitrary[Transhipment]
      )
    }

  implicit lazy val arbitraryEnRouteEvent: Arbitrary[EnRouteEvent] =
    Arbitrary {

      for {
        place         <- stringsWithMaxLength(EnRouteEvent.Constants.placeLength)
        countryCode   <- stringsWithMaxLength(EnRouteEvent.Constants.countryCodeLength)
        alreadyInNcts <- arbitrary[Boolean]
        eventDetails  <- arbitrary[EventDetails]
        seals         <- Gen.option(listWithMaxLength(maxNumberOfSeals)(Arbitrary(stringsWithMaxLength(EnRouteEvent.Constants.sealsLength))))
      } yield {

        val removeEmptySealsList = seals match {
          case Some(seals) if seals.nonEmpty => Some(seals)
          case _                             => None
        }

        EnRouteEvent(place, countryCode, alreadyInNcts, eventDetails, removeEmptySealsList)
      }
    }

  implicit lazy val arbitraryNormalNotification: Arbitrary[NormalNotification] =
    Arbitrary {

      for {
        mrn                <- arbitrary[MovementReferenceNumber].map(_.toString())
        place              <- stringsWithMaxLength(NormalNotification.Constants.notificationPlaceLength)
        date               <- localDateGen
        subPlace           <- Gen.option(stringsWithMaxLength(NormalNotification.Constants.customsSubPlaceLength))
        trader             <- arbitrary[Trader]
        presentationOffice <- stringsWithMaxLength(NormalNotification.Constants.presentationOfficeLength)
        events             <- Gen.option(listWithMaxLength[EnRouteEvent](NormalNotification.Constants.maxNumberOfEnRouteEvents))
      } yield NormalNotification(mrn, place, date, subPlace, trader, presentationOffice, events)
    }

  implicit lazy val arbitrarySimplifiedNotification: Arbitrary[SimplifiedNotification] =
    Arbitrary {

      for {
        mrn                <- arbitrary[MovementReferenceNumber].map(_.toString)
        place              <- stringsWithMaxLength(SimplifiedNotification.Constants.notificationPlaceLength)
        date               <- localDateGen
        approvedLocation   <- Gen.option(stringsWithMaxLength(SimplifiedNotification.Constants.approvedLocationLength))
        trader             <- arbitrary[Trader]
        presentationOffice <- stringsWithMaxLength(SimplifiedNotification.Constants.presentationOfficeLength)
        events             <- Gen.option(listWithMaxLength[EnRouteEvent](NormalNotification.Constants.maxNumberOfEnRouteEvents))
      } yield SimplifiedNotification(mrn, place, date, approvedLocation, trader, presentationOffice, events)
    }

  implicit lazy val arbitraryArrivalNotification: Arbitrary[ArrivalNotification] =
    Arbitrary {

      Gen.oneOf(arbitrary[NormalNotification], arbitrary[SimplifiedNotification])
    }

  lazy val generatorTraderWithEoriAllValues: Gen[TraderWithEori] =
    for {
      eori            <- stringsWithMaxLength(TraderWithEori.Constants.eoriLength)
      name            <- stringsWithMaxLength(TraderWithEori.Constants.nameLength)
      streetAndNumber <- stringsWithMaxLength(TraderWithEori.Constants.streetAndNumberLength)
      postCode        <- stringsWithMaxLength(TraderWithEori.Constants.postCodeLength)
      city            <- stringsWithMaxLength(TraderWithEori.Constants.cityLength)
    } yield TraderWithEori(eori, Some(name), Some(streetAndNumber), Some(postCode), Some(city), Some("GB"))

}
