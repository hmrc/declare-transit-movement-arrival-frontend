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

package generators

import models.domain._
import models.messages.ErrorType.{GenericError, MRNError}
import models.messages._
import models.reference.{CountryCode, CustomsOffice}
import models.{domain, messages, EoriNumber, MovementReferenceNumber, NormalProcedureFlag, ProcedureTypeFlag, SimplifiedProcedureFlag}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import utils.Format._

import java.time.{LocalDate, LocalTime}

trait MessagesModelGenerators extends Generators {

  implicit val arbValidString: Arbitrary[String] = Arbitrary(Gen.alphaNumStr)

  private val gbCountryCode = "GB"

  private val maxNumberOfSeals = 99
  val pastDate: LocalDate      = LocalDate.of(1900, 1, 1)
  val dateNow: LocalDate       = LocalDate.now

  implicit lazy val arbitraryProcedureType: Arbitrary[ProcedureType] =
    Arbitrary {
      Gen.oneOf(ProcedureType.Normal, ProcedureType.Simplified)
    }

  implicit lazy val arbitraryDomainTrader: Arbitrary[domain.TraderDomain] =
    Arbitrary {

      for {
        eori            <- stringsWithMaxLength(domain.TraderDomain.Constants.eoriLength)
        name            <- stringsWithMaxLength(domain.TraderDomain.Constants.nameLength)
        streetAndNumber <- stringsWithMaxLength(domain.TraderDomain.Constants.streetAndNumberLength)
        postCode        <- stringsWithMaxLength(domain.TraderDomain.Constants.postCodeLength)
        city            <- stringsWithMaxLength(domain.TraderDomain.Constants.cityLength)
      } yield domain.TraderDomain(name, streetAndNumber, city, postCode, gbCountryCode, eori)
    }

  implicit lazy val arbitraryMessagesTrader: Arbitrary[messages.Trader] =
    Arbitrary {

      for {
        eori            <- stringsWithMaxLength(domain.TraderDomain.Constants.eoriLength)
        name            <- stringsWithMaxLength(domain.TraderDomain.Constants.nameLength)
        streetAndNumber <- stringsWithMaxLength(domain.TraderDomain.Constants.streetAndNumberLength)
        postCode        <- stringsWithMaxLength(domain.TraderDomain.Constants.postCodeLength)
        city            <- stringsWithMaxLength(domain.TraderDomain.Constants.cityLength)
      } yield messages.Trader(name, streetAndNumber, city, postCode, gbCountryCode, eori)
    }

  private val localDateGen: Gen[LocalDate] =
    datesBetween(LocalDate.of(1900, 1, 1), LocalDate.now)

  implicit lazy val arbitraryIncidentWithInformationDomain: Arbitrary[IncidentWithInformationDomain] =
    Arbitrary {
      for {
        information <- stringsWithMaxLength(IncidentWithInformation.Constants.informationLength)
      } yield IncidentWithInformationDomain(information)
    }

  implicit lazy val arbitraryIncidentWithoutInformationDomain: Arbitrary[IncidentWithoutInformationDomain.type] =
    Arbitrary {
      IncidentWithoutInformationDomain
    }

  implicit lazy val arbitraryVehicularTranshipmentDomain: Arbitrary[VehicularTranshipmentDomain] =
    Arbitrary {

      for {

        transportIdentity <- stringsWithMaxLength(VehicularTranshipment.Constants.transportIdentityLength)
        transportCountry  <- arbitrary[CountryCode]
        containers        <- Gen.option(listWithMaxLength[ContainerDomain](2))
      } yield VehicularTranshipmentDomain(transportIdentity = transportIdentity, transportCountry = transportCountry, containers = containers)
    }

  implicit lazy val arbitraryVehicularTranshipment: Arbitrary[VehicularTranshipment] =
    Arbitrary {

      for {

        transportIdentity <- stringsWithMaxLength(VehicularTranshipment.Constants.transportIdentityLength)
        transportCountry  <- arbitrary[CountryCode]
        containers        <- Gen.option(listWithMaxLength[Container](2))
      } yield VehicularTranshipment(transportIdentity = transportIdentity, transportCountry = transportCountry, containers = containers)
    }

  implicit lazy val arbitraryContainer: Arbitrary[Container] =
    Arbitrary {
      for {
        containerNumber <- stringsWithMaxLength(Transhipment.Constants.containerLength).suchThat(_.length > 0)
      } yield Container(containerNumber)
    }

  implicit lazy val arbitraryContainerDomain: Arbitrary[ContainerDomain] =
    Arbitrary {
      for {
        containerNumber <- stringsWithMaxLength(Transhipment.Constants.containerLength).suchThat(_.length > 0)
      } yield ContainerDomain(containerNumber)
    }

  implicit lazy val arbitraryContainers: Arbitrary[Seq[Container]] =
    Arbitrary(listWithMaxLength[Container](2))

  implicit lazy val arbitraryContainersDomain: Arbitrary[Seq[ContainerDomain]] =
    Arbitrary(listWithMaxLength[ContainerDomain](2))

  implicit lazy val arbitraryContainerTranshipment: Arbitrary[ContainerTranshipment] =
    Arbitrary {
      for {
        containers <- listWithMaxLength[Container](2)
      } yield ContainerTranshipment(containers = containers)
    }

  implicit lazy val arbitraryContainerTranshipmentDomain: Arbitrary[ContainerTranshipmentDomain] =
    Arbitrary {
      for {
        containers <- listWithMaxLength[ContainerDomain](2)
      } yield ContainerTranshipmentDomain(containers = containers)
    }

  implicit lazy val arbitraryTranshipment: Arbitrary[Transhipment] =
    Arbitrary {
      Gen.oneOf[Transhipment](
        arbitrary[VehicularTranshipment],
        arbitrary[ContainerTranshipment]
      )
    }

  implicit lazy val arbitraryTranshipmentDomain: Arbitrary[TranshipmentDomain] =
    Arbitrary {
      Gen.oneOf[TranshipmentDomain](
        arbitrary[VehicularTranshipmentDomain],
        arbitrary[ContainerTranshipmentDomain]
      )
    }

  implicit lazy val incidentWithInformation: Arbitrary[IncidentWithInformation] =
    Arbitrary {
      for {
        information <- stringsWithMaxLength(IncidentWithInformation.Constants.informationLength)
      } yield IncidentWithInformation(information)
    }

  implicit lazy val incidentWithoutInformation: Arbitrary[IncidentWithoutInformation] =
    Arbitrary {
      IncidentWithoutInformation()
    }

  implicit lazy val arbitraryEventDetails: Arbitrary[EventDetails] =
    Arbitrary {
      Gen.oneOf[EventDetails](
        arbitrary[IncidentWithInformation],
        arbitrary[Transhipment]
      )
    }

  implicit lazy val arbitraryEventDetailsDomain: Arbitrary[EventDetailsDomain] =
    Arbitrary {
      Gen.oneOf[EventDetailsDomain](
        arbitrary[IncidentWithInformationDomain],
        arbitrary[TranshipmentDomain]
      )
    }

  implicit lazy val arbitrarySeal: Arbitrary[Seal] =
    Arbitrary {
      for {
        seal <- stringsWithMaxLength(EnRouteEvent.Constants.sealsLength).suchThat(_.length > 0)
      } yield Seal(seal)
    }

  implicit lazy val arbitrarySealDomain: Arbitrary[SealDomain] =
    Arbitrary {
      for {
        sealNumber <- stringsWithMaxLength(EnRouteEvent.Constants.sealsLength).suchThat(_.length > 0)
      } yield SealDomain(sealNumber)
    }

  implicit lazy val arbitrarySeals: Arbitrary[Seq[Seal]] =
    Arbitrary(listWithMaxLength[Seal](maxNumberOfSeals))

  implicit lazy val arbitraryEnRouteEvent: Arbitrary[EnRouteEvent] =
    Arbitrary {

      for {
        place         <- stringsWithMaxLength(EnRouteEvent.Constants.placeLength)
        countryCode   <- arbitrary[CountryCode]
        alreadyInNcts <- arbitrary[Boolean]
        eventDetails  <- arbitrary[EventDetails]
        seals         <- Gen.option(listWithMaxLength[Seal](1))
      } yield EnRouteEvent(place, countryCode, alreadyInNcts, eventDetails, seals)
    }

  implicit lazy val arbitraryDomainEnRouteEvent: Arbitrary[EnRouteEventDomain] =
    Arbitrary {

      for {
        place         <- stringsWithMaxLength(EnRouteEvent.Constants.placeLength)
        country       <- arbitrary[CountryCode]
        alreadyInNcts <- arbitrary[Boolean]
        eventDetails  <- arbitrary[EventDetailsDomain]
        seals         <- Gen.option(listWithMaxLength[SealDomain](1))
      } yield EnRouteEventDomain(place, country, alreadyInNcts, eventDetails, seals)
    }

  implicit lazy val arbitraryNormalNotification: Arbitrary[NormalNotification] =
    Arbitrary {

      for {
        mrn           <- arbitrary[MovementReferenceNumber]
        place         <- stringsWithMaxLength(NormalNotification.Constants.notificationPlaceLength)
        date          <- localDateGen
        subPlace      <- stringsWithMaxLength(NormalNotification.Constants.customsSubPlaceLength)
        trader        <- arbitrary[domain.TraderDomain]
        customsOffice <- arbitrary[CustomsOffice]
        events        <- Gen.option(listWithMaxLength[EnRouteEventDomain](NormalNotification.Constants.maxNumberOfEnRouteEvents))
      } yield domain.NormalNotification(mrn, place, date, subPlace, trader, customsOffice, events)
    }

  implicit lazy val arbitrarySimplifiedNotification: Arbitrary[SimplifiedNotification] =
    Arbitrary {

      for {
        mrn              <- arbitrary[MovementReferenceNumber]
        date             <- localDateGen
        approvedLocation <- stringsWithMaxLength(SimplifiedNotification.Constants.approvedLocationLength)
        trader           <- arbitrary[TraderDomain]
        customsOffice    <- arbitrary[CustomsOffice]
        events           <- Gen.option(listWithMaxLength[EnRouteEventDomain](NormalNotification.Constants.maxNumberOfEnRouteEvents))
        authedEoriNumber <- arbitrary[EoriNumber]
      } yield SimplifiedNotification(mrn, trader.postCode, date, approvedLocation, trader, customsOffice, events, authedEoriNumber)
    }

  implicit lazy val arbitraryArrivalNotification: Arbitrary[ArrivalNotificationDomain] =
    Arbitrary {
      Gen.oneOf(arbitrary[NormalNotification], arbitrary[SimplifiedNotification])
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

  implicit lazy val arbitraryCustomsOfficeOfPresentation: Arbitrary[CustomsOfficeOfPresentation] =
    Arbitrary {
      for {
        customsOffice <- stringsWithMaxLength(CustomsOfficeOfPresentation.Constants.customsOfficeLength)
      } yield CustomsOfficeOfPresentation(customsOffice)
    }

  implicit lazy val arbitraryInterchangeControlReference: Arbitrary[InterchangeControlReference] =
    Arbitrary {
      for {
        date  <- localDateGen
        index <- Gen.posNum[Int]
      } yield InterchangeControlReference(dateFormatted(date), index)
    }

  implicit lazy val arbitraryMeta: Arbitrary[Meta] =
    Arbitrary {
      for {
        interchangeControlReference <- arbitrary[InterchangeControlReference]
        date                        <- arbitrary[LocalDate]
        time                        <- arbitrary[LocalTime]
      } yield Meta(
        interchangeControlReference,
        date,
        LocalTime.of(time.getHour, time.getMinute),
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

  implicit lazy val arbitraryProcedureTypeFlag: Arbitrary[ProcedureTypeFlag] =
    Arbitrary {
      for {
        procedureType <- Gen.oneOf(Seq(SimplifiedProcedureFlag, NormalProcedureFlag))
      } yield procedureType
    }

  implicit lazy val arbitraryHeader: Arbitrary[Header] =
    Arbitrary {
      for {
        movementReferenceNumber          <- arbitrary[MovementReferenceNumber].map(_.toString())
        arrivalNotificationPlace         <- stringsWithMaxLength(Header.Constants.arrivalNotificationPlaceLength)
        procedureTypeFlag                <- arbitrary[ProcedureTypeFlag]
        customsSubPlace                  <- stringsWithMaxLength(Header.Constants.customsSubPlaceLength)
        arrivalAuthorisedLocationOfGoods <- stringsWithMaxLength(Header.Constants.arrivalAuthorisedLocationOfGoodsLength)
        notificationDate                 <- arbitrary[LocalDate]
      } yield {

        val customsSubPlaceToggle = if (procedureTypeFlag == NormalProcedureFlag) Some(customsSubPlace) else None
        val authLocation          = if (procedureTypeFlag == SimplifiedProcedureFlag) Some(arrivalAuthorisedLocationOfGoods) else None

        Header(
          movementReferenceNumber = movementReferenceNumber,
          customsSubPlace = customsSubPlaceToggle,
          arrivalNotificationPlace = arrivalNotificationPlace,
          arrivalAuthorisedLocationOfGoods = authLocation,
          procedureTypeFlag = procedureTypeFlag,
          notificationDate = notificationDate
        )
      }
    }

  implicit lazy val arbitraryArrivalMovementRequest: Arbitrary[ArrivalMovementRequest] =
    Arbitrary {
      for {
        eori <- arbitrary[EoriNumber]
        meta <- arbitrary[Meta]
        header <- arbitrary[Header].map(
          header => header.copy(notificationDate = meta.dateOfPreparation)
        )
        trader        <- arbitrary[Trader]
        customsOffice <- arbitrary[CustomsOfficeOfPresentation]
        enRouteEvents <- Gen.option(listWithMaxLength[EnRouteEvent](1))
      } yield {
        val traderWithEori = trader.copy(eori = eori.value)
        val updatedHeader  = if (header.procedureTypeFlag == SimplifiedProcedureFlag) header.copy(arrivalNotificationPlace = trader.postCode) else header

        ArrivalMovementRequest(meta, updatedHeader, traderWithEori, customsOffice, enRouteEvents)
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
        errorType <- Gen.oneOf(arbitrary[GenericError], arbitrary[MRNError])
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

  val arrivalNotificationWithSubplace: Gen[(NormalNotification, domain.TraderDomain)] =
    for {
      base     <- arbitrary[NormalNotification]
      trader   <- arbitrary[domain.TraderDomain]
      mrn      <- arbitrary[MovementReferenceNumber]
      subPlace <- stringsWithMaxLength(NormalNotification.Constants.customsSubPlaceLength)
    } yield {

      val expected: NormalNotification = base
        .copy(movementReferenceNumber = mrn)
        .copy(trader = trader)
        .copy(customsSubPlace = subPlace)
        .copy(notificationDate = LocalDate.now())

      (expected, trader)
    }

  val enRouteEventIncident: Gen[(EnRouteEventDomain, IncidentWithInformationDomain)] = for {
    enRouteEvent <- arbitrary[EnRouteEventDomain]
    incident     <- arbitrary[IncidentWithInformationDomain]
  } yield (enRouteEvent.copy(eventDetails = incident), incident)

  val enRouteEventVehicularTranshipment: Gen[(EnRouteEventDomain, VehicularTranshipmentDomain)] = for {
    enRouteEvent          <- arbitrary[EnRouteEventDomain]
    vehicularTranshipment <- arbitrary[VehicularTranshipmentDomain]
  } yield (enRouteEvent.copy(eventDetails = vehicularTranshipment), vehicularTranshipment)

  val enRouteEventContainerTranshipment: Gen[(EnRouteEventDomain, ContainerTranshipmentDomain)] = for {
    generatedEnRouteEvent <- arbitrary[EnRouteEventDomain]
    containerTranshipment <- arbitrary[ContainerTranshipmentDomain]
  } yield (generatedEnRouteEvent.copy(eventDetails = containerTranshipment), containerTranshipment)

  val enRouteEventContainerTranshipmentWithoutSeals: Gen[(EnRouteEventDomain, ContainerTranshipmentDomain)] = for {
    generatedEnRouteEvent <- arbitrary[EnRouteEventDomain]
    containerTranshipment <- arbitrary[ContainerTranshipmentDomain]
  } yield {
    val enRouteEvent = generatedEnRouteEvent.copy(eventDetails = containerTranshipment, seals = None)

    (enRouteEvent, containerTranshipment)
  }

  val simplifiedNotificationWithSubplace: Gen[(SimplifiedNotification, TraderDomain)] =
    for {
      base             <- arbitrary[SimplifiedNotification]
      trader           <- arbitrary[TraderDomain]
      mrn              <- arbitrary[MovementReferenceNumber]
      approvedLocation <- stringsWithMaxLength(SimplifiedNotification.Constants.approvedLocationLength)
    } yield {

      val expected: SimplifiedNotification = base
        .copy(movementReferenceNumber = mrn)
        .copy(trader = trader)
        .copy(notificationDate = LocalDate.now())
        .copy(authorisedLocation = approvedLocation)

      (expected, trader)
    }
}
