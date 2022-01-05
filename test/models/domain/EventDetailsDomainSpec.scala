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

import generators.MessagesModelGenerators
import models.messages.{ContainerTranshipment, IncidentWithInformation, VehicularTranshipment}
import models.{TranshipmentType, _}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsObject, Json}

class EventDetailsDomainSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with MessagesModelGenerators {

  "IncidentDomain" - {

    "must serialise from an IncidentWithInformation" in {
      forAll(arbitrary[IncidentWithInformationDomain]) {
        incidentWithInformationDomain =>
          val json = Json.obj("incidentInformation" -> incidentWithInformationDomain.incidentInformation, "isTranshipment" -> false)
          Json.toJson(incidentWithInformationDomain: IncidentDomain)(IncidentDomain.incidentDomainJsonWrites) mustEqual json
      }
    }

    "must serialise from an IncidentWithoutInformation" in {
      forAll(arbitrary[IncidentWithoutInformationDomain.type]) {
        incidentWithoutInformationDomain =>
          val json = Json.obj("isTranshipment" -> false)
          Json.toJson(incidentWithoutInformationDomain: IncidentDomain)(IncidentDomain.incidentDomainJsonWrites) mustEqual json
      }
    }

  }

  "IncidentWithInformation" - {

    "must serialise" in {

      forAll(arbitrary[IncidentWithInformationDomain]) {
        incidentWithInformationDomain =>
          val json = Json.obj("incidentInformation" -> incidentWithInformationDomain.incidentInformation, "isTranshipment" -> false)
          Json.toJson(incidentWithInformationDomain)(IncidentWithInformationDomain.incidentWithInformationJsonWrites) mustEqual json
      }
    }

    "must convert to IncidentWithInformation model" in {

      forAll(arbitrary[IncidentWithInformationDomain]) {
        incidentDomain =>
          IncidentWithInformationDomain.domainIncidentToIncident(incidentDomain) mustBe an[IncidentWithInformation]
      }
    }
  }

  "IncidentWithoutInformation" - {

    "must serialise" in {

      forAll(arbitrary[IncidentWithoutInformationDomain.type]) {
        incidentWithoutInformationDomain =>
          val json = Json.obj("isTranshipment" -> false)
          Json.toJson(incidentWithoutInformationDomain)(IncidentWithoutInformationDomain.incidentWithoutInformationJsonWrites) mustEqual json
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

      forAll(arbitrary[IncidentWithInformationDomain]) {
        incidentDomain =>
          val json = Json.obj("incidentInformation" -> incidentDomain.incidentInformation, "isTranshipment" -> false)
          Json.toJson(incidentDomain: EventDetailsDomain) mustEqual json
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
