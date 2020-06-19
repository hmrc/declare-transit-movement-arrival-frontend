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

import java.time.LocalDate

import models.{GoodsLocation, MovementReferenceNumber}
import models.reference.CustomsOffice
import pages._
import play.api.libs.json._
import queries.EventsQuery

import scala.language.implicitConversions

sealed trait ArrivalNotification

object ArrivalNotification {

  implicit lazy val writes: Writes[ArrivalNotification] = Writes {
    case n: NormalNotification     => Json.toJson(n)(NormalNotification.writes)
    case s: SimplifiedNotification => Json.toJson(s)(SimplifiedNotification.writes)
  }
}

final case class NormalNotification(movementReferenceNumber: MovementReferenceNumber,
                                    notificationPlace: String,
                                    notificationDate: LocalDate,
                                    customsSubPlace: Option[String],
                                    trader: Trader,
                                    presentationOfficeId: String,
                                    presentationOfficeName: String,
                                    enRouteEvents: Option[Seq[EnRouteEvent]])
    extends ArrivalNotification {

  val procedure: ProcedureType = ProcedureType.Normal
}

object NormalNotification {

  object Constants {
    val customsSubPlaceLength    = 17
    val notificationPlaceLength  = 35
    val presentationOfficeLength = 8
    val maxNumberOfEnRouteEvents = 9
  }

  implicit lazy val writes: OWrites[NormalNotification] =
    OWrites[NormalNotification] {
      notification =>
        Json
          .obj(
            GoodsLocationPage.toString       -> GoodsLocation.BorderForceOffice.toString,
            PlaceOfNotificationPage.toString -> notification.notificationPlace,
            CustomsSubPlacePage.toString     -> notification.customsSubPlace,
            TraderAddressPage.toString -> Json.obj(
              "buildingAndStreet" -> notification.trader.streetAndNumber,
              "city"              -> notification.trader.city,
              "postcode"          -> notification.trader.postCode
            ),
            IsTraderAddressPlaceOfNotificationPage.toString -> notification.notificationPlace.equalsIgnoreCase(notification.trader.postCode),
            PresentationOfficePage.toString -> Json.toJson(
              CustomsOffice(notification.presentationOfficeId, notification.presentationOfficeName, Seq.empty, None)),
            EventsQuery.toString         -> Json.toJson(notification.enRouteEvents),
            TraderEoriPage.toString      -> notification.trader.eori,
            TraderNamePage.toString      -> notification.trader.name,
            IncidentOnRoutePage.toString -> notification.enRouteEvents.isDefined
          )
    }
}

final case class SimplifiedNotification(
  movementReferenceNumber: String, // TODO: Make this a MovementReferenceNumber
  notificationPlace: String,
  notificationDate: LocalDate,
  approvedLocation: Option[String],
  trader: Trader,
  presentationOffice: String,
  enRouteEvents: Option[Seq[EnRouteEvent]]
) extends ArrivalNotification {

  val procedure: ProcedureType = ProcedureType.Simplified
}

object SimplifiedNotification {

  object Constants {
    val notificationPlaceLength  = 35
    val approvedLocationLength   = 17
    val presentationOfficeLength = 8
    val maxNumberOfEnRouteEvents = 9
    val authorisedLocationRegex  = "^[a-zA-Z0-9]*$"
  }

  implicit lazy val writes: OWrites[SimplifiedNotification] = {
    OWrites[SimplifiedNotification] {
      notification =>
        Json
          .obj(
            "procedure"               -> Json.toJson(notification.procedure),
            "movementReferenceNumber" -> notification.movementReferenceNumber,
            "notificationPlace"       -> notification.notificationPlace,
            "notificationDate"        -> notification.notificationDate,
            "approvedLocation"        -> notification.approvedLocation,
            "trader"                  -> Json.toJson(notification.trader),
            "presentationOffice"      -> notification.presentationOffice,
            "enRouteEvents"           -> Json.toJson(notification.enRouteEvents)
          )
    }
  }
}
