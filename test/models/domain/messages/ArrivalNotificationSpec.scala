/*
 * Copyright 2019 HM Revenue & Customs
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

package models.domain.messages

import java.time.LocalDate

import generators.DomainModelGenerators
import models.domain.behaviours.JsonBehaviours
import models.domain.{EnRouteEvent, ProcedureType, Trader}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.{FreeSpec, MustMatchers}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsError, JsSuccess, Json}

class ArrivalNotificationSpec extends FreeSpec with MustMatchers
  with ScalaCheckPropertyChecks with DomainModelGenerators with JsonBehaviours {

  "Normal notification" - {

    mustHaveDualReadsAndWrites(arbitrary[NormalNotification])

    "must deserialise when no customs sub-place or en-route events are present" in {
      forAll(arbitrary[NormalNotification]) {
        case  notification @ NormalNotification(mrn, place, date, _, trader, presentationOffice, _) =>

          val json = Json.obj(
            "procedure"               -> Json.toJson(ProcedureType.Normal),
            "movementReferenceNumber" -> mrn,
            "notificationPlace"       -> place,
            "notificationDate"        -> date,
            "trader"                  -> Json.toJson(trader),
            "presentationOffice"      -> presentationOffice
          )

          val expectedResult = notification.copy(customsSubPlace = None, enRouteEvents = Seq.empty)

          json.validate[NormalNotification] mustEqual JsSuccess(expectedResult)
      }
    }

    "must deserialise when customs sub place and en-route events are present" in {
      forAll(arbitrary[NormalNotification]) {
        case  notification @ NormalNotification(mrn, place, date, subPlace, trader, presentationOffice, events) =>

          val json = Json.obj(
            "procedure"               -> Json.toJson(ProcedureType.Normal),
            "movementReferenceNumber" -> mrn,
            "notificationPlace"       -> place,
            "notificationDate"        -> date,
            "customsSubPlace"         -> subPlace,
            "trader"                  -> Json.toJson(trader),
            "presentationOffice"      -> presentationOffice,
            "enRouteEvents"           -> Json.toJson(events)
          )

          json.validate[NormalNotification] mustEqual JsSuccess(notification)
      }
    }

    "must fail to deserialise when `procedure` is `simplified`" in {

      forAll(arbitrary[NormalNotification]) {
        case  NormalNotification(mrn, place, date, subPlace, trader, presentationOffice, events) =>

          val json = Json.obj(
            "procedure"               -> Json.toJson(ProcedureType.Simplified),
            "movementReferenceNumber" -> mrn,
            "notificationPlace"       -> place,
            "notificationDate"        -> date,
            "customsSubPlace"         -> subPlace,
            "trader"                  -> Json.toJson(trader),
            "presentationOffice"      -> presentationOffice,
            "enRouteEvents"           -> Json.toJson(events)
          )

          json.validate[NormalNotification] mustEqual JsError("procedure must be `normal`")
      }
    }

    "must serialise" in  {

      forAll(stringsWithMaxLength(17), arbitrary[NormalNotification]) {
        case (subPlace, arrival @ NormalNotification(mrn, place, date, _, trader, presentationOffice, events)) =>

          val notification = arrival.copy(customsSubPlace = Some(subPlace))

          val json = if (events.isEmpty) {
            Json.obj(
              "procedure"               -> Json.toJson(ProcedureType.Normal),
              "movementReferenceNumber" -> mrn,
              "notificationPlace"       -> place,
              "notificationDate"        -> date,
              "customsSubPlace"         -> subPlace,
              "trader"                  -> Json.toJson(trader),
              "presentationOffice"      -> presentationOffice
            )
          } else {
            Json.obj(
              "procedure"               -> Json.toJson(ProcedureType.Normal),
              "movementReferenceNumber" -> mrn,
              "notificationPlace"       -> place,
              "notificationDate"        -> date,
              "customsSubPlace"         -> subPlace,
              "trader"                  -> Json.toJson(trader),
              "presentationOffice"      -> presentationOffice,
              "enRouteEvents"           -> Json.toJson(events)
            )
          }

          Json.toJson(notification)(NormalNotification.writes) mustEqual json
      }
    }
  }

  "Simplified notification" - {

    mustHaveDualReadsAndWrites(arbitrary[SimplifiedNotification])

    "must deserialise when no approved location or en-route events are present" in {

      forAll(arbitrary[SimplifiedNotification]) {
        case notification @ SimplifiedNotification(mrn, place, date, _, trader, presentationOffice, _) =>

          val json = Json.obj(
            "procedure"               -> Json.toJson(ProcedureType.Simplified),
            "movementReferenceNumber" -> mrn,
            "notificationPlace"       -> place,
            "notificationDate"        -> date,
            "trader"                  -> Json.toJson(trader),
            "presentationOffice"      -> presentationOffice
          )

          val expectedResult = notification.copy(approvedLocation = None, enRouteEvents = Seq.empty)

          json.validate[SimplifiedNotification] mustEqual JsSuccess(expectedResult)
      }
    }

    "must deserialise when approved location and en-route events are present" in {
      forAll(arbitrary[SimplifiedNotification]) {
        case  notification @ SimplifiedNotification(mrn, place, date, approvedLocation, trader, presentationOffice, events) =>

          val json = Json.obj(
            "procedure"               -> Json.toJson(ProcedureType.Simplified),
            "movementReferenceNumber" -> mrn,
            "notificationPlace"       -> place,
            "notificationDate"        -> date,
            "approvedLocation"        -> approvedLocation,
            "trader"                  -> Json.toJson(trader),
            "presentationOffice"      -> presentationOffice,
            "enRouteEvents"           -> Json.toJson(events)
          )

          json.validate[SimplifiedNotification] mustEqual JsSuccess(notification)
      }
    }

    "must fail to deserialise when `procedure` is `normal`" in {

      forAll(arbitrary[SimplifiedNotification]) {
        case SimplifiedNotification(mrn, place, date, approvedLocation, trader, presentationOffice, events) =>

          val json = Json.obj(
            "procedure"               -> Json.toJson(ProcedureType.Normal),
            "movementReferenceNumber" -> mrn,
            "notificationPlace"       -> place,
            "notificationDate"        -> date,
            "approvedLocation"        -> approvedLocation,
            "trader"                  -> Json.toJson(trader),
            "presentationOffice"      -> presentationOffice,
            "enRouteEvents"           -> Json.toJson(events)
          )

          json.validate[SimplifiedNotification] mustEqual JsError("procedure must be `simplified`")
      }
    }

    "must serialise" in  {

      forAll(stringsWithMaxLength(17), arbitrary[SimplifiedNotification]) {
        case  (approvedLocation, notification @ SimplifiedNotification(mrn, place, date, _, trader, presentationOffice, events)) =>

          val json = if (events.isEmpty) {
            Json.obj(
              "procedure"               -> Json.toJson(ProcedureType.Simplified),
              "movementReferenceNumber" -> mrn,
              "notificationPlace"       -> place,
              "notificationDate"        -> date,
              "approvedLocation"        -> approvedLocation,
              "trader"                  -> Json.toJson(trader),
              "presentationOffice"      -> presentationOffice
            )
          } else {
            Json.obj(
              "procedure"               -> Json.toJson(ProcedureType.Simplified),
              "movementReferenceNumber" -> mrn,
              "notificationPlace"       -> place,
              "notificationDate"        -> date,
              "approvedLocation"        -> approvedLocation,
              "trader"                  -> Json.toJson(trader),
              "presentationOffice"      -> presentationOffice,
              "enRouteEvents"           -> Json.toJson(events)
            )
          }

          val simplifiedNotification = notification.copy(approvedLocation = Some(approvedLocation))

          Json.toJson(simplifiedNotification)(SimplifiedNotification.writes) mustEqual json
      }
    }
  }

  "Arrival Notification" - {

    "must deserialise to a Normal notification" in {

      val date = datesBetween(LocalDate.of(1900, 1, 1), LocalDate.now)

      forAll(arbitrary[String], arbitrary[String], date, arbitrary[Trader], arbitrary[String]) {
        (mrn, place, date, trader, presentationOffice) =>

          val json = Json.obj(
            "procedure"               -> Json.toJson(ProcedureType.Normal),
            "movementReferenceNumber" -> mrn,
            "notificationPlace"       -> place,
            "notificationDate"        -> date,
            "trader"                  -> Json.toJson(trader),
            "presentationOffice"      -> presentationOffice
          )

          val expectedResult = NormalNotification(mrn, place, date, None, trader, presentationOffice, Seq.empty)

          json.validate[ArrivalNotification] mustEqual JsSuccess(expectedResult)
      }
    }

    "must deserialise to a Simplified notification" in {

      val date = datesBetween(LocalDate.of(1900, 1, 1), LocalDate.now)

      forAll(arbitrary[String], arbitrary[String], date, arbitrary[Trader], arbitrary[String]) {
        (mrn, place, date, trader, presentationOffice) =>

          val json = Json.obj(
            "procedure"               -> Json.toJson(ProcedureType.Simplified),
            "movementReferenceNumber" -> mrn,
            "notificationPlace"       -> place,
            "notificationDate"        -> date,
            "trader"                  -> Json.toJson(trader),
            "presentationOffice"      -> presentationOffice
          )

          val expectedResult = SimplifiedNotification(mrn, place, date, None, trader, presentationOffice, Seq.empty)

          json.validate[ArrivalNotification] mustEqual JsSuccess(expectedResult)
      }
    }

    "must serialise from a Normal notification" in {

      val gen = for {
        mrn                <- arbitrary[String]
        place              <- arbitrary[String]
        date               <- datesBetween(LocalDate.of(1900, 1, 1), LocalDate.now)
        subPlace           <- arbitrary[String]
        trader             <- arbitrary[Trader]
        presentationOffice <- arbitrary[String]
        events             <- arbitrary[Seq[EnRouteEvent]]
      } yield (mrn, place, date, subPlace, trader, presentationOffice, events)

      forAll(gen) {
        case  (mrn, place, date, subPlace, trader, presentationOffice, events) =>

          val json = if (events.isEmpty) {
            Json.obj(
              "procedure"               -> Json.toJson(ProcedureType.Normal),
              "movementReferenceNumber" -> mrn,
              "notificationPlace"       -> place,
              "notificationDate"        -> date,
              "customsSubPlace"         -> subPlace,
              "trader"                  -> Json.toJson(trader),
              "presentationOffice"      -> presentationOffice
            )
          } else {
            Json.obj(
              "procedure"               -> Json.toJson(ProcedureType.Normal),
              "movementReferenceNumber" -> mrn,
              "notificationPlace"       -> place,
              "notificationDate"        -> date,
              "customsSubPlace"         -> subPlace,
              "trader"                  -> Json.toJson(trader),
              "presentationOffice"      -> presentationOffice,
              "enRouteEvents"           -> Json.toJson(events)
            )
          }

          val notification = NormalNotification(mrn, place, date, Some(subPlace), trader, presentationOffice, events)

          Json.toJson(notification: ArrivalNotification) mustEqual json
      }
    }

    "must serialise from a Simplified notification" in {

      val gen = for {
        mrn                <- arbitrary[String]
        place              <- arbitrary[String]
        date               <- datesBetween(LocalDate.of(1900, 1, 1), LocalDate.now)
        approvedLocation   <- arbitrary[String]
        trader             <- arbitrary[Trader]
        presentationOffice <- arbitrary[String]
        events             <- arbitrary[Seq[EnRouteEvent]]
      } yield (mrn, place, date, approvedLocation, trader, presentationOffice, events)

      forAll(gen) {
        case  (mrn, place, date, approvedLocation, trader, presentationOffice, events) =>

          val json = if (events.isEmpty) {
            Json.obj(
              "procedure"               -> Json.toJson(ProcedureType.Simplified),
              "movementReferenceNumber" -> mrn,
              "notificationPlace"       -> place,
              "notificationDate"        -> date,
              "approvedLocation"        -> approvedLocation,
              "trader"                  -> Json.toJson(trader),
              "presentationOffice"      -> presentationOffice
            )
          } else {
            Json.obj(
              "procedure"               -> Json.toJson(ProcedureType.Simplified),
              "movementReferenceNumber" -> mrn,
              "notificationPlace"       -> place,
              "notificationDate"        -> date,
              "approvedLocation"        -> approvedLocation,
              "trader"                  -> Json.toJson(trader),
              "presentationOffice"      -> presentationOffice,
              "enRouteEvents"           -> Json.toJson(events)
            )
          }

          val notification = SimplifiedNotification(mrn, place, date, Some(approvedLocation), trader, presentationOffice, events)

          Json.toJson(notification: ArrivalNotification) mustEqual json
      }
    }
  }
}
