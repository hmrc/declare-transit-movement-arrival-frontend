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

package models.messages

import java.time.{LocalDate, LocalDateTime, LocalTime}

import generators.MessagesModelGenerators
import models.{messages, LanguageCode, NormalProcedureFlag, ProcedureTypeFlag}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.{FreeSpec, MustMatchers, OptionValues}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import play.twirl.api.utils.StringEscapeUtils
import utils.Format

import scala.xml.{Node, NodeSeq}
import scala.xml.Utility.trim
import scala.xml.XML.loadString

class ArrivalNotificationRequestSpec
    extends FreeSpec
    with MustMatchers
    with GuiceOneAppPerSuite
    with MessagesModelGenerators
    with ScalaCheckDrivenPropertyChecks
    with OptionValues {

  private val dateOfPreparation          = LocalDate.now()
  private val dateOfPreparationFormatted = Format.dateFormatted(dateOfPreparation)
  private val timeOfPreparation          = LocalTime.now()
  private val timeOfPreparationFormatted = Format.timeFormatted(timeOfPreparation)

  private val minimalArrivalNotificationRequest: ArrivalMovementRequest =
    ArrivalMovementRequest(
      meta = Meta(
        MessageSender("LOCAL", "EORI&123"),
        InterchangeControlReference("2019", 1),
        dateOfPreparation,
        timeOfPreparation
      ),
      header                      = messages.Header("MovementReferenceNumber", None, "arrivalNotificationPlace", None, NormalProcedureFlag, dateOfPreparation),
      traderDestination           = TraderDestination(None, None, None, None, None, None),
      customsOfficeOfPresentation = CustomsOfficeOfPresentation("PresentationOffice"),
      enRouteEvents               = None
    )

  private val minimalValidXml: Node = {
    <CC007A>
        <SynIdeMES1>{minimalArrivalNotificationRequest.syntaxIdentifier}</SynIdeMES1>
        <SynVerNumMES2>{minimalArrivalNotificationRequest.meta.syntaxVersionNumber}</SynVerNumMES2>
        {
          minimalArrivalNotificationRequest.meta.messageSender.toXml
        }
        <MesRecMES6>{minimalArrivalNotificationRequest.meta.messageRecipient}</MesRecMES6>
        <DatOfPreMES9>{dateOfPreparationFormatted}</DatOfPreMES9>
        <TimOfPreMES10>{timeOfPreparationFormatted}</TimOfPreMES10>
        {
          minimalArrivalNotificationRequest.meta.interchangeControlReference.toXml
        }
        <AppRefMES14>{minimalArrivalNotificationRequest.meta.applicationReference}</AppRefMES14>
        <TesIndMES18>{minimalArrivalNotificationRequest.meta.testIndicator}</TesIndMES18>
        <MesIdeMES19>{minimalArrivalNotificationRequest.meta.messageIndication}</MesIdeMES19>
        <MesTypMES20>{minimalArrivalNotificationRequest.messageCode.code}</MesTypMES20>
        <HEAHEA>
          <DocNumHEA5>{minimalArrivalNotificationRequest.header.movementReferenceNumber}</DocNumHEA5>
          <ArrNotPlaHEA60>{minimalArrivalNotificationRequest.header.arrivalNotificationPlace}</ArrNotPlaHEA60>
          <ArrNotPlaHEA60LNG>{Header.Constants.languageCode.code}</ArrNotPlaHEA60LNG>
          <ArrAgrLocOfGooHEA63LNG>{Header.Constants.languageCode.code}</ArrAgrLocOfGooHEA63LNG>
          <SimProFlaHEA132>{minimalArrivalNotificationRequest.header.procedureTypeFlag.code}</SimProFlaHEA132>
          <ArrNotDatHEA141>{dateOfPreparationFormatted}</ArrNotDatHEA141>
        </HEAHEA>
        <TRADESTRD>
          <NADLNGRD>{Header.Constants.languageCode.code}</NADLNGRD>
        </TRADESTRD>
        <CUSOFFPREOFFRES>
          <RefNumRES1>{minimalArrivalNotificationRequest.customsOfficeOfPresentation.presentationOffice}</RefNumRES1>
        </CUSOFFPREOFFRES>
      </CC007A>
  }

  "ArrivalMovementRequest" - {
    "must create valid xml" in {
      forAll(arbitrary[ArrivalMovementRequest]) {
        arrivalMovementRequest =>
          whenever(hasEoriWithNormalProcedure(arrivalMovementRequest)) {

            val validXml: Node =
              <CC007A> {
                    buildAndEncodeElem(arrivalMovementRequest.meta.syntaxIdentifier, "SynIdeMES1") ++
                    buildAndEncodeElem(arrivalMovementRequest.meta.syntaxVersionNumber, "SynVerNumMES2") ++
                    arrivalMovementRequest.meta.messageSender.toXml ++
                    buildOptionalElem(arrivalMovementRequest.meta.senderIdentificationCodeQualifier, "SenIdeCodQuaMES4") ++
                    buildOptionalElem(arrivalMovementRequest.meta.recipientIdentificationCodeQualifier, "RecIdeCodQuaMES7") ++
                    buildAndEncodeElem(arrivalMovementRequest.meta.messageRecipient, "MesRecMES6") ++
                    buildAndEncodeElem(arrivalMovementRequest.meta.dateOfPreparation, "DatOfPreMES9") ++
                    buildAndEncodeElem(Format.timeFormatted(arrivalMovementRequest.meta.timeOfPreparation), "TimOfPreMES10") ++
                    arrivalMovementRequest.meta.interchangeControlReference.toXml ++
                    buildOptionalElem(arrivalMovementRequest.meta.recipientsReferencePassword, "RecRefMES12") ++
                    buildOptionalElem(arrivalMovementRequest.meta.recipientsReferencePasswordQualifier, "RecRefQuaMES13") ++
                    buildAndEncodeElem(arrivalMovementRequest.meta.applicationReference, "AppRefMES14") ++
                    buildOptionalElem(arrivalMovementRequest.meta.priority, "PriMES15") ++
                    buildOptionalElem(arrivalMovementRequest.meta.acknowledgementRequest, "AckReqMES16") ++
                    buildOptionalElem(arrivalMovementRequest.meta.communicationsAgreementId, "ComAgrIdMES17") ++
                    buildAndEncodeElem(arrivalMovementRequest.meta.testIndicator, "TesIndMES18") ++
                    buildAndEncodeElem(arrivalMovementRequest.meta.messageIndication, "MesIdeMES19") ++
                    buildAndEncodeElem(arrivalMovementRequest.messageCode.code, "MesTypMES20") ++
                    buildOptionalElem(arrivalMovementRequest.meta.commonAccessReference, "ComAccRefMES21") ++
                    buildOptionalElem(arrivalMovementRequest.meta.messageSequenceNumber, "MesSeqNumMES22") ++
                    buildOptionalElem(arrivalMovementRequest.meta.firstAndLastTransfer, "FirAndLasTraMES23")
                  }
                  <HEAHEA> {
                    buildAndEncodeElem(arrivalMovementRequest.header.movementReferenceNumber, "DocNumHEA5") ++
                    buildOptionalElem(arrivalMovementRequest.header.customsSubPlace, "CusSubPlaHEA66") ++
                    buildAndEncodeElem(arrivalMovementRequest.header.arrivalNotificationPlace, "ArrNotPlaHEA60") ++
                    buildAndEncodeElem(Header.Constants.languageCode, "ArrNotPlaHEA60LNG") ++
                    buildOptionalElem(arrivalMovementRequest.header.arrivalAgreedLocationOfGoods, "ArrAgrLocCodHEA62") ++
                    buildOptionalElem(arrivalMovementRequest.header.arrivalAgreedLocationOfGoods, "ArrAgrLocOfGooHEA63") ++
                    buildAndEncodeElem(Header.Constants.languageCode, "ArrAgrLocOfGooHEA63LNG") ++
                    buildOptionalElem(arrivalMovementRequest.header.arrivalAgreedLocationOfGoods, "ArrAutLocOfGooHEA65") ++
                    buildAndEncodeElem(arrivalMovementRequest.header.procedureTypeFlag, "SimProFlaHEA132") ++
                    buildAndEncodeElem(arrivalMovementRequest.header.notificationDate, "ArrNotDatHEA141")
                  }
                  </HEAHEA>
                  <TRADESTRD> {
                    buildOptionalElem(arrivalMovementRequest.traderDestination.name, "NamTRD7") ++
                    buildOptionalElem(arrivalMovementRequest.traderDestination.streetAndNumber, "StrAndNumTRD22") ++
                    buildOptionalElem(arrivalMovementRequest.traderDestination.postCode, "PosCodTRD23") ++
                    buildOptionalElem(arrivalMovementRequest.traderDestination.city, "CitTRD24") ++
                    buildOptionalElem(arrivalMovementRequest.traderDestination.countryCode, "CouTRD25") ++
                    buildAndEncodeElem(TraderDestination.Constants.languageCode, "NADLNGRD") ++
                    buildOptionalElem(arrivalMovementRequest.traderDestination.eori, "TINTRD59")
                    }
                  </TRADESTRD>
                  <CUSOFFPREOFFRES>
                    <RefNumRES1>{arrivalMovementRequest.customsOfficeOfPresentation.presentationOffice}</RefNumRES1>
                  </CUSOFFPREOFFRES>
                  {buildEnRouteEvent(arrivalMovementRequest.enRouteEvents, Header.Constants.languageCode)}
                </CC007A>

            val result = trim(arrivalMovementRequest.toXml)

            result mustBe trim(loadString(validXml.toString))
          }
      }
    }

    "must return minimal valid xml" in {

      val result = trim(minimalArrivalNotificationRequest.toXml)

      result mustBe trim(loadString(minimalValidXml.toString))
    }

    "must return valid xml with an EnRouteEvent" in {

      val arrivalNotificationRequestWithIncident: ArrivalMovementRequest =
        ArrivalMovementRequest(
          meta = Meta(
            messageSender               = MessageSender("LOCAL", "EORI&123"),
            interchangeControlReference = InterchangeControlReference("2019", 1),
            dateOfPreparation,
            timeOfPreparation
          ),
          header                      = messages.Header("MovementReferenceNumber", None, "arrivalNotificationPlace", None, NormalProcedureFlag, dateOfPreparation),
          traderDestination           = TraderDestination(None, None, None, None, None, None),
          customsOfficeOfPresentation = CustomsOfficeOfPresentation("PresentationOffice"),
          enRouteEvents = Some(
            Seq(
              EnRouteEvent(
                place         = "place",
                countryCode   = "GB",
                alreadyInNcts = true,
                eventDetails  = Some(Incident(None, None, None, None, None)),
                seals         = None
              )
            ))
        )

      val minimalValidXmlWithEnrouteEvent: Node =
        <CC007A>
          <SynIdeMES1>{arrivalNotificationRequestWithIncident.syntaxIdentifier}</SynIdeMES1>
          <SynVerNumMES2>{arrivalNotificationRequestWithIncident.meta.syntaxVersionNumber}</SynVerNumMES2>
          {
            arrivalNotificationRequestWithIncident.meta.messageSender.toXml
          }
          <MesRecMES6>{arrivalNotificationRequestWithIncident.meta.messageRecipient}</MesRecMES6>
          <DatOfPreMES9>{dateOfPreparationFormatted}</DatOfPreMES9>
          <TimOfPreMES10>{timeOfPreparationFormatted}</TimOfPreMES10>
          {
            arrivalNotificationRequestWithIncident.meta.interchangeControlReference.toXml
          }
          <AppRefMES14>{arrivalNotificationRequestWithIncident.meta.applicationReference}</AppRefMES14>
          <TesIndMES18>{arrivalNotificationRequestWithIncident.meta.testIndicator}</TesIndMES18>
          <MesIdeMES19>{arrivalNotificationRequestWithIncident.meta.messageIndication}</MesIdeMES19>
          <MesTypMES20>{arrivalNotificationRequestWithIncident.messageCode.code}</MesTypMES20>
          <HEAHEA>
            <DocNumHEA5>{arrivalNotificationRequestWithIncident.header.movementReferenceNumber}</DocNumHEA5>
            <ArrNotPlaHEA60>{arrivalNotificationRequestWithIncident.header.arrivalNotificationPlace}</ArrNotPlaHEA60>
            <ArrNotPlaHEA60LNG>{Header.Constants.languageCode.code}</ArrNotPlaHEA60LNG>
            <ArrAgrLocOfGooHEA63LNG>{Header.Constants.languageCode.code}</ArrAgrLocOfGooHEA63LNG>
            <SimProFlaHEA132>{arrivalNotificationRequestWithIncident.header.procedureTypeFlag.code}</SimProFlaHEA132>
            <ArrNotDatHEA141>{dateOfPreparationFormatted}</ArrNotDatHEA141>
          </HEAHEA>
          <TRADESTRD>
            <NADLNGRD>{Header.Constants.languageCode.code}</NADLNGRD>
          </TRADESTRD>
          <CUSOFFPREOFFRES>
            <RefNumRES1>{arrivalNotificationRequestWithIncident.customsOfficeOfPresentation.presentationOffice}</RefNumRES1>
          </CUSOFFPREOFFRES>
          {
            arrivalNotificationRequestWithIncident.enRouteEvents.value.map {
              enrouteEvent =>
                <ENROUEVETEV>
                  <PlaTEV10>{enrouteEvent.place}</PlaTEV10>
                  <PlaTEV10LNG>{Header.Constants.languageCode.code}</PlaTEV10LNG>
                  <CouTEV13>{enrouteEvent.countryCode}</CouTEV13>
                  <CTLCTL>
                    <AlrInNCTCTL29>1</AlrInNCTCTL29>
                  </CTLCTL>
                  <INCINC>
                    <IncFlaINC3>1</IncFlaINC3>
                    <IncInfINC4LNG>{Header.Constants.languageCode.code}</IncInfINC4LNG>
                    <EndAutINC7LNG>{Header.Constants.languageCode.code}</EndAutINC7LNG>
                    <EndPlaINC10LNG>{Header.Constants.languageCode.code}</EndPlaINC10LNG>
                  </INCINC>
                </ENROUEVETEV>
            }
          }
        </CC007A>

      val result = trim(arrivalNotificationRequestWithIncident.toXml)

      result mustBe trim(loadString(minimalValidXmlWithEnrouteEvent.toString))
    }
  }

  private def buildSeals(seals: Seq[Seal]): NodeSeq = {
    val sealsXml = seals.map {
      seal =>
        <SEAIDSI1>
              <SeaIdeSI11>
                {seal.numberOrMark}
              </SeaIdeSI11>
              <SeaIdeSI11LNG>EN</SeaIdeSI11LNG>
            </SEAIDSI1>
    }

    <SEAINFSF1>
          <SeaNumSF12>
            {seals.size}
          </SeaNumSF12>{sealsXml}
        </SEAINFSF1>
  }

  private def buildEnRouteEvent(enRouteEvents: Option[Seq[EnRouteEvent]], languageCode: LanguageCode): NodeSeq = enRouteEvents match {
    case Some(events) =>
      events.map {
        event =>
          <ENROUEVETEV> {
            buildAndEncodeElem(event.place,"PlaTEV10") ++
              buildAndEncodeElem(languageCode,"PlaTEV10LNG") ++
              buildAndEncodeElem(event.countryCode,"CouTEV13")
            }
            <CTLCTL> {
              buildAndEncodeElem(event.alreadyInNcts,"AlrInNCTCTL29")
              }
            </CTLCTL> {
            event.eventDetails.map (buildIncidentType(_, event.seals, languageCode)).getOrElse(NodeSeq.Empty)
            }
          </ENROUEVETEV>
      }
    case None => NodeSeq.Empty
  }

  private def buildIncidentType(event: EventDetails, sealsOpt: Option[Seq[Seal]], languageCode: LanguageCode): NodeSeq = {
    val seals = sealsOpt.fold(NodeSeq.Empty) {
      seal =>
        buildSeals(seal)
    }
    event match {
      case incident: Incident =>
        <INCINC>
        {
        buildIncidentFlag(incident.information.isDefined) ++
          buildOptionalElem(incident.information, "IncInfINC4") ++
          buildAndEncodeElem(languageCode, "IncInfINC4LNG") ++
          buildOptionalElem(incident.date, "EndDatINC6") ++
          buildOptionalElem(incident.authority, "EndAutINC7") ++
          buildAndEncodeElem(languageCode, "EndAutINC7LNG") ++
          buildOptionalElem(incident.place, "EndPlaINC10") ++
          buildAndEncodeElem(languageCode, "EndPlaINC10LNG") ++
          buildOptionalElem(incident.country, "EndCouINC12")
        }
      </INCINC> ++ seals

      case containerTranshipment: ContainerTranshipment =>
        seals ++
          <TRASHP> {
        buildOptionalElem(containerTranshipment.date, "EndDatSHP60") ++
        buildOptionalElem(containerTranshipment.authority, "EndAutSHP61") ++
        buildAndEncodeElem(languageCode,"EndAutSHP61LNG") ++
        buildOptionalElem(containerTranshipment.place, "EndPlaSHP63") ++
        buildAndEncodeElem(languageCode,"EndPlaSHP63LNG") ++
        buildOptionalElem(containerTranshipment.country, "EndCouSHP65") ++
        containerTranshipment.containers.map {
          container =>
            <CONNR3>
              {buildAndEncodeElem(container.containerNumber, "ConNumNR31")}
            </CONNR3>
          }
        }
      </TRASHP>

      case vehicularTranshipment: VehicularTranshipment =>
        seals ++
          <TRASHP> {
        buildAndEncodeElem(vehicularTranshipment.transportIdentity,"NewTraMeaIdeSHP26") ++
        buildAndEncodeElem(languageCode,"NewTraMeaIdeSHP26LNG") ++
        buildAndEncodeElem(vehicularTranshipment.transportCountry,"NewTraMeaNatSHP54") ++
        buildOptionalElem(vehicularTranshipment.date, "EndDatSHP60") ++
        buildOptionalElem(vehicularTranshipment.authority, "EndAutSHP61") ++
        buildAndEncodeElem(languageCode, "EndAutSHP61LNG") ++
        buildOptionalElem(vehicularTranshipment.place, "EndPlaSHP63") ++
        buildAndEncodeElem(languageCode, "EndPlaSHP63LNG") ++
        buildOptionalElem(vehicularTranshipment.country, "EndCouSHP65") ++
        {
          vehicularTranshipment.containers match {
            case Some(containers) =>
              containers.map {
                container =>
                  <CONNR3>
                    {buildAndEncodeElem(container.containerNumber, "ConNumNR31")}
                  </CONNR3>
              }
            case _ => NodeSeq.Empty
            }
          }
        }
      </TRASHP>

    }
  }

  private def hasEoriWithNormalProcedure(arrivalMovementRequest: ArrivalMovementRequest): Boolean =
    arrivalMovementRequest.traderDestination.eori.isDefined &&
      arrivalMovementRequest.header.procedureTypeFlag.equals(NormalProcedureFlag)

  private def buildOptionalElem[A](value: Option[A], elementTag: String): NodeSeq = value match {
    case Some(result: String) =>
      val encodeResult = StringEscapeUtils.escapeXml11(result)
      loadString(s"<$elementTag>$encodeResult</$elementTag>")
    case Some(result: LocalDate)     => loadString(s"<$elementTag>${Format.dateFormatted(result)}</$elementTag>")
    case Some(result: LocalDateTime) => loadString(s"<$elementTag>${Format.dateFormatted(result)}</$elementTag>")
    case _                           => NodeSeq.Empty
  }

  private def buildAndEncodeElem[A](value: A, elementTag: String): NodeSeq = value match {
    case result: String =>
      val encodeResult = StringEscapeUtils.escapeXml11(result)
      loadString(s"<$elementTag>$encodeResult</$elementTag>")
    case result: Boolean           => loadString(s"<$elementTag>${if (result) 1 else 0}</$elementTag>")
    case result: LocalDate         => loadString(s"<$elementTag>${Format.dateFormatted(result)}</$elementTag>")
    case result: LocalDateTime     => loadString(s"<$elementTag>${Format.dateFormatted(result)}</$elementTag>")
    case result: LanguageCode      => loadString(s"<$elementTag>${result.code}</$elementTag>")
    case result: ProcedureTypeFlag => loadString(s"<$elementTag>${result.code}</$elementTag>")
    case _                         => NodeSeq.Empty
  }

  private def buildIncidentFlag(hasIncidentInformation: Boolean): NodeSeq =
    if (hasIncidentInformation) NodeSeq.Empty else <IncFlaINC3>1</IncFlaINC3>

}
