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

package models.domain

import models._
import models.messages.{ContainerTranshipment, EventDetails, IncidentWithInformation, IncidentWithoutInformation, VehicularTranshipment}
import models.reference.CountryCode
import play.api.libs.json._

sealed trait EventDetailsDomain

object EventDetailsDomain {

  def eventDetailsDomainToEventDetails(eventDetailsDomain: EventDetailsDomain): EventDetails =
    eventDetailsDomain match {
      case container: ContainerTranshipmentDomain =>
        ContainerTranshipmentDomain.domainContainerTranshipmenttoContainerTranshipment(container)
      case vehicularTranshipment: VehicularTranshipmentDomain =>
        VehicularTranshipmentDomain.domainVehicularTranshipmentToVehicularTranshipment(vehicularTranshipment)
      case incidentWithInformation: IncidentWithInformationDomain =>
        IncidentWithInformationDomain.domainIncidentToIncident(incidentWithInformation)
      case _: IncidentWithoutInformationDomain.type =>
        IncidentWithoutInformation()
    }

  object Constants {
    val authorityLength = 35
    val placeLength     = 35
  }

  implicit lazy val writes: OWrites[EventDetailsDomain] = OWrites {
    case i: IncidentDomain     => Json.toJsObject(i)(IncidentDomain.incidentDomainJsonWrites)
    case t: TranshipmentDomain => Json.toJsObject(t)(TranshipmentDomain.transhipmentJsonWrites)
  }
}

sealed trait IncidentDomain extends EventDetailsDomain

object IncidentDomain {

  implicit lazy val incidentDomainJsonWrites: OWrites[IncidentDomain] = OWrites {
    case i: IncidentWithInformationDomain         => Json.toJsObject(i)(IncidentWithInformationDomain.incidentWithInformationJsonWrites)
    case i: IncidentWithoutInformationDomain.type => Json.toJsObject(i)(IncidentWithoutInformationDomain.incidentWithoutInformationJsonWrites)
  }
}

case object IncidentWithoutInformationDomain extends IncidentDomain {

  implicit lazy val incidentWithoutInformationJsonWrites: OWrites[IncidentWithoutInformationDomain.type] = OWrites[IncidentWithoutInformationDomain.type] {
    _ =>
      Json.obj("isTranshipment" -> false)
  }

}

final case class IncidentWithInformationDomain(incidentInformation: String) extends IncidentDomain

object IncidentWithInformationDomain {

  def domainIncidentToIncident(incident: IncidentWithInformationDomain): IncidentWithInformation =
    IncidentWithInformationDomain
      .unapply(incident)
      .map {
        case _ @incidentInformation =>
          IncidentWithInformation(
            incidentInformation
          )
      }
      .get

  object Constants {
    val informationLength = 350
  }

  implicit lazy val incidentWithInformationJsonWrites: OWrites[IncidentWithInformationDomain] = OWrites[IncidentWithInformationDomain] {
    incident =>
      Json
        .obj(
          "incidentInformation" -> incident.incidentInformation,
          "isTranshipment"      -> false
        )
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
  transportCountry: CountryCode,
  containers: Option[Seq[ContainerDomain]]
) extends TranshipmentDomain

object VehicularTranshipmentDomain {

  object Constants {
    val transportIdentityLength = 27
  }

  def domainVehicularTranshipmentToVehicularTranshipment(transhipment: VehicularTranshipmentDomain): VehicularTranshipment =
    VehicularTranshipmentDomain
      .unapply(transhipment)
      .map {
        case (transportIdentity, transportCountry, container) =>
          VehicularTranshipment(
            transportIdentity,
            transportCountry,
            container.map(_.map(ContainerDomain.domainContainerToContainer))
          )
      }
      .get

  implicit lazy val vehicularTranshipmentJsonWrites: OWrites[VehicularTranshipmentDomain] =
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
            "containers"           -> Json.toJson(transhipment.containers),
            "transhipmentType"     -> transhipmentType.toString,
            "isTranshipment"       -> true
          )
          .filterNulls
    }
}

final case class ContainerTranshipmentDomain(containers: Seq[ContainerDomain]) extends TranshipmentDomain {
  require(containers.nonEmpty, "At least one container number must be provided")
}

object ContainerTranshipmentDomain {

  def domainContainerTranshipmenttoContainerTranshipment(transhipment: ContainerTranshipmentDomain): ContainerTranshipment =
    ContainerTranshipmentDomain
      .unapply(transhipment)
      .map {
        case _ @(containers) =>
          ContainerTranshipment(
            containers.map(ContainerDomain.domainContainerToContainer)
          )
      }
      .get

  implicit lazy val containerJsonWrites: OWrites[ContainerTranshipmentDomain] = OWrites {
    transhipment =>
      Json
        .obj(
          "containers"       -> transhipment.containers,
          "transhipmentType" -> TranshipmentType.DifferentContainer.toString,
          "isTranshipment"   -> true
        )
        .filterNulls
  }
}
