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
import models.messages.behaviours.JsonBehaviours
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, StreamlinedXmlEquality}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class EnRouteEventSpec
    extends AnyFreeSpec
    with Matchers
    with ScalaCheckPropertyChecks
    with MessagesModelGenerators
    with JsonBehaviours
    with OptionValues
    with StreamlinedXmlEquality {

  "EnRouteEvent" - {

    "must create valid xml with IncidentWithInformation and seal" in {

      forAll(arbitrary[EnRouteEvent], arbitrary[Seal], arbitrary[IncidentWithInformation]) {
        (enRouteEvent, seal, incident) =>
          val enRouteEventWithSealAndIncident = enRouteEvent.copy(seals = Some(Seq(seal)), eventDetails = incident)

          val result =
            <ENROUEVETEV>
              <PlaTEV10>{enRouteEventWithSealAndIncident.place}</PlaTEV10>
              <PlaTEV10LNG>{LanguageCodeEnglish.code}</PlaTEV10LNG>
              <CouTEV13>{enRouteEventWithSealAndIncident.countryCode.code}</CouTEV13>
              <CTLCTL>
                <AlrInNCTCTL29>{if (enRouteEventWithSealAndIncident.alreadyInNcts) 1 else 0}</AlrInNCTCTL29>
              </CTLCTL>
              {
              incident.toXml
            }
              <SEAINFSF1>
                <SeaNumSF12>1</SeaNumSF12>
                {
              seal.toXml
            }
              </SEAINFSF1>
            </ENROUEVETEV>

          enRouteEventWithSealAndIncident.toXml mustEqual result
      }
    }

    "must create valid xml with container transhipment and seal" in {

      forAll(arbitrary[EnRouteEvent], arbitrary[Seal], arbitrary[ContainerTranshipment]) {
        (enRouteEvent, seal, containerTranshipment) =>
          val enRouteEventWithContainer = enRouteEvent.copy(seals = Some(Seq(seal)), eventDetails = containerTranshipment)

          val result =
            <ENROUEVETEV>
              <PlaTEV10>{enRouteEventWithContainer.place}</PlaTEV10>
              <PlaTEV10LNG>{LanguageCodeEnglish.code}</PlaTEV10LNG>
              <CouTEV13>{enRouteEventWithContainer.countryCode.code}</CouTEV13>
              <CTLCTL>
                <AlrInNCTCTL29>{if (enRouteEventWithContainer.alreadyInNcts) 1 else 0}</AlrInNCTCTL29>
              </CTLCTL>
              <SEAINFSF1>
                <SeaNumSF12>1</SeaNumSF12>
                {
              seal.toXml
            }
              </SEAINFSF1>
              {
              containerTranshipment.toXml
            }
            </ENROUEVETEV>

          enRouteEventWithContainer.toXml mustEqual result
      }
    }

    "must create valid xml with vehicular transhipment with seal" in {

      forAll(arbitrary[EnRouteEvent], arbitrary[Seal], arbitrary[VehicularTranshipment]) {
        (enRouteEvent, seal, vehicularTranshipment) =>
          val enRouteEventWithVehicle = enRouteEvent.copy(seals = Some(Seq(seal)), eventDetails = vehicularTranshipment)

          val result =
            <ENROUEVETEV>
              <PlaTEV10>{enRouteEventWithVehicle.place}</PlaTEV10>
              <PlaTEV10LNG>{LanguageCodeEnglish.code}</PlaTEV10LNG>
              <CouTEV13>{enRouteEventWithVehicle.countryCode.code}</CouTEV13>
              <CTLCTL>
                <AlrInNCTCTL29>{if (enRouteEventWithVehicle.alreadyInNcts) 1 else 0}</AlrInNCTCTL29>
              </CTLCTL>
              <SEAINFSF1>
                <SeaNumSF12>1</SeaNumSF12>
                {
              seal.toXml
            }
              </SEAINFSF1>
              {
              vehicularTranshipment.toXml
            }
            </ENROUEVETEV>

          enRouteEventWithVehicle.toXml mustEqual result
      }
    }

    "XML reader" - {

      "must read xml with vehicular transhipment with seal" in {

        forAll(arbitrary[EnRouteEvent], arbitrary[Seal], arbitrary[VehicularTranshipment]) {
          (enRouteEvent, seal, vehicularTranshipment) =>
            val enRouteEventWithVehicle = enRouteEvent.copy(seals = Some(Seq(seal)), eventDetails = vehicularTranshipment)
            val alreadyInNcts: Int      = if (enRouteEventWithVehicle.alreadyInNcts) 1 else 0
            val xml =
              <ENROUEVETEV>
                <PlaTEV10>{enRouteEventWithVehicle.place}</PlaTEV10>
                <PlaTEV10LNG>{LanguageCodeEnglish.code}</PlaTEV10LNG>
                <CouTEV13>{enRouteEventWithVehicle.countryCode.code}</CouTEV13>
                <CTLCTL><AlrInNCTCTL29>{alreadyInNcts}</AlrInNCTCTL29></CTLCTL>
                <SEAINFSF1>
                  <SeaNumSF12>1</SeaNumSF12>{seal.toXml}
                </SEAINFSF1>{vehicularTranshipment.toXml}
              </ENROUEVETEV>
            val result = XmlReader.of[EnRouteEvent].read(xml).toOption.value

            result mustEqual enRouteEventWithVehicle
        }
      }

      "must read xml as container transhipment and seal" in {

        forAll(arbitrary[EnRouteEvent], arbitrary[Seal], arbitrary[ContainerTranshipment]) {
          (enRouteEvent, seal, containerTranshipment) =>
            val enRouteEventWithContainer = enRouteEvent.copy(seals = Some(Seq(seal)), eventDetails = containerTranshipment)

            val xml =
              <ENROUEVETEV>
                <PlaTEV10>{enRouteEventWithContainer.place}</PlaTEV10>
                <PlaTEV10LNG>{LanguageCodeEnglish.code}</PlaTEV10LNG>
                <CouTEV13>{enRouteEventWithContainer.countryCode.code}</CouTEV13>
                <CTLCTL>
                  <AlrInNCTCTL29>{if (enRouteEventWithContainer.alreadyInNcts) 1 else 0}</AlrInNCTCTL29>
                </CTLCTL>
                <SEAINFSF1>
                  <SeaNumSF12>1</SeaNumSF12>
                  {
                seal.toXml
              }
                </SEAINFSF1>
                {
                containerTranshipment.toXml
              }
              </ENROUEVETEV>

            val result = XmlReader.of[EnRouteEvent].read(xml).toOption.value

            result mustBe enRouteEventWithContainer
        }
      }

      "must read xml as IncidentWithInformation and seal" in {

        forAll(arbitrary[EnRouteEvent], arbitrary[Seal], arbitrary[IncidentWithInformation]) {
          (enRouteEvent, seal, incident) =>
            val enRouteEventWithSealAndIncident = enRouteEvent.copy(seals = Some(Seq(seal)), eventDetails = incident)

            val xml =
              <ENROUEVETEV>
                <PlaTEV10>{enRouteEventWithSealAndIncident.place}</PlaTEV10>
                <PlaTEV10LNG>{LanguageCodeEnglish.code}</PlaTEV10LNG>
                <CouTEV13>{enRouteEventWithSealAndIncident.countryCode.code}</CouTEV13>
                <CTLCTL>
                  <AlrInNCTCTL29>{if (enRouteEventWithSealAndIncident.alreadyInNcts) 1 else 0}</AlrInNCTCTL29>
                </CTLCTL>
                {
                incident.toXml
              }
                <SEAINFSF1>
                  <SeaNumSF12>1</SeaNumSF12>
                  {
                seal.toXml
              }
                </SEAINFSF1>
              </ENROUEVETEV>

            val result = XmlReader.of[EnRouteEvent].read(xml).toOption.value

            result mustBe enRouteEventWithSealAndIncident
        }
      }

      "must write and read xml as EnRouteEvent" in {
        forAll(arbitrary[EnRouteEvent]) {
          enRouteEvent =>
            val result = XmlReader.of[EnRouteEvent].read(enRouteEvent.toXml).toOption.value
            result mustBe enRouteEvent
        }
      }
    }
  }

}
