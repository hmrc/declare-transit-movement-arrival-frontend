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

import cats.syntax.all._
import com.lucidchart.open.xtract.XmlReader._
import com.lucidchart.open.xtract.{XmlReader, __ => xmlPath}
import models.XMLReads._
import models.XMLWrites._
import models.domain.EnRouteEventDomain
import models.reference.CountryCode
import models.{LanguageCodeEnglish, XMLWrites}

import scala.xml.NodeSeq

final case class EnRouteEvent(
  place: String,
  countryCode: CountryCode,
  alreadyInNcts: Boolean,
  eventDetails: EventDetails,
  seals: Option[Seq[Seal]]
)

object EnRouteEvent {

  object Constants {
    val placeLength = 35
    val sealsLength = 20
  }

  def enRouteEventToDomain(enRouteEvent: EnRouteEvent): EnRouteEventDomain =
    EnRouteEvent
      .unapply(enRouteEvent)
      .map {
        case _ @(place, country, alreadyInNct, eventDetails, seals) =>
          EnRouteEventDomain(
            place,
            country,
            alreadyInNct,
            EventDetails.buildEventDetailsDomain(eventDetails),
            seals.map(_.map(Seal.sealToDomain))
          )
      }
      .get

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
        <PlaTEV10>{escapeXml(enRouteEvent.place)}</PlaTEV10> ++
          <PlaTEV10LNG>{LanguageCodeEnglish.code}</PlaTEV10LNG> ++
          <CouTEV13>{enRouteEvent.countryCode.code}</CouTEV13>
      }
        <CTLCTL>
          {
        <AlrInNCTCTL29>{booleanToInt(enRouteEvent.alreadyInNcts)}</AlrInNCTCTL29>
      }
        </CTLCTL>
        {
        enRouteEvent.eventDetails match {
          case incidentWithInformation: IncidentWithInformation       => incidentWithInformation.toXml ++ buildSealsXml
          case incidentWithoutInformation: IncidentWithoutInformation => incidentWithoutInformation.toXml ++ buildSealsXml
          case containerTranshipment: ContainerTranshipment           => buildSealsXml ++ containerTranshipment.toXml
          case vehicularTranshipment: VehicularTranshipment           => buildSealsXml ++ vehicularTranshipment.toXml
        }
      }
      </ENROUEVETEV>
  }

  implicit lazy val xmlReader: XmlReader[EnRouteEvent] = (
    (xmlPath \ "PlaTEV10").read[String],
    (xmlPath \ "CouTEV13").read[String].map(CountryCode(_)),
    (xmlPath \ "CTLCTL" \ "AlrInNCTCTL29").read(booleanFromIntReader),
    xmlPath.read[EventDetails],
    (xmlPath \ "SEAINFSF1" \ "SEAIDSI1").read(strictReadOptionSeq[Seal])
  ).mapN(apply)
}
