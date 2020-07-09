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
import models.GoodsLocation
import models.messages.behaviours.JsonBehaviours
import models.reference.CustomsOffice
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.{FreeSpec, MustMatchers}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._
import play.api.libs.json.{JsObject, Json}
import queries.EventsQuery

class ArrivalNotificationDomainSpec extends FreeSpec with MustMatchers with ScalaCheckPropertyChecks with MessagesModelGenerators with JsonBehaviours {

  "Normal notification" - {

    "must serialise" in {

      forAll(arbitrary[NormalNotification]) {
        normalNotification =>
          val json = createNormalNotificationJson(normalNotification)
          Json.toJson(normalNotification)(NormalNotification.writes) mustEqual json
      }
    }
  }

  "Simplified notification" - {

    "must serialise" in {

      forAll(arbitrary[SimplifiedNotification]) {
        simplifiedNotification =>
          val json = createSimplifiedNotificationJson(simplifiedNotification)
          Json.toJson(simplifiedNotification)(SimplifiedNotification.writes) mustEqual json
      }
    }
  }

  "Arrival Notification" - {

    "must serialise from a Normal notification" in {

      forAll(arbitrary[NormalNotification]) {
        normalNotification =>
          val json = createNormalNotificationJson(normalNotification)
          Json.toJson(normalNotification: ArrivalNotificationDomain) mustEqual json
      }
    }

    "must serialise from a Simplified notification" in {

      forAll(arbitrary[SimplifiedNotification]) {
        simplifiedNotification =>
          val json = createSimplifiedNotificationJson(simplifiedNotification)
          Json.toJson(simplifiedNotification: ArrivalNotificationDomain) mustEqual json
      }
    }
  }

  private def createNormalNotificationJson(notification: NormalNotification): JsObject =
    Json.obj(
      PlaceOfNotificationPage.toString -> notification.notificationPlace,
      CustomsSubPlacePage.toString     -> notification.customsSubPlace,
      TraderAddressPage.toString -> Json.obj(
        "buildingAndStreet" -> notification.trader.streetAndNumber,
        "city"              -> notification.trader.city,
        "postcode"          -> notification.trader.postCode
      ),
      PresentationOfficePage.toString                 -> Json.toJson(CustomsOffice(notification.presentationOfficeId, notification.presentationOfficeName, Seq.empty, None)),
      EventsQuery.toString                            -> Json.toJson(notification.enRouteEvents),
      TraderEoriPage.toString                         -> notification.trader.eori,
      TraderNamePage.toString                         -> notification.trader.name,
      GoodsLocationPage.toString                      -> GoodsLocation.BorderForceOffice.toString,
      IsTraderAddressPlaceOfNotificationPage.toString -> notification.notificationPlace.equalsIgnoreCase(notification.trader.postCode),
      IncidentOnRoutePage.toString                    -> notification.enRouteEvents.isDefined
    )

  private def createSimplifiedNotificationJson(notification: SimplifiedNotification): JsObject =
    Json.obj(
      GoodsLocationPage.toString             -> GoodsLocation.AuthorisedConsigneesLocation.toString,
      AuthorisedLocationPage.toString        -> notification.approvedLocation,
      ConsigneeNamePage.toString             -> notification.trader.name,
      ConsigneeEoriConfirmationPage.toString -> false,
      ConsigneeEoriNumberPage.toString       -> notification.trader.eori,
      ConsigneeAddressPage.toString -> Json.obj(
        "buildingAndStreet" -> notification.trader.streetAndNumber,
        "city"              -> notification.trader.city,
        "postcode"          -> notification.trader.postCode
      ),
      PresentationOfficePage.toString -> Json.toJson(
        CustomsOffice(notification.presentationOfficeId, notification.presentationOfficeName, Seq.empty, None)
      ),
      IncidentOnRoutePage.toString -> notification.enRouteEvents.isDefined,
      EventsQuery.toString         -> Json.toJson(notification.enRouteEvents)
    )
}
