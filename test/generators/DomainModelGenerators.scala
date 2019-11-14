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
import models.domain.messages.{ArrivalNotification, NormalNotification, SimplifiedNotification}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}

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

  implicit lazy val arbitraryEndorsement: Arbitrary[Endorsement] =
    Arbitrary {

      for {
        date      <- Gen.option(datesBetween(LocalDate.of(1900, 1, 1), LocalDate.now))
        authority <- Gen.option(stringsWithMaxLength(35))
        place     <- Gen.option(stringsWithMaxLength(35))
        country   <- Gen.option(stringsWithMaxLength(2))
      } yield Endorsement(date, authority, place, country)
    }

  implicit lazy val arbitraryIncident: Arbitrary[Incident] =
    Arbitrary {

      for {
        information <- Gen.option(stringsWithMaxLength(350))
        endorsement <- arbitrary[Endorsement]
      } yield Incident(information, endorsement)
    }

  implicit lazy val arbitraryVehicularTranshipment: Arbitrary[VehicularTranshipment] =
    Arbitrary {

      for {
        transportIdentity <- stringsWithMaxLength(27)
        transportCountry  <- stringsWithMaxLength(2)
        endorsement       <- arbitrary[Endorsement]
        containers        <- Gen.listOf(stringsWithMaxLength(17))
      } yield VehicularTranshipment(transportIdentity, transportCountry, endorsement, containers)
    }

  implicit lazy val arbitraryContainerTranshipment: Arbitrary[ContainerTranshipment] =
    Arbitrary {

      for {
        endorsement       <- arbitrary[Endorsement]
        containers        <- Gen.nonEmptyListOf(stringsWithMaxLength(17))
      } yield ContainerTranshipment(endorsement, containers)
    }

  implicit lazy val arbitraryTranshipment: Arbitrary[Transhipment] =
    Arbitrary{
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
        numberOfSeals <- Gen.choose[Int](0, 9999)
        seals         <- Gen.listOfN(numberOfSeals, stringsWithMaxLength(20))
      } yield EnRouteEvent(place, countryCode, alreadyInNcts, eventDetails, seals)
    }

  implicit lazy val arbitraryNormalNotification: Arbitrary[NormalNotification] =
    Arbitrary {

      for {
        mrn                <- arbitrary[MovementReferenceNumber].map(_.toString)
        place              <- stringsWithMaxLength(35)
        date               <- datesBetween(LocalDate.of(1900, 1, 1), LocalDate.now)
        subPlace           <- Gen.option(stringsWithMaxLength(17))
        trader             <- arbitrary[Trader]
        presentationOffice <- stringsWithMaxLength(8)
        events             <- arbitrary[Seq[EnRouteEvent]]
      } yield NormalNotification(mrn, place, date, subPlace, trader, presentationOffice, Nil) //TODO replace with events when we implement
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
        events             <- arbitrary[Seq[EnRouteEvent]]
      } yield SimplifiedNotification(mrn, place, date, approvedLocation, trader, presentationOffice, events)
    }

  implicit lazy val arbitraryArrivalNotification: Arbitrary[ArrivalNotification] =
    Arbitrary {

      Gen.oneOf(arbitrary[NormalNotification], arbitrary[SimplifiedNotification])
    }

  lazy val generatorTraderWithEoriAllValues: Gen[TraderWithEori] =
    for {
      eori            <- arbitrary[String]
      name            <- arbitrary[String]
      streetAndNumber <- arbitrary[String]
      postCode        <- arbitrary[String]
      city            <- arbitrary[String]
    } yield TraderWithEori(eori, Some(name), Some(streetAndNumber), Some(postCode), Some(city), Some("GB"))

}
