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

import java.time.LocalDate

import cats.syntax.all._
import com.lucidchart.open.xtract.XmlReader._
import com.lucidchart.open.xtract.{XmlReader, __ => xmlPath}
import models.XMLReads._
import models.XMLWrites
import models.XMLWrites._
import play.api.libs.json._
import utils.Format

import scala.language.implicitConversions
import scala.xml.NodeSeq

sealed trait EventDetails

object EventDetails {

  object Constants {
    val authorityLength = 35
    val placeLength     = 35
    val countryLength   = 2
  }

  implicit lazy val reads: Reads[EventDetails] = {

    implicit class ReadsWithContravariantOr[A](a: Reads[A]) {

      def or[B >: A](b: Reads[B]): Reads[B] =
        a.map[B](identity).orElse(b)
    }

    implicit def convertToSupertype[A, B >: A](a: Reads[A]): Reads[B] =
      a.map(identity)

    Transhipment.reads or
      Incident.format
  }

  implicit lazy val writes: Writes[EventDetails] = Writes {
    case i: Incident     => Json.toJson(i)(Incident.format)
    case t: Transhipment => Json.toJson(t)(Transhipment.writes)
  }
}

final case class Incident(
  information: Option[String],
  date: Option[LocalDate]   = None,
  authority: Option[String] = None,
  place: Option[String]     = None,
  country: Option[String]   = None
) extends EventDetails

object Incident {

  object Constants {
    val informationLength = 350
  }

  implicit lazy val format: Format[Incident] =
    Json.format[Incident]

  implicit def writes: XMLWrites[Incident] = XMLWrites[Incident] {
    incident =>
      <INCINC>
      {
      incident.information.map(
        information =>
        <IncInfINC4> {escapeXml(information)} </IncInfINC4>
      ).getOrElse(
        <IncFlaINC3>1</IncFlaINC3>
      ) ++
        <IncInfINC4LNG> {Header.Constants.languageCode.code} </IncInfINC4LNG> ++
        incident.date.fold[NodeSeq](NodeSeq.Empty)(date =>
          <EndDatINC6> {Format.dateFormatted(date)} </EndDatINC6>
        ) ++
        incident.authority.fold[NodeSeq](NodeSeq.Empty)(authority =>
          <EndAutINC7> {escapeXml(authority)} </EndAutINC7>
        ) ++
        <EndAutINC7LNG>{Header.Constants.languageCode.code}</EndAutINC7LNG> ++
        incident.place.fold(NodeSeq.Empty)(place =>
          <EndPlaINC10> {escapeXml(place)}</EndPlaINC10>
        ) ++
        <EndPlaINC10LNG>{Header.Constants.languageCode.code}</EndPlaINC10LNG> ++
        incident.country.fold(NodeSeq.Empty)(country =>
          <EndCouINC12> {escapeXml(country)} </EndCouINC12>
        )
      }
    </INCINC>
  }

  implicit val xmlReader: XmlReader[Incident] =
    (
      (xmlPath \ "IncInfINC4").read[String].optional,
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

  implicit lazy val reads: Reads[Transhipment] = {

    implicit class ReadsWithContravariantOr[A](a: Reads[A]) {

      def or[B >: A](b: Reads[B]): Reads[B] =
        a.map[B](identity).orElse(b)
    }

    implicit def convertToSupertype[A, B >: A](a: Reads[A]): Reads[B] =
      a.map(identity)

    VehicularTranshipment.reads or
      ContainerTranshipment.format
  }

  implicit lazy val writes: Writes[Transhipment] = Writes {
    case t: VehicularTranshipment => Json.toJson(t)(VehicularTranshipment.writes)
    case t: ContainerTranshipment => Json.toJson(t)(ContainerTranshipment.format)
  }
}

final case class VehicularTranshipment(
  transportIdentity: String,
  transportCountry: String,
  date: Option[LocalDate]   = None,
  authority: Option[String] = None,
  place: Option[String]     = None,
  country: Option[String]   = None,
  containers: Option[Seq[Container]]
) extends Transhipment

object VehicularTranshipment {

  object Constants {

    val transportIdentityLength = 27
    val transportCountryLength  = 2

  }

  implicit lazy val reads: Reads[VehicularTranshipment] = {

    import play.api.libs.functional.syntax._

    (
      (__ \ "transportIdentity").read[String] and
        (__ \ "transportCountry").read[String] and
        (__ \ "date").readNullable[LocalDate] and
        (__ \ "authority").readNullable[String] and
        (__ \ "place").readNullable[String] and
        (__ \ "country").readNullable[String] and
        (__ \ "containers").readNullable[Seq[Container]]
    )(VehicularTranshipment.apply _)
  }

  implicit lazy val writes: OWrites[VehicularTranshipment] =
    OWrites[VehicularTranshipment] {
      transhipment =>
        Json
          .obj(
            "transportIdentity" -> transhipment.transportIdentity,
            "transportCountry"  -> transhipment.transportCountry,
            "date"              -> transhipment.date,
            "authority"         -> transhipment.authority,
            "place"             -> transhipment.place,
            "country"           -> transhipment.country,
            "containers"        -> Json.toJson(transhipment.containers)
          )
    }

  implicit def xmlWrites: XMLWrites[VehicularTranshipment] = XMLWrites[VehicularTranshipment] {
    transhipment =>
      <TRASHP>
        {
          <NewTraMeaIdeSHP26> {escapeXml(transhipment.transportIdentity)} </NewTraMeaIdeSHP26> ++
            <NewTraMeaIdeSHP26LNG> {Header.Constants.languageCode.code} </NewTraMeaIdeSHP26LNG> ++
            <NewTraMeaNatSHP54> {escapeXml(transhipment.transportCountry)} </NewTraMeaNatSHP54> ++ {
            transhipment.date.fold(NodeSeq.Empty)(date =>
              <EndDatSHP60> {Format.dateFormatted(date)} </EndDatSHP60>
            )
          } ++ {
            transhipment.authority.fold(NodeSeq.Empty)(authority =>
              <EndAutSHP61> {escapeXml(authority)} </EndAutSHP61>
            )
          } ++
            <EndAutSHP61LNG> {Header.Constants.languageCode.code} </EndAutSHP61LNG> ++ {
            transhipment.place.fold(NodeSeq.Empty)(place =>
              <EndPlaSHP63> {escapeXml(place)} </EndPlaSHP63>
            )
          } ++
            <EndPlaSHP63LNG> {Header.Constants.languageCode.code} </EndPlaSHP63LNG> ++ {
            transhipment.country.fold(NodeSeq.Empty)(country =>
              <EndCouSHP65> {escapeXml(country)} </EndCouSHP65>
            )
          } ++ transhipment.containers.fold(NodeSeq.Empty)(_.flatMap(_.toXml))
        }
      </TRASHP>
  }

  implicit lazy val xmlReader: XmlReader[VehicularTranshipment] = ???
}

final case class ContainerTranshipment(
  date: Option[LocalDate]   = None,
  authority: Option[String] = None,
  place: Option[String]     = None,
  country: Option[String]   = None,
  containers: Seq[Container]
) extends Transhipment {
  require(containers.nonEmpty, "At least one container number must be provided")
}

object ContainerTranshipment {

  implicit lazy val format: Format[ContainerTranshipment] =
    Json.format[ContainerTranshipment]

  implicit def xmlWrites: XMLWrites[ContainerTranshipment] = XMLWrites[ContainerTranshipment] {
    transhipment =>
      <TRASHP>
        {
          transhipment.date.fold(NodeSeq.Empty) (
            date =>
              <EndDatSHP60> {Format.dateFormatted(date)} </EndDatSHP60>
          ) ++
          transhipment.authority.fold(NodeSeq.Empty) (
            authority =>
            <EndAutSHP61> {escapeXml(authority)} </EndAutSHP61>
          ) ++
            <EndAutSHP61LNG> {Header.Constants.languageCode.code} </EndAutSHP61LNG> ++
          transhipment.place.fold(NodeSeq.Empty) (
            place =>
              <EndPlaSHP63> {escapeXml(place)} </EndPlaSHP63>
          ) ++
            <EndPlaSHP63LNG> {Header.Constants.languageCode.code} </EndPlaSHP63LNG> ++
          transhipment.country.fold(NodeSeq.Empty) (
              country =>
                <EndCouSHP65> {escapeXml(country)} </EndCouSHP65>
          ) ++ transhipment.containers.map(_.toXml)
      }
      </TRASHP>
  }

  implicit lazy val xmlReader: XmlReader[ContainerTranshipment] = (
    (xmlPath \ "EndDatSHP60").read[LocalDate].optional,
    (xmlPath \ "EndAutSHP61").read[String].optional,
    (xmlPath \ "EndPlaSHP63").read[String].optional,
    (xmlPath \ "EndCouSHP65").read[String].optional,
    (xmlPath \ "CONNR3").read(seq[Container])
  ).mapN(apply)
}
