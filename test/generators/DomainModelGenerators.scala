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

  implicit lazy val arbitraryProcedureType: Arbitrary[ProcedureType] =
    Arbitrary {
      Gen.oneOf(ProcedureType.Normal, ProcedureType.Simplified)
    }

  implicit lazy val arbitraryTraderWithEori: Arbitrary[TraderWithEori] =
    Arbitrary {

      for {
        eori            <- stringsWithMaxLength(17)
        name            <- Gen.option(stringsWithMaxLength(35))
        streetAndNumber <- Gen.option(stringsWithMaxLength(35))
        postCode        <- Gen.option(stringsWithMaxLength(9))
        city            <- Gen.option(stringsWithMaxLength(35))
        countryCode     <- Gen.option(stringsWithMaxLength(2))
      } yield TraderWithEori(eori, name, streetAndNumber, postCode, city, countryCode)
    }

  implicit lazy val arbitraryTraderWithoutEori: Arbitrary[TraderWithoutEori] =
    Arbitrary {

      for {
        name            <- stringsWithMaxLength(35)
        streetAndNumber <- stringsWithMaxLength(35)
        postCode        <- stringsWithMaxLength(9)
        city            <- stringsWithMaxLength(35)
        countryCode     <- stringsWithMaxLength(2)
      } yield TraderWithoutEori(name, streetAndNumber, postCode, city, countryCode)
    }

  implicit lazy val arbitraryTrader: Arbitrary[Trader] =
    Arbitrary {
      Gen.oneOf(arbitrary[TraderWithEori], arbitrary[TraderWithoutEori])
    }

  implicit lazy val arbitraryIncident: Arbitrary[Incident] =
    Arbitrary {

      for {
        information          <- Gen.option(stringsWithMaxLength(350))
        endorsementDate      <- Gen.option(datesBetween(LocalDate.of(1900, 1, 1), LocalDate.now))
        endorsementAuthority <- Gen.option(stringsWithMaxLength(35))
        endorsementPlace     <- Gen.option(stringsWithMaxLength(35))
        endorsementCountry   <- Gen.option(stringsWithMaxLength(2))
      } yield Incident(information, endorsementDate, endorsementAuthority, endorsementPlace, endorsementCountry)
    }

  implicit lazy val arbitraryVehicularTranshipment: Arbitrary[VehicularTranshipment] =
    Arbitrary {

      for {
        transportIdentity    <- stringsWithMaxLength(27)
        transportCountry     <- stringsWithMaxLength(2)
        endorsementDate      <- Gen.option(datesBetween(LocalDate.of(1900, 1, 1), LocalDate.now))
        endorsementAuthority <- Gen.option(stringsWithMaxLength(35))
        endorsementPlace     <- Gen.option(stringsWithMaxLength(35))
        endorsementCountry   <- Gen.option(stringsWithMaxLength(2))
        numberOfContainers   <- Gen.choose[Int](1, 99)
        containers           <- Gen.option(Gen.listOfN(numberOfContainers, stringsWithMaxLength(17)))
      } yield
        VehicularTranshipment(transportIdentity, transportCountry, endorsementDate, endorsementAuthority, endorsementPlace, endorsementCountry, containers)
    }

  implicit lazy val arbitraryContainerTranshipment: Arbitrary[ContainerTranshipment] =
    Arbitrary {

      for {
        endorsementDate      <- Gen.option(datesBetween(LocalDate.of(1900, 1, 1), LocalDate.now))
        endorsementAuthority <- Gen.option(stringsWithMaxLength(35))
        endorsementPlace     <- Gen.option(stringsWithMaxLength(35))
        endorsementCountry   <- Gen.option(stringsWithMaxLength(2))
        numberOfContainers   <- Gen.choose[Int](1, 99)
        containers           <- Gen.listOfN(numberOfContainers, stringsWithMaxLength(17))
      } yield ContainerTranshipment(endorsementDate, endorsementAuthority, endorsementPlace, endorsementCountry, containers)
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
        place         <- stringsWithMaxLength(35)
        countryCode   <- stringsWithMaxLength(2)
        alreadyInNcts <- arbitrary[Boolean]
        eventDetails  <- arbitrary[EventDetails]
        numberOfSeals <- Gen.choose[Int](0, 99)
        seals         <- Gen.option(Gen.listOfN(numberOfSeals, stringsWithMaxLength(20)))
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
        place              <- stringsWithMaxLength(35)
        date               <- datesBetween(LocalDate.of(1900, 1, 1), LocalDate.now)
        subPlace           <- Gen.option(stringsWithMaxLength(17))
        trader             <- arbitrary[Trader]
        presentationOffice <- stringsWithMaxLength(8)
        events             <- Gen.option(seqWithMaxLength[EnRouteEvent](9))
      } yield NormalNotification(mrn, place, date, subPlace, trader, presentationOffice, events)
    }

  implicit lazy val arbitrarySimplifiedNotification: Arbitrary[SimplifiedNotification] =
    Arbitrary {

      for {
        mrn                <- arbitrary[MovementReferenceNumber].map(_.toString)
        place              <- stringsWithMaxLength(35)
        date               <- datesBetween(LocalDate.of(1900, 1, 1), LocalDate.now)
        approvedLocation   <- Gen.option(stringsWithMaxLength(17))
        trader             <- arbitrary[Trader]
        presentationOffice <- stringsWithMaxLength(8)
        events             <- Gen.option(seqWithMaxLength[EnRouteEvent](9))
      } yield SimplifiedNotification(mrn, place, date, approvedLocation, trader, presentationOffice, events)
    }

  implicit lazy val arbitraryArrivalNotification: Arbitrary[ArrivalNotification] =
    Arbitrary {

      Gen.oneOf(arbitrary[NormalNotification], arbitrary[SimplifiedNotification])
    }

  lazy val generatorTraderWithEoriAllValues: Gen[TraderWithEori] =
    for {
      eori            <- stringsWithMaxLength(17)
      name            <- stringsWithMaxLength(35)
      streetAndNumber <- stringsWithMaxLength(35)
      postCode        <- stringsWithMaxLength(9)
      city            <- stringsWithMaxLength(35)
    } yield TraderWithEori(eori, Some(name), Some(streetAndNumber), Some(postCode), Some(city), Some("GB"))

}
