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

import generators.MessagesModelGenerators
import models.messages.{ContainerTranshipment, Incident, VehicularTranshipment}
import models.{TranshipmentType, _}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.{FreeSpec, MustMatchers, OptionValues}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsObject, Json}

class EventDetailsDomainSpec extends FreeSpec with MustMatchers with ScalaCheckPropertyChecks with MessagesModelGenerators {

  "IncidentDomain" - {

    "must serialise" in {

      forAll(arbitrary[IncidentDomain]) {
        incidentDomain =>
          val json = incidentDomainJson(incidentDomain)
          Json.toJson(incidentDomain)(IncidentDomain.incidentJsonWrites) mustEqual json
      }
    }

    "must convert to Incident model" in {

      forAll(arbitrary[IncidentDomain]) {
        incidentDomain =>
          IncidentDomain.domainIncidentToIncident(incidentDomain) mustBe an[Incident]
      }
    }

  }

  "ContainerTranshipmentDomain" - {

    "must serialise" in {

      forAll(arbitrary[ContainerTranshipmentDomain]) {
        containerTranshipmentDomain =>
          val json = Json.toJson(containerTranshipmentDomain)(ContainerTranshipmentDomain.containerJsonWrites)
          Json.toJson(containerTranshipmentDomain)(ContainerTranshipmentDomain.containerJsonWrites) mustEqual json
      }
    }

    "must convert to ContainerTranshipment model" in {

      forAll(arbitrary[ContainerTranshipmentDomain]) {
        containerTranshipmentDomain =>
          val result = ContainerTranshipmentDomain.domainContainerTranshipmenttoContainerTranshipment(containerTranshipmentDomain)

          result mustBe an[ContainerTranshipment]
      }
    }
  }

  "VehicularTranshipmentDomain" - {

    "must serialise" in {

      forAll(arbitrary[VehicularTranshipmentDomain]) {
        vehicularTranshipment =>
          val json = vehicularTranshipmentJson(vehicularTranshipment)

          Json.toJson(vehicularTranshipment)(VehicularTranshipmentDomain.vehicularTranshipmentJsonWrites) mustEqual json
      }
    }

    "must convert to VehicularTranshipment model" in {

      forAll(arbitrary[VehicularTranshipmentDomain]) {
        vehicularTranshipmentDomain =>
          val result = VehicularTranshipmentDomain.domainVehicularTranshipmentToVehicularTranshipment(vehicularTranshipmentDomain)

          result mustBe an[VehicularTranshipment]
      }
    }

  }

  "TranshipmentDomain" - {

    "must serialise from a Vehicular transhipment" in {

      forAll(arbitrary[VehicularTranshipmentDomain]) {
        vehicularTranshipment =>
          val json = vehicularTranshipmentJson(vehicularTranshipment)
          Json.toJson(vehicularTranshipment: TranshipmentDomain)(TranshipmentDomain.transhipmentJsonWrites) mustEqual json
      }
    }

    "must serialise from a Container transhipment" in {
      val transhipmentType = Json.obj("transhipmentType" -> TranshipmentType.DifferentContainer.toString)

      forAll(arbitrary[ContainerTranshipmentDomain]) {
        containerTranshipment =>
          val json = Json.toJson(containerTranshipment)(ContainerTranshipmentDomain.containerJsonWrites).as[JsObject] ++ transhipmentType
          Json.toJson(containerTranshipment: TranshipmentDomain)(TranshipmentDomain.transhipmentJsonWrites) mustEqual json
      }
    }
  }

  "EventDetailsDomain" - {

    "must serialise from an Incident" in {

      forAll(arbitrary[IncidentDomain]) {
        incident =>
          val json = incidentDomainJson(incident)
          Json.toJson(incident: EventDetailsDomain) mustEqual json
      }
    }

    "must serialise from a Vehicular transhipment" in {

      forAll(arbitrary[VehicularTranshipmentDomain]) {
        vehicularTranshipment =>
          val json = Json.toJson(vehicularTranshipment)(VehicularTranshipmentDomain.vehicularTranshipmentJsonWrites)
          Json.toJson(vehicularTranshipment: EventDetailsDomain) mustEqual json
      }
    }

    "must serialise from a Container transhipment" in {
      val additionalJsObject = Json.obj("transhipmentType" -> TranshipmentType.DifferentContainer.toString, "isTranshipment" -> true)

      forAll(arbitrary[ContainerTranshipmentDomain]) {
        containerTranshipment =>
          val json = Json.toJson(containerTranshipment)(ContainerTranshipmentDomain.containerJsonWrites).as[JsObject] ++ additionalJsObject
          Json.toJson(containerTranshipment: EventDetailsDomain) mustEqual json
      }
    }
  }

  private def incidentDomainJson(incident: IncidentDomain): JsObject =
    incident.incidentInformation match {
      case Some(information) =>
        Json.obj("incidentInformation" -> information, "isTranshipment" -> false)
      case _ =>
        Json.obj("isTranshipment" -> false)
    }

  private def vehicularTranshipmentJson(vehicularTranshipment: VehicularTranshipmentDomain): JsObject = {
    val transhipmentType = if (vehicularTranshipment.containers.isDefined) {
      TranshipmentType.DifferentContainerAndVehicle
    } else {
      TranshipmentType.DifferentVehicle
    }

    Json
      .obj(
        "transportIdentity"    -> vehicularTranshipment.transportIdentity,
        "transportNationality" -> Json.toJson(vehicularTranshipment.transportCountry),
        "containers"           -> Json.toJson(vehicularTranshipment.containers),
        "transhipmentType"     -> transhipmentType.toString,
        "isTranshipment"       -> true
      )
      .filterNulls
  }

}
