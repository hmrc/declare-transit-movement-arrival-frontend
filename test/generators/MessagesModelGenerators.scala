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
import models.messages.ErrorType.{GenericError, MRNError}
import models.messages._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import utils.Format._

trait MessagesModelGenerators extends Generators {

  private val maxNumberOfSeals = 99
  val pastDate: LocalDate      = LocalDate.of(1900, 1, 1)
  val dateNow: LocalDate       = LocalDate.now

  implicit lazy val arbitraryProcedureType: Arbitrary[ProcedureType] =
    Arbitrary {
      Gen.oneOf(ProcedureType.Normal, ProcedureType.Simplified)
    }

  implicit lazy val arbitraryTrader: Arbitrary[Trader] =
    Arbitrary {

      for {
        eori            <- stringsWithMaxLength(Trader.Constants.eoriLength)
        name            <- stringsWithMaxLength(Trader.Constants.nameLength)
        streetAndNumber <- stringsWithMaxLength(Trader.Constants.streetAndNumberLength)
        postCode        <- stringsWithMaxLength(Trader.Constants.postCodeLength)
        city            <- stringsWithMaxLength(Trader.Constants.cityLength)
        countryCode     <- stringsWithMaxLength(Trader.Constants.countryCodeLength)
      } yield Trader(eori, name, streetAndNumber, postCode, city, countryCode)
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

        val sealsOpt = if (eventDetails.isDefined) Some(seals) else None

        EnRouteEvent(place, countryCode, alreadyInNcts, eventDetails, sealsOpt)
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

  lazy val generatorTrader: Gen[Trader] =
    for {
      eori            <- stringsWithMaxLength(Trader.Constants.eoriLength)
      name            <- stringsWithMaxLength(Trader.Constants.nameLength)
      streetAndNumber <- stringsWithMaxLength(Trader.Constants.streetAndNumberLength)
      postCode        <- stringsWithMaxLength(Trader.Constants.postCodeLength)
      city            <- stringsWithMaxLength(Trader.Constants.cityLength)
    } yield Trader(eori, name, streetAndNumber, postCode, city, "GB")

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

  implicit lazy val arbitraryMessageSender: Arbitrary[MessageSender] = {
    Arbitrary {
      for {
        environment <- Gen.oneOf(Seq("LOCAL", "QA", "STAGING", "PRODUCTION"))
        eori        <- stringsWithMaxLength(Trader.Constants.eoriLength)
      } yield MessageSender(environment, eori)
    }
  }

  implicit lazy val arbitraryInterchangeControlReference: Arbitrary[InterchangeControlReference] = {
    Arbitrary {
      for {
        date  <- localDateGen
        index <- Gen.posNum[Int]
      } yield InterchangeControlReference(dateFormatted(date), index)
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
        presentationOfficeName   <- stringsWithMaxLength(35)
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
        meta          <- arbitrary[Meta]
        header        <- arbitrary[Header].map(_.copy(notificationDate = meta.dateOfPreparation))
        trader        <- arbitrary[Trader]
        customsOffice <- arbitrary[CustomsOfficeOfPresentation].map(_.copy(presentationOffice = header.presentationOfficeId))
        enRouteEvents <- Gen.option(listWithMaxLength[EnRouteEvent](1))
      } yield ArrivalMovementRequest(meta, header, trader, customsOffice, enRouteEvents)
    }
  }

  implicit lazy val mrnErrorType: Arbitrary[MRNError] =
    Arbitrary {
      Gen.oneOf(ErrorType.mrnValues)
    }

  implicit lazy val genericErrorType: Arbitrary[GenericError] =
    Arbitrary {
      Gen.oneOf(ErrorType.genericValues)
    }

  implicit lazy val arbitraryErrorType: Arbitrary[ErrorType] =
    Arbitrary {
      for {
        genericError      <- arbitrary[GenericError]
        mrnRejectionError <- arbitrary[MRNError]
        errorType         <- Gen.oneOf(Seq(genericError, mrnRejectionError))
      } yield errorType
    }

  implicit lazy val arbitraryRejectionError: Arbitrary[FunctionalError] =
    Arbitrary {

      for {
        errorType     <- arbitrary[ErrorType]
        pointer       <- arbitrary[String]
        reason        <- arbitrary[Option[String]]
        originalValue <- arbitrary[Option[String]]
      } yield FunctionalError(errorType, ErrorPointer(pointer), reason, originalValue)
    }
}
