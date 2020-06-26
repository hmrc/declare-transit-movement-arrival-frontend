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

package models.domain

import java.time.LocalDate

import models._
import models.messages.{ContainerTranshipment, EventDetails, Incident, VehicularTranshipment}
import models.reference.Country
import play.api.libs.json._

import scala.language.implicitConversions

sealed trait EventDetailsDomain

object EventDetailsDomain {

  def eventDetailsDomainToEventDetails(eventDetailsDomain: EventDetailsDomain): EventDetails =
    eventDetailsDomain match {
      case incident: IncidentDomain =>
        IncidentDomain.domainIncidentToIncident(incident)
      case container: ContainerTranshipmentDomain =>
        ContainerTranshipmentDomain.domainContainerTranshipmenttoContainerTranshipment(container)
      case vehicularTranshipment: VehicularTranshipmentDomain =>
        VehicularTranshipmentDomain.domainVehicularTranshipmentToVehicularTranshipment(vehicularTranshipment)
    }

  object Constants {
    val authorityLength = 35
    val placeLength     = 35
    val countryLength   = 2
  }

  implicit lazy val writes: OWrites[EventDetailsDomain] = OWrites {
    case i: IncidentDomain     => Json.toJsObject(i)(IncidentDomain.incidentJsonWrites)
    case t: TranshipmentDomain => Json.toJsObject(t)(TranshipmentDomain.transhipmentJsonWrites)
  }
}

final case class IncidentDomain(
  incidentInformation: Option[String],
  date: Option[LocalDate]   = None,
  authority: Option[String] = None,
  place: Option[String]     = None,
  country: Option[String]   = None
) extends EventDetailsDomain

object IncidentDomain {

  def domainIncidentToIncident(incident: IncidentDomain): Incident =
    IncidentDomain.unapply(incident).map((Incident.apply _).tupled).get

  object Constants {
    val informationLength = 350
  }

  implicit lazy val incidentJsonWrites: OWrites[IncidentDomain] = OWrites[IncidentDomain] {
    incident =>
      Json
        .obj(
          "incidentInformation" -> incident.incidentInformation,
          "date"                -> incident.date,
          "authority"           -> incident.authority,
          "place"               -> incident.place,
          "country"             -> incident.country,
          "isTranshipment"      -> false
        )
        .filterNulls
  }
}

sealed trait TranshipmentDomain extends EventDetailsDomain

object TranshipmentDomain {

  object Constants {
    val containerLength = 17
    val maxContainers   = 99
  }

  implicit lazy val transhipmentJsonWrites: OWrites[TranshipmentDomain] = OWrites {
    case t: VehicularTranshipmentDomain => Json.toJsObject(t)(VehicularTranshipmentDomain.vehicularTranshipmentJsonWrites)
    case t: ContainerTranshipmentDomain => Json.toJsObject(t)(ContainerTranshipmentDomain.containerJsonWrites)
  }
}

final case class VehicularTranshipmentDomain(
  transportIdentity: String,
  transportCountry: Country,
  date: Option[LocalDate]   = None,
  authority: Option[String] = None,
  place: Option[String]     = None,
  country: Option[String]   = None,
  containers: Option[Seq[ContainerDomain]]
) extends TranshipmentDomain

object VehicularTranshipmentDomain {

  object Constants {
    val transportIdentityLength = 27
    val transportCountryLength  = 2
  }

  def domainVehicularTranshipmentToVehicularTranshipment(transhipment: VehicularTranshipmentDomain): VehicularTranshipment =
    VehicularTranshipmentDomain
      .unapply(transhipment)
      .map {
        case _ @(transportIdentity, transportCountry, date, authority, place, country, container) =>
          VehicularTranshipment(
            transportIdentity,
            transportCountry.code,
            date,
            authority,
            place,
            country,
            container.map(_.map(ContainerDomain.domainContainerToContainer))
          )
      }
      .get

  implicit lazy val vehicularTranshipmentJsonWrites: OWrites[VehicularTranshipmentDomain] = {
    OWrites[VehicularTranshipmentDomain] {
      transhipment =>
        val transhipmentType: TranshipmentType =
          if (transhipment.containers.isDefined)
            TranshipmentType.DifferentContainerAndVehicle
          else
            TranshipmentType.DifferentVehicle

        Json
          .obj(
            "transportIdentity"    -> transhipment.transportIdentity,
            "transportNationality" -> transhipment.transportCountry,
            "date"                 -> transhipment.date,
            "authority"            -> transhipment.authority,
            "place"                -> transhipment.place,
            "country"              -> transhipment.country,
            "containers"           -> Json.toJson(transhipment.containers),
            "transhipmentType"     -> transhipmentType.toString,
            "isTranshipment"       -> true
          )
          .filterNulls
    }
  }
}

final case class ContainerTranshipmentDomain(
  date: Option[LocalDate]   = None,
  authority: Option[String] = None,
  place: Option[String]     = None,
  country: Option[Country]  = None,
  containers: Seq[ContainerDomain]
) extends TranshipmentDomain {
  require(containers.nonEmpty, "At least one container number must be provided")
}

object ContainerTranshipmentDomain {

  def domainContainerTranshipmenttoContainerTranshipment(transhipment: ContainerTranshipmentDomain): ContainerTranshipment =
    ContainerTranshipmentDomain
      .unapply(transhipment)
      .map {
        case _ @(date, authority, place, country, containers) =>
          ContainerTranshipment(
            date,
            authority,
            place,
            country.map(_.code),
            containers.map(ContainerDomain.domainContainerToContainer)
          )
      }
      .get

  implicit lazy val containerJsonWrites: OWrites[ContainerTranshipmentDomain] = OWrites {
    transhipment =>
      Json
        .obj(
          "date"             -> transhipment.date,
          "authority"        -> transhipment.authority,
          "place"            -> transhipment.place,
          "country"          -> transhipment.country,
          "containers"       -> transhipment.containers,
          "transhipmentType" -> TranshipmentType.DifferentContainer.toString,
          "isTranshipment"   -> true
        )
        .filterNulls
  }
}
