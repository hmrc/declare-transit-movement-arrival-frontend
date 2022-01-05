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

import com.lucidchart.open.xtract.XmlReader
import generators.MessagesModelGenerators
import models.LanguageCodeEnglish
import models.XMLWrites._
import models.domain.{ContainerTranshipmentDomain, IncidentWithInformationDomain, VehicularTranshipmentDomain}
import models.messages.behaviours.JsonBehaviours
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.OptionValues._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.StreamlinedXmlEquality
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import utils.Format

import scala.xml.NodeSeq

class EventDetailsSpec
    extends AnyFreeSpec
    with Matchers
    with ScalaCheckPropertyChecks
    with MessagesModelGenerators
    with JsonBehaviours
    with StreamlinedXmlEquality {

  "Incident" - {

    "must read an IncidentWithInformation XML" in {
      forAll(arbitrary[IncidentWithInformation]) {
        incidentWithInformation =>
          val result = XmlReader.of[Incident].read(incidentWithInformation.toXml).toOption.value

          result mustBe incidentWithInformation
      }
    }

    "must read an IncidentWithoutInformation XML" in {
      forAll(arbitrary[IncidentWithoutInformation]) {
        incidentWithoutInformation =>
          val result = XmlReader.of[Incident].read(incidentWithoutInformation.toXml).toOption.value

          result mustBe incidentWithoutInformation
      }
    }

  }

  "IncidentWithInformation" - {

    "must serialise to xml and deserialise xml" in {
      forAll(arbitrary[IncidentWithInformation]) {
        incident =>
          val result = XmlReader.of[IncidentWithInformation].read(incident.toXml).toOption.value
          result mustEqual incident
      }
    }

    "must convert to IncidentWithInformationDomain model" in {
      forAll(arbitrary[IncidentWithInformation]) {
        incident =>
          IncidentWithInformation.incidentWithInformationToDomain(incident) mustBe an[IncidentWithInformationDomain]
      }
    }
  }

  "IncidentWithoutInformation" - {

    "must serialise to xml and deserialise xml" in {
      forAll(arbitrary[IncidentWithoutInformation]) {
        incident =>
          val result = XmlReader.of[IncidentWithoutInformation].read(incident.toXml).toOption.value
          result mustEqual incident
      }
    }
  }

  "ContainerTranshipment" - {

    "must create valid xml" in {

      forAll(arbitrary[ContainerTranshipment], arbitrary[Container], arbitrary[Container]) {
        (transhipment, container1, container2) =>
          val containerTranshipment: ContainerTranshipment =
            transhipment.copy(containers = Seq(container1, container2))

          val endorsementDateNode = containerTranshipment.date.map {
            date =>
              <EndDatSHP60>{Format.dateFormatted(date)}</EndDatSHP60>
          }
          val endorsementAuthority = containerTranshipment.authority.map {
            authority =>
              <EndAutSHP61>{authority}</EndAutSHP61>
          }
          val endorsementPlace = containerTranshipment.place.map {
            place =>
              <EndPlaSHP63>{place}</EndPlaSHP63>
          }
          val endorsementCountry = containerTranshipment.country.map {
            country =>
              <EndCouSHP65>{country}</EndCouSHP65>
          }

          val expectedResult =
            <TRASHP>
              {
              endorsementDateNode.getOrElse(NodeSeq.Empty) ++
                endorsementAuthority.getOrElse(NodeSeq.Empty)
            }
              <EndAutSHP61LNG>{LanguageCodeEnglish.code}</EndAutSHP61LNG>
              {
              endorsementPlace.getOrElse(NodeSeq.Empty)
            }
              <EndPlaSHP63LNG>{LanguageCodeEnglish.code}</EndPlaSHP63LNG>
              {
              endorsementCountry.getOrElse(NodeSeq.Empty)
            }
              <CONNR3>
                <ConNumNR31>{containerTranshipment.containers.head.containerNumber}</ConNumNR31>
              </CONNR3>
              <CONNR3>
                <ConNumNR31>{containerTranshipment.containers(1).containerNumber}</ConNumNR31>
              </CONNR3>
            </TRASHP>

          containerTranshipment.toXml mustEqual expectedResult
      }
    }

    "must read xml as Container Transhipment model" in {

      forAll(arbitrary[ContainerTranshipment], arbitrary[Container], arbitrary[Container]) {
        (transhipment, container1, container2) =>
          val containerTranshipment: ContainerTranshipment =
            transhipment.copy(containers = Seq(container1, container2))

          val endorsementDateNode = containerTranshipment.date.map {
            date =>
              <EndDatSHP60>{Format.dateFormatted(date)}</EndDatSHP60>
          }
          val endorsementAuthority = containerTranshipment.authority.map {
            authority =>
              <EndAutSHP61>{authority}</EndAutSHP61>
          }
          val endorsementPlace = containerTranshipment.place.map {
            place =>
              <EndPlaSHP63>{place}</EndPlaSHP63>
          }
          val endorsementCountry = containerTranshipment.country.map {
            country =>
              <EndCouSHP65>{country}</EndCouSHP65>
          }

          val xml =
            <TRASHP>
              {
              endorsementDateNode.getOrElse(NodeSeq.Empty) ++
                endorsementAuthority.getOrElse(NodeSeq.Empty)
            }
              <EndAutSHP61LNG>{LanguageCodeEnglish.code}</EndAutSHP61LNG>
              {
              endorsementPlace.getOrElse(NodeSeq.Empty)
            }
              <EndPlaSHP63LNG>{LanguageCodeEnglish.code}</EndPlaSHP63LNG>
              {
              endorsementCountry.getOrElse(NodeSeq.Empty)
            }
              <CONNR3>
                <ConNumNR31>{containerTranshipment.containers.head.containerNumber}</ConNumNR31>
              </CONNR3>
              <CONNR3>
                <ConNumNR31>{containerTranshipment.containers(1).containerNumber}</ConNumNR31>
              </CONNR3>
            </TRASHP>
          val result = XmlReader.of[ContainerTranshipment].read(xml).toOption.value
          result mustEqual containerTranshipment
      }
    }

    "must write to xml and read xml as Container Transshipment" in {
      forAll(arbitrary[ContainerTranshipment]) {
        containerTranshipment =>
          val result = XmlReader.of[ContainerTranshipment].read(containerTranshipment.toXml).toOption.value
          result mustEqual containerTranshipment
      }
    }

    "must fail to construct when given an empty sequence of containers" in {

      intercept[IllegalArgumentException] {
        ContainerTranshipment(containers = Seq.empty)
      }
    }

    "must convert to ContainerTranshipmentDomain model" in {
      forAll(arbitrary[ContainerTranshipment]) {
        containerTranshipment =>
          ContainerTranshipment.containerTranshipmentToDomain(containerTranshipment) mustBe an[ContainerTranshipmentDomain]
      }
    }
  }

  "VehicularTranshipment" - {

    "must create valid xml without containers" in {
      forAll(arbitrary[VehicularTranshipment]) {
        transhipment =>
          val vehicularTranshipment = transhipment.copy(containers = None)

          val endorsementDateNode = vehicularTranshipment.date.map {
            date =>
              <EndDatSHP60>{Format.dateFormatted(date)}</EndDatSHP60>
          }
          val endorsementAuthority = vehicularTranshipment.authority.map {
            authority =>
              <EndAutSHP61>{authority}</EndAutSHP61>
          }
          val endorsementPlace = vehicularTranshipment.place.map {
            place =>
              <EndPlaSHP63>{place}</EndPlaSHP63>
          }
          val endorsementCountry = vehicularTranshipment.country.map {
            country =>
              <EndCouSHP65>{country}</EndCouSHP65>
          }

          val expectedResult =
            <TRASHP>
              <NewTraMeaIdeSHP26>{vehicularTranshipment.transportIdentity}</NewTraMeaIdeSHP26>
              <NewTraMeaIdeSHP26LNG>{LanguageCodeEnglish.code}</NewTraMeaIdeSHP26LNG>
              <NewTraMeaNatSHP54>{vehicularTranshipment.transportCountry.code}</NewTraMeaNatSHP54>
              {
              endorsementDateNode.getOrElse(NodeSeq.Empty) ++
                endorsementAuthority.getOrElse(NodeSeq.Empty)
            }
              <EndAutSHP61LNG>{LanguageCodeEnglish.code}</EndAutSHP61LNG>
              {
              endorsementPlace.getOrElse(NodeSeq.Empty)
            }
              <EndPlaSHP63LNG>{LanguageCodeEnglish.code}</EndPlaSHP63LNG>
              {
              endorsementCountry.getOrElse(NodeSeq.Empty)
            }
            </TRASHP>

          vehicularTranshipment.toXml mustEqual expectedResult
      }
    }

    "must read xml as Vehicular transhipment without containers" in {
      forAll(arbitrary[VehicularTranshipment]) {
        transhipment =>
          val vehicularTranshipment = transhipment.copy(containers = None)

          val endorsementDateNode = vehicularTranshipment.date.map {
            date =>
              <EndDatSHP60>{Format.dateFormatted(date)}</EndDatSHP60>
          }
          val endorsementAuthority = vehicularTranshipment.authority.map {
            authority =>
              <EndAutSHP61>{authority}</EndAutSHP61>
          }
          val endorsementPlace = vehicularTranshipment.place.map {
            place =>
              <EndPlaSHP63>{place}</EndPlaSHP63>
          }
          val endorsementCountry = vehicularTranshipment.country.map {
            country =>
              <EndCouSHP65>{country}</EndCouSHP65>
          }

          val xml =
            <TRASHP>
              <NewTraMeaIdeSHP26>{vehicularTranshipment.transportIdentity}</NewTraMeaIdeSHP26>
              <NewTraMeaIdeSHP26LNG>{LanguageCodeEnglish.code}</NewTraMeaIdeSHP26LNG>
              <NewTraMeaNatSHP54>{vehicularTranshipment.transportCountry.code}</NewTraMeaNatSHP54>
              {
              endorsementDateNode.getOrElse(NodeSeq.Empty) ++
                endorsementAuthority.getOrElse(NodeSeq.Empty)
            }
              <EndAutSHP61LNG>{LanguageCodeEnglish.code}</EndAutSHP61LNG>
              {
              endorsementPlace.getOrElse(NodeSeq.Empty)
            }
              <EndPlaSHP63LNG>{LanguageCodeEnglish.code}</EndPlaSHP63LNG>
              {
              endorsementCountry.getOrElse(NodeSeq.Empty)
            }
            </TRASHP>

          val result = XmlReader.of[VehicularTranshipment].read(xml).toOption.value
          result mustEqual vehicularTranshipment
      }
    }

    "must create valid xml with containers" in {
      forAll(arbitrary[VehicularTranshipment], arbitrary[Container], arbitrary[Container]) {
        (transhipment, container1, container2) =>
          val vehicularTranshipment = transhipment.copy(containers = Some(Seq(container1, container2)))

          val endorsementDateNode = vehicularTranshipment.date.map {
            date =>
              <EndDatSHP60>{Format.dateFormatted(date)}</EndDatSHP60>
          }
          val endorsementAuthority = vehicularTranshipment.authority.map {
            authority =>
              <EndAutSHP61>{authority}</EndAutSHP61>
          }
          val endorsementPlace = vehicularTranshipment.place.map {
            place =>
              <EndPlaSHP63>{place}</EndPlaSHP63>
          }
          val endorsementCountry = vehicularTranshipment.country.map {
            country =>
              <EndCouSHP65>{country}</EndCouSHP65>
          }

          val expectedResult =
            <TRASHP>
              <NewTraMeaIdeSHP26>{vehicularTranshipment.transportIdentity}</NewTraMeaIdeSHP26>
              <NewTraMeaIdeSHP26LNG>{LanguageCodeEnglish.code}</NewTraMeaIdeSHP26LNG>
              <NewTraMeaNatSHP54>{vehicularTranshipment.transportCountry.code}</NewTraMeaNatSHP54>
              {
              endorsementDateNode.getOrElse(NodeSeq.Empty) ++
                endorsementAuthority.getOrElse(NodeSeq.Empty)
            }
              <EndAutSHP61LNG>{LanguageCodeEnglish.code}</EndAutSHP61LNG>
              {
              endorsementPlace.getOrElse(NodeSeq.Empty)
            }
              <EndPlaSHP63LNG>{LanguageCodeEnglish.code}</EndPlaSHP63LNG>
              {
              endorsementCountry.getOrElse(NodeSeq.Empty)
            }
              <CONNR3>
                <ConNumNR31>{vehicularTranshipment.containers.value.head.containerNumber}</ConNumNR31>
              </CONNR3>
              <CONNR3>
                <ConNumNR31>{vehicularTranshipment.containers.value(1).containerNumber}</ConNumNR31>
              </CONNR3>
            </TRASHP>

          vehicularTranshipment.toXml mustEqual expectedResult
      }
    }

    "must read xml as Vehicular transhipment with containers" in {
      forAll(arbitrary[VehicularTranshipment], arbitrary[Container], arbitrary[Container]) {
        (transhipment, container1, container2) =>
          val vehicularTranshipment = transhipment.copy(containers = Some(Seq(container1, container2)))

          val endorsementDateNode = vehicularTranshipment.date.map {
            date =>
              <EndDatSHP60>{Format.dateFormatted(date)}</EndDatSHP60>
          }
          val endorsementAuthority = vehicularTranshipment.authority.map {
            authority =>
              <EndAutSHP61>{authority}</EndAutSHP61>
          }
          val endorsementPlace = vehicularTranshipment.place.map {
            place =>
              <EndPlaSHP63>{place}</EndPlaSHP63>
          }
          val endorsementCountry = vehicularTranshipment.country.map {
            country =>
              <EndCouSHP65>{country}</EndCouSHP65>
          }

          val xml =
            <TRASHP>
              <NewTraMeaIdeSHP26>{vehicularTranshipment.transportIdentity}</NewTraMeaIdeSHP26>
              <NewTraMeaIdeSHP26LNG>{LanguageCodeEnglish.code}</NewTraMeaIdeSHP26LNG>
              <NewTraMeaNatSHP54>{vehicularTranshipment.transportCountry.code}</NewTraMeaNatSHP54>
              {
              endorsementDateNode.getOrElse(NodeSeq.Empty) ++
                endorsementAuthority.getOrElse(NodeSeq.Empty)
            }
              <EndAutSHP61LNG>{LanguageCodeEnglish.code}</EndAutSHP61LNG>
              {
              endorsementPlace.getOrElse(NodeSeq.Empty)
            }
              <EndPlaSHP63LNG>{LanguageCodeEnglish.code}</EndPlaSHP63LNG>
              {
              endorsementCountry.getOrElse(NodeSeq.Empty)
            }
              <CONNR3>
                <ConNumNR31>{vehicularTranshipment.containers.value.head.containerNumber}</ConNumNR31>
              </CONNR3>
              <CONNR3>
                <ConNumNR31>{vehicularTranshipment.containers.value(1).containerNumber}</ConNumNR31>
              </CONNR3>
            </TRASHP>

          val result = XmlReader.of[VehicularTranshipment].read(xml).toOption.value
          result mustEqual vehicularTranshipment
      }
    }

    "must write to xml and read xml as Vehicular transhipment" in {
      forAll(arbitrary[VehicularTranshipment]) {
        vehicularTranshipment =>
          val result = XmlReader.of[VehicularTranshipment].read(vehicularTranshipment.toXml).toOption.value
          result mustEqual vehicularTranshipment
      }
    }

    "must convert to VehicularTranshipment model" in {
      forAll(arbitrary[VehicularTranshipment]) {
        vehicularTranshipment =>
          val result = VehicularTranshipment.vehicularTranshipmentToDomain(vehicularTranshipment)

          result mustBe an[VehicularTranshipmentDomain]
      }
    }
  }
}
