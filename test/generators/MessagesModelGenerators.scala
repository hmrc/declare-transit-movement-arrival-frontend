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

import java.time.{LocalDate, LocalTime}

import models._
import models.messages._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}

trait MessagesModelGenerators extends Generators {

  private val maxNumberOfSeals = 99
  val pastDate: LocalDate      = LocalDate.of(1900, 1, 1)
  val dateNow: LocalDate       = LocalDate.now

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

  implicit lazy val arbitraryIncident: Arbitrary[Incident] =
    Arbitrary {

      for {
        information <- Gen.option(stringsWithMaxLength(Incident.Constants.informationLength))
      } yield Incident(information)
    }

  implicit lazy val arbitraryVehicularTranshipment: Arbitrary[VehicularTranshipment] =
    Arbitrary {

      for {

        transportIdentity <- stringsWithMaxLength(VehicularTranshipment.Constants.transportIdentityLength)
        transportCountry  <- stringsWithMaxLength(VehicularTranshipment.Constants.transportCountryLength)
        containers        <- Gen.option(listWithMaxLength[Container](Transhipment.Constants.maxContainers))
      } yield VehicularTranshipment(transportIdentity = transportIdentity, transportCountry = transportCountry, containers = containers)
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
        containers <- listWithMaxLength[Container](Transhipment.Constants.maxContainers)
      } yield ContainerTranshipment(containers = containers)
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

  implicit lazy val arbitrarySeal: Arbitrary[Seal] =
    Arbitrary {
      for {
        seal <- stringsWithMaxLength(EnRouteEvent.Constants.sealsLength).suchThat(_.length > 0)
      } yield Seal(seal)
    }

  implicit lazy val arbitrarySeals: Arbitrary[Seq[Seal]] =
    Arbitrary(listWithMaxLength[Seal](maxNumberOfSeals))

  implicit lazy val arbitraryEnRouteEvent: Arbitrary[EnRouteEvent] =
    Arbitrary {

      for {
        place         <- stringsWithMaxLength(EnRouteEvent.Constants.placeLength)
        countryCode   <- stringsWithMaxLength(EnRouteEvent.Constants.countryCodeLength)
        alreadyInNcts <- arbitrary[Boolean]
        eventDetails  <- arbitrary[Option[EventDetails]]
        seals         <- listWithMaxLength[Seal](1)
      } yield {

        val removeEmptySealsList = seals match {
          case seals if seals.nonEmpty => Some(seals)
          case _                       => None
        }

        EnRouteEvent(place, countryCode, alreadyInNcts, eventDetails, removeEmptySealsList)
      }
    }

  implicit lazy val arbitraryNormalNotification: Arbitrary[NormalNotification] =
    Arbitrary {

      for {
        mrn                    <- arbitrary[MovementReferenceNumber]
        place                  <- stringsWithMaxLength(NormalNotification.Constants.notificationPlaceLength)
        date                   <- localDateGen
        subPlace               <- Gen.option(stringsWithMaxLength(NormalNotification.Constants.customsSubPlaceLength))
        trader                 <- arbitrary[Trader]
        presentationOfficeId   <- stringsWithMaxLength(NormalNotification.Constants.presentationOfficeLength)
        presentationOfficeName <- arbitrary[String]
        events                 <- Gen.option(listWithMaxLength[EnRouteEvent](NormalNotification.Constants.maxNumberOfEnRouteEvents))
      } yield NormalNotification(mrn, place, date, subPlace, trader, presentationOfficeId, presentationOfficeName, events)
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

  implicit lazy val arbitraryGoodsReleaseNotification: Arbitrary[GoodsReleaseNotificationMessage] =
    Arbitrary {

      for {
        mrn                <- arbitrary[MovementReferenceNumber].map(_.toString())
        releaseDate        <- datesBetween(pastDate, dateNow)
        trader             <- arbitrary[Trader]
        presentationOffice <- stringsWithMaxLength(GoodsReleaseNotificationMessage.Constants.presentationOfficeLength)
      } yield models.messages.GoodsReleaseNotificationMessage(mrn, releaseDate, trader, presentationOffice)
    }

  implicit lazy val arbitraryArrivalNotificationRejection: Arbitrary[ArrivalNotificationRejectionMessage] =
    Arbitrary {

      for {
        mrn    <- arbitrary[MovementReferenceNumber].map(_.toString())
        date   <- datesBetween(pastDate, dateNow)
        action <- arbitrary[Option[String]]
        reason <- arbitrary[Option[String]]
        errors <- listWithMaxLength[FunctionalError](5)
      } yield ArrivalNotificationRejectionMessage(mrn, date, action, reason, errors)
    }

  implicit lazy val arbitraryCustomsOfficeOfPresentation: Arbitrary[CustomsOfficeOfPresentation] = {
    Arbitrary {

      for {
        presentationOffice <- stringsWithMaxLength(CustomsOfficeOfPresentation.Constants.presentationOfficeLength)
      } yield CustomsOfficeOfPresentation(presentationOffice)
    }
  }

  implicit lazy val arbitraryTraderDestination: Arbitrary[TraderDestination] = {
    Arbitrary {

      for {
        name            <- Gen.option(stringsWithMaxLength(TraderDestination.Constants.nameLength))
        streetAndNumber <- Gen.option(stringsWithMaxLength(TraderDestination.Constants.streetAndNumberLength))
        postCode        <- Gen.option(stringsWithMaxLength(TraderDestination.Constants.postCodeLength))
        city            <- Gen.option(stringsWithMaxLength(TraderDestination.Constants.cityLength))
        countryCode     <- Gen.option(stringsWithMaxLength(TraderDestination.Constants.countryCodeLength))
        eori            <- Gen.option(stringsWithMaxLength(TraderDestination.Constants.eoriLength))
      } yield TraderDestination(name, streetAndNumber, postCode, city, countryCode, eori)

    }
  }

  implicit lazy val arbitraryMessageSender: Arbitrary[MessageSender] = {
    Arbitrary {
      for {
        environment <- Gen.oneOf(Seq("LOCAL", "QA", "STAGING", "PRODUCTION"))
        eori        <- arbitrary[String]
      } yield MessageSender(environment, eori)
    }
  }

  implicit lazy val arbitraryInterchangeControlReference: Arbitrary[InterchangeControlReference] = {
    Arbitrary {
      for {
        dateTime <- arbitrary[String]
        index    <- arbitrary[Int]
      } yield InterchangeControlReference(dateTime, index)
    }
  }

  implicit lazy val arbitraryMeta: Arbitrary[Meta] = {
    Arbitrary {
      for {
        messageSender               <- arbitrary[MessageSender]
        interchangeControlReference <- arbitrary[InterchangeControlReference]
        date                        <- arbitrary[LocalDate]
        time                        <- arbitrary[LocalTime]
      } yield
        Meta(
          messageSender,
          interchangeControlReference,
          date,
          time,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None
        )
    }
  }

  implicit lazy val arbitraryProcedureTypeFlag: Arbitrary[ProcedureTypeFlag] = {
    Arbitrary {
      for {
        procedureType <- Gen.oneOf(Seq(SimplifiedProcedureFlag, NormalProcedureFlag))
      } yield procedureType
    }
  }

  implicit lazy val arbitraryHeader: Arbitrary[Header] = {
    Arbitrary {
      for {
        movementReferenceNumber  <- arbitrary[MovementReferenceNumber].map(_.toString())
        customsSubPlace          <- Gen.option(stringsWithMaxLength(Header.Constants.customsSubPlaceLength))
        arrivalNotificationPlace <- stringsWithMaxLength(Header.Constants.arrivalNotificationPlaceLength)
        procedureTypeFlag        <- arbitrary[ProcedureTypeFlag]
        notificationDate         <- arbitrary[LocalDate]
        presentationOfficeId     <- stringsWithMaxLength(CustomsOfficeOfPresentation.Constants.presentationOfficeLength)
        presentationOfficeName   <- nonEmptyString
      } yield
        Header(movementReferenceNumber,
               customsSubPlace,
               arrivalNotificationPlace,
               presentationOfficeId,
               presentationOfficeName,
               None,
               procedureTypeFlag,
               notificationDate)
    }
  }

  implicit lazy val arbitraryArrivalMovementRequest: Arbitrary[ArrivalMovementRequest] = {
    Arbitrary {
      for {
        meta              <- arbitrary[Meta]
        header            <- arbitrary[Header].map(_.copy(notificationDate = meta.dateOfPreparation))
        traderDestination <- arbitrary[TraderDestination]
        customsOffice     <- arbitrary[CustomsOfficeOfPresentation].map(_.copy(presentationOffice = header.presentationOfficeId))
        enRouteEvents     <- Gen.option(listWithMaxLength[EnRouteEvent](1))
      } yield ArrivalMovementRequest(meta, header, traderDestination, customsOffice, enRouteEvents)
    }
  }

  val arbitraryArrivalMovementRequestWithEori: Gen[ArrivalMovementRequest] = {
    for {
      arrivalNotificationRequest <- arbitraryArrivalMovementRequest.arbitrary
      traderWithEori             <- arbitrary[TraderWithEori]

    } yield {

      val newHeader = arrivalNotificationRequest.header.copy(procedureTypeFlag = NormalProcedureFlag)

      arrivalNotificationRequest
        .copy(header = newHeader)
        .copy(
          traderDestination = TraderDestination(
            traderWithEori.name,
            traderWithEori.streetAndNumber,
            traderWithEori.postCode,
            traderWithEori.city,
            traderWithEori.countryCode,
            Some(traderWithEori.eori)
          ))
    }
  }

  val arbitraryArrivalMovementRequestWithoutEori: Gen[ArrivalMovementRequest] = {
    for {
      arrivalNotificationRequest <- arbitraryArrivalMovementRequestWithEori
      traderWithEori             <- arbitrary[TraderWithoutEori]
    } yield {

      arrivalNotificationRequest.copy(traderDestination = {
        TraderDestination(
          Some(traderWithEori.name),
          Some(traderWithEori.streetAndNumber),
          Some(traderWithEori.postCode),
          Some(traderWithEori.city),
          Some(traderWithEori.countryCode),
          None
        )
      })

    }
  }

  implicit lazy val arbitraryRejectionError: Arbitrary[FunctionalError] =
    Arbitrary {

      for {
        errorType     <- arbitrary[Int]
        pointer       <- arbitrary[String]
        reason        <- arbitrary[Option[String]]
        originalValue <- arbitrary[Option[String]]
      } yield FunctionalError(ErrorType(errorType), ErrorPointer(pointer), reason, originalValue)
    }
}
