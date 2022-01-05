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

package models.messages

import java.time.LocalDate

import cats.syntax.all._
import com.lucidchart.open.xtract.XmlReader._
import com.lucidchart.open.xtract.{ParseFailure, XmlReader, __ => xmlPath}
import models.XMLReads._
import models.XMLWrites
import models.XMLWrites._
import models.domain._
import models.reference.CountryCode
import utils.Format

import scala.xml.NodeSeq

sealed trait EventDetails

object EventDetails {

  def buildEventDetailsDomain(eventDetails: EventDetails): EventDetailsDomain =
    eventDetails match {
      case incidentWithInformation: IncidentWithInformation =>
        IncidentWithInformation.incidentWithInformationToDomain(incidentWithInformation)
      case _: IncidentWithoutInformation =>
        IncidentWithoutInformationDomain
      case vehicularTranshipment: VehicularTranshipment =>
        VehicularTranshipment.vehicularTranshipmentToDomain(vehicularTranshipment)
      case containerTranshipment: ContainerTranshipment =>
        ContainerTranshipment.containerTranshipmentToDomain(containerTranshipment)
    }

  implicit def xmlReader: XmlReader[EventDetails] = XmlReader {
    xml =>
      val transhipmentPath = xmlPath \ "TRASHP"
      val incidentPath     = xmlPath \ "INCINC"

      if (transhipmentPath(xml).nonEmpty)
        transhipmentPath.read(Transhipment.xmlReader).read(xml)
      else if (incidentPath(xml).nonEmpty)
        incidentPath.read(Incident.xmlReader).read(xml)
      else ParseFailure()
  }
}

sealed trait Incident extends EventDetails

object Incident {
  implicit lazy val xmlReader: XmlReader[Incident] = IncidentWithInformation.xmlReader or IncidentWithoutInformation.xmlReader
}

final case class IncidentWithInformation(
  incidentInformation: String,
  date: Option[LocalDate] = None,
  authority: Option[String] = None,
  place: Option[String] = None,
  country: Option[String] = None
) extends Incident

object IncidentWithInformation {

  object Constants {
    val informationLength = 350
  }

  def incidentWithInformationToDomain(incident: IncidentWithInformation): IncidentWithInformationDomain =
    IncidentWithInformation
      .unapply(incident)
      .map {
        case (incidentInformation, _, _, _, _) =>
          IncidentWithInformationDomain(incidentInformation)
      }
      .get

  implicit def xmlWrites: XMLWrites[IncidentWithInformation] = XMLWrites[IncidentWithInformation] {
    incident =>
      <INCINC>
      {
        <IncInfINC4>{escapeXml(incident.incidentInformation)}</IncInfINC4>
        <IncInfINC4LNG>{Header.Constants.languageCode.code}</IncInfINC4LNG> ++
          incident.date.fold[NodeSeq](NodeSeq.Empty)(
            date => <EndDatINC6>{Format.dateFormatted(date)}</EndDatINC6>
          ) ++
          incident.authority.fold[NodeSeq](NodeSeq.Empty)(
            authority => <EndAutINC7>{escapeXml(authority)}</EndAutINC7>
          ) ++
          <EndAutINC7LNG>{Header.Constants.languageCode.code}</EndAutINC7LNG> ++
          incident.place.fold(NodeSeq.Empty)(
            place => <EndPlaINC10>{escapeXml(place)}</EndPlaINC10>
          ) ++
          <EndPlaINC10LNG>{Header.Constants.languageCode.code}</EndPlaINC10LNG> ++
          incident.country.fold(NodeSeq.Empty)(
            country => <EndCouINC12>{escapeXml(country)}</EndCouINC12>
          )
      }
    </INCINC>
  }

  implicit val xmlReader: XmlReader[IncidentWithInformation] =
    (
      (xmlPath \ "IncInfINC4").read[String],
      (xmlPath \ "EndDatINC6").read[LocalDate].optional,
      (xmlPath \ "EndAutINC7").read[String].optional,
      (xmlPath \ "EndPlaINC10").read[String].optional,
      (xmlPath \ "EndCouINC12").read[String].optional
    ).mapN(apply)
}

final case class IncidentWithoutInformation(
  date: Option[LocalDate] = None,
  authority: Option[String] = None,
  place: Option[String] = None,
  country: Option[String] = None
) extends Incident

object IncidentWithoutInformation {

  implicit def xmlWrites: XMLWrites[IncidentWithoutInformation] = XMLWrites[IncidentWithoutInformation] {
    incident =>
      <INCINC>
        {
        <IncFlaINC3>1</IncFlaINC3>
          <IncInfINC4LNG>{Header.Constants.languageCode.code}</IncInfINC4LNG> ++
          incident.date.fold[NodeSeq](NodeSeq.Empty)(
            date => <EndDatINC6>{Format.dateFormatted(date)}</EndDatINC6>
          ) ++
          incident.authority.fold[NodeSeq](NodeSeq.Empty)(
            authority => <EndAutINC7>{escapeXml(authority)}</EndAutINC7>
          ) ++
          <EndAutINC7LNG>{
            Header.Constants.languageCode.code
          }</EndAutINC7LNG> ++ //TODO This potentially needs to be included in the above fold as the elements are paired
          incident.place.fold(NodeSeq.Empty)(
            place => <EndPlaINC10>{escapeXml(place)}</EndPlaINC10>
          ) ++
          <EndPlaINC10LNG>{Header.Constants.languageCode.code}</EndPlaINC10LNG> ++
          incident.country.fold(NodeSeq.Empty)(
            country => <EndCouINC12>{escapeXml(country)}</EndCouINC12>
          )
      }
      </INCINC>
  }

  implicit val xmlReader: XmlReader[IncidentWithoutInformation] =
    (
      (xmlPath \ "EndDatINC6").read[LocalDate].optional,
      (xmlPath \ "EndAutINC7").read[String].optional,
      (xmlPath \ "EndPlaINC10").read[String].optional,
      (xmlPath \ "EndCouINC12").read[String].optional
    ).mapN(apply)
}

sealed trait Transhipment extends EventDetails

object Transhipment {

  object Constants {
    val containerLength = 17
    val maxContainers   = 99
  }

  implicit lazy val xmlReader: XmlReader[Transhipment] = VehicularTranshipment.xmlReader or ContainerTranshipment.xmlReader
}

final case class VehicularTranshipment(
  transportIdentity: String,
  transportCountry: CountryCode,
  containers: Option[Seq[Container]],
  date: Option[LocalDate] = None,
  authority: Option[String] = None,
  place: Option[String] = None,
  country: Option[String] = None
) extends Transhipment

object VehicularTranshipment {

  object Constants {
    val transportIdentityLength = 27
  }

  def vehicularTranshipmentToDomain(transhipment: VehicularTranshipment): VehicularTranshipmentDomain =
    VehicularTranshipment
      .unapply(transhipment)
      .map {
        case _ @(transportIdentity, transportCountry, containers, _, _, _, _) =>
          VehicularTranshipmentDomain(
            transportIdentity,
            transportCountry,
            containers.map(_.map(Container.containerToDomain))
          )
      }
      .get

  implicit def xmlWrites: XMLWrites[VehicularTranshipment] = XMLWrites[VehicularTranshipment] {
    transhipment =>
      <TRASHP>
        {
        <NewTraMeaIdeSHP26>{escapeXml(transhipment.transportIdentity)}</NewTraMeaIdeSHP26> ++
          <NewTraMeaIdeSHP26LNG>{Header.Constants.languageCode.code}</NewTraMeaIdeSHP26LNG> ++
          <NewTraMeaNatSHP54>{escapeXml(transhipment.transportCountry.code)}</NewTraMeaNatSHP54> ++ {
            transhipment.date.fold(NodeSeq.Empty)(
              date => <EndDatSHP60> {Format.dateFormatted(date)} </EndDatSHP60>
            )
          } ++ {
            transhipment.authority.fold(NodeSeq.Empty)(
              authority => <EndAutSHP61> {escapeXml(authority)} </EndAutSHP61>
            )
          } ++
          <EndAutSHP61LNG> {Header.Constants.languageCode.code} </EndAutSHP61LNG> ++ {
            transhipment.place.fold(NodeSeq.Empty)(
              place => <EndPlaSHP63> {escapeXml(place)} </EndPlaSHP63>
            )
          } ++
          <EndPlaSHP63LNG> {Header.Constants.languageCode.code} </EndPlaSHP63LNG> ++ {
            transhipment.country.fold(NodeSeq.Empty)(
              country => <EndCouSHP65> {escapeXml(country)} </EndCouSHP65>
            )
          } ++ transhipment.containers.fold(NodeSeq.Empty)(_.flatMap(_.toXml))
      }
      </TRASHP>
  }

  implicit lazy val xmlReader: XmlReader[VehicularTranshipment] = (
    (xmlPath \ "NewTraMeaIdeSHP26").read[String],
    (xmlPath \ "NewTraMeaNatSHP54").read[String].map(CountryCode(_)),
    (xmlPath \ "CONNR3").read(strictReadOptionSeq[Container]),
    (xmlPath \ "EndDatSHP60").read[LocalDate].optional,
    (xmlPath \ "EndAutSHP61").read[String].optional,
    (xmlPath \ "EndPlaSHP63").read[String].optional,
    (xmlPath \ "EndCouSHP65").read[String].optional
  ).mapN(apply)
}

final case class ContainerTranshipment(
  containers: Seq[Container],
  date: Option[LocalDate] = None,
  authority: Option[String] = None,
  place: Option[String] = None,
  country: Option[String] = None
) extends Transhipment {
  require(containers.nonEmpty, "At least one container number must be provided")
}

object ContainerTranshipment {

  def containerTranshipmentToDomain(transhipment: ContainerTranshipment): ContainerTranshipmentDomain =
    ContainerTranshipment
      .unapply(transhipment)
      .map {
        case _ @(containers, _, _, _, _) =>
          ContainerTranshipmentDomain(
            containers.map(Container.containerToDomain)
          )
      }
      .get

  implicit def xmlWrites: XMLWrites[ContainerTranshipment] = XMLWrites[ContainerTranshipment] {
    transhipment =>
      <TRASHP>
        {
        transhipment.date.fold(NodeSeq.Empty)(
          date => <EndDatSHP60> {Format.dateFormatted(date)} </EndDatSHP60>
        ) ++
          transhipment.authority.fold(NodeSeq.Empty)(
            authority => <EndAutSHP61> {escapeXml(authority)} </EndAutSHP61>
          ) ++
          <EndAutSHP61LNG> {Header.Constants.languageCode.code} </EndAutSHP61LNG> ++
          transhipment.place.fold(NodeSeq.Empty)(
            place => <EndPlaSHP63> {escapeXml(place)} </EndPlaSHP63>
          ) ++
          <EndPlaSHP63LNG> {Header.Constants.languageCode.code} </EndPlaSHP63LNG> ++
          transhipment.country.fold(NodeSeq.Empty)(
            country => <EndCouSHP65> {escapeXml(country)} </EndCouSHP65>
          ) ++ transhipment.containers.map(_.toXml)
      }
      </TRASHP>
  }

  implicit lazy val xmlReader: XmlReader[ContainerTranshipment] = (
    (xmlPath \ "CONNR3").read(strictReadSeq[Container]),
    (xmlPath \ "EndDatSHP60").read[LocalDate].optional,
    (xmlPath \ "EndAutSHP61").read[String].optional,
    (xmlPath \ "EndPlaSHP63").read[String].optional,
    (xmlPath \ "EndCouSHP65").read[String].optional
  ).mapN(apply)
}
