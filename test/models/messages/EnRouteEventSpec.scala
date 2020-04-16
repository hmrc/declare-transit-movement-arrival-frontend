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

import generators.MessagesModelGenerators
import models.LanguageCodeEnglish
import models.XMLWrites._
import models.messages.behaviours.JsonBehaviours
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.{FreeSpec, MustMatchers}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsObject, JsSuccess, Json}

import scala.xml.Utility.trim

class EnRouteEventSpec extends FreeSpec with MustMatchers with ScalaCheckPropertyChecks with MessagesModelGenerators with JsonBehaviours {

  "EnRouteEvent" - {

    "must create valid xml with Incident and seal" in {

      forAll(arbitrary[EnRouteEvent], arbitrary[Seal], arbitrary[Incident]) {
        (enRouteEvent, seal, incident) =>
          val enRouteEventWithSealAndIncident = enRouteEvent.copy(seals = Some(Seq(seal)), eventDetails = Some(incident))

          val result = {
            <ENROUEVETEV>
              <PlaTEV10>{enRouteEventWithSealAndIncident.place}</PlaTEV10>
              <PlaTEV10LNG>{LanguageCodeEnglish.code}</PlaTEV10LNG>
              <CouTEV13>{enRouteEventWithSealAndIncident.countryCode}</CouTEV13>
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
          }

          enRouteEventWithSealAndIncident.toXml.map(trim) mustBe result.map(trim)
      }
    }

    "must create valid xml with container transhipment and seal" in {

      forAll(arbitrary[EnRouteEvent], arbitrary[Seal], arbitrary[ContainerTranshipment]) {
        (enRouteEvent, seal, containerTranshipment) =>
          val enRouteEventWithContainer = enRouteEvent.copy(seals = Some(Seq(seal)), eventDetails = Some(containerTranshipment))

          val result = {
            <ENROUEVETEV>
              <PlaTEV10>{enRouteEventWithContainer.place}</PlaTEV10>
              <PlaTEV10LNG>{LanguageCodeEnglish.code}</PlaTEV10LNG>
              <CouTEV13>{enRouteEventWithContainer.countryCode}</CouTEV13>
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
          }

          enRouteEventWithContainer.toXml.map(trim) mustBe result.map(trim)
      }
    }

    "must create valid xml with vehicular transhipment with seal" in {

      forAll(arbitrary[EnRouteEvent], arbitrary[Seal], arbitrary[VehicularTranshipment]) {
        (enRouteEvent, seal, vehicularTranshipment) =>
          val enRouteEventWithVehicle = enRouteEvent.copy(seals = Some(Seq(seal)), eventDetails = Some(vehicularTranshipment))

          val result = {
            <ENROUEVETEV>
              <PlaTEV10>{enRouteEventWithVehicle.place}</PlaTEV10>
              <PlaTEV10LNG>{LanguageCodeEnglish.code}</PlaTEV10LNG>
              <CouTEV13>{enRouteEventWithVehicle.countryCode}</CouTEV13>
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
          }

          enRouteEventWithVehicle.toXml.map(trim) mustBe result.map(trim)
      }
    }
    "must deserialise" in {

      forAll(arbitrary[EnRouteEvent]) {
        enRouteEvent =>
          val json = createEnRouteEventJson(enRouteEvent)
          json.validate[EnRouteEvent] mustEqual JsSuccess(enRouteEvent)
      }
    }

    "must serialise" in {

      forAll(arbitrary[EnRouteEvent]) {
        enRouteEvent =>
          val json = createEnRouteEventJson(enRouteEvent)
          Json.toJson(enRouteEvent) mustEqual json
      }
    }
  }

  def createEnRouteEventJson(enRouteEvent: EnRouteEvent): JsObject =
    Json.obj(
      "place"         -> enRouteEvent.place,
      "countryCode"   -> enRouteEvent.countryCode,
      "alreadyInNcts" -> enRouteEvent.alreadyInNcts,
      "eventDetails"  -> Json.toJson(enRouteEvent.eventDetails),
      "seals"         -> Json.toJson(enRouteEvent.seals)
    )
}
