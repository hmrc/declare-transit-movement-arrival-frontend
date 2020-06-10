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

import com.lucidchart.open.xtract.{XmlReader, __ => xmlPath}
import com.lucidchart.open.xtract.XmlReader._
import models.XMLWrites._
import models.XMLReads._
import cats.syntax.all._
import models.{LanguageCodeEnglish, XMLWrites}
import play.api.libs.json._

import scala.xml.NodeSeq

final case class EnRouteEvent(place: String, countryCode: String, alreadyInNcts: Boolean, eventDetails: Option[EventDetails], seals: Option[Seq[Seal]])

object EnRouteEvent {

  object Constants {
    val placeLength       = 35
    val countryCodeLength = 2
    val sealsLength       = 20
  }

  implicit lazy val reads: Reads[EnRouteEvent] = {

    import play.api.libs.functional.syntax._

    (
      (__ \ "place").read[String] and
        (__ \ "countryCode").read[String] and
        (__ \ "alreadyInNcts").read[Boolean] and
        (__ \ "eventDetails").readNullable[EventDetails] and
        (__ \ "seals").readNullable[Seq[Seal]]
    )(EnRouteEvent(_, _, _, _, _))
  }

  implicit lazy val writes: OWrites[EnRouteEvent] =
    OWrites[EnRouteEvent] {
      event =>
        Json
          .obj(
            "place"         -> event.place,
            "countryCode"   -> event.countryCode,
            "alreadyInNcts" -> event.alreadyInNcts,
            "eventDetails"  -> Json.toJson(event.eventDetails),
            "seals"         -> Json.toJson(event.seals)
          )

    }

  implicit def xmlWrites: XMLWrites[EnRouteEvent] = XMLWrites[EnRouteEvent] {
    enRouteEvent =>
      val buildSealsXml = enRouteEvent.seals match {
        case Some(seals) =>
          <SEAINFSF1>
            {
            <SeaNumSF12> {escapeXml(seals.size.toString)} </SeaNumSF12> ++
              seals.map(_.toXml)
            }
          </SEAINFSF1>
        case _ => NodeSeq.Empty
      }

      <ENROUEVETEV>
        {
          <PlaTEV10>{ escapeXml(enRouteEvent.place)}</PlaTEV10> ++
          <PlaTEV10LNG>{ LanguageCodeEnglish.code}</PlaTEV10LNG> ++
          <CouTEV13>{ enRouteEvent.countryCode }</CouTEV13>
        }
        <CTLCTL>
          {
            <AlrInNCTCTL29>{booleanToInt(enRouteEvent.alreadyInNcts)}</AlrInNCTCTL29>
          }
        </CTLCTL>
        {
        enRouteEvent.eventDetails.map {
          case incident: Incident                           => incident.toXml ++ buildSealsXml
          case containerTranshipment: ContainerTranshipment => buildSealsXml ++ containerTranshipment.toXml
          case vehicularTranshipment: VehicularTranshipment => buildSealsXml ++ vehicularTranshipment.toXml
        }.getOrElse(NodeSeq.Empty)
        }
      </ENROUEVETEV>
  }

  implicit lazy val xmlReader: XmlReader[EnRouteEvent] = (
    (xmlPath \ "PlaTEV10").read[String],
    (xmlPath \ "CouTEV13").read[String],
    (xmlPath \ "CTLCTL" \ "AlrInNCTCTL29").read(booleanFromIntReader),
    xmlPath.read[EventDetails].optional,
    (xmlPath \ "SEAINFSF1" \ "SEAIDSI1").read(strictReadOptionSeq[Seal])
  ).mapN(apply)
}
