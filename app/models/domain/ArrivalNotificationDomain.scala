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

import java.time.LocalDate

import models.messages.ProcedureType
import models.reference.CustomsOffice
import models.{EoriNumber, GoodsLocation, MovementReferenceNumber}
import pages._
import play.api.libs.json._
import queries.EventsQuery

sealed trait ArrivalNotificationDomain {
  def movementReferenceNumber: MovementReferenceNumber
  def trader: TraderDomain
  def notificationDate: LocalDate
  def customsOffice: CustomsOffice
}

object ArrivalNotificationDomain {

  implicit lazy val writes: OWrites[ArrivalNotificationDomain] = OWrites {
    case n: NormalNotification     => Json.toJsObject(n)(NormalNotification.writes)
    case s: SimplifiedNotification => Json.toJsObject(s)(SimplifiedNotification.writes)
  }
}

final case class NormalNotification(movementReferenceNumber: MovementReferenceNumber,
                                    notificationPlace: String,
                                    notificationDate: LocalDate,
                                    customsSubPlace: String,
                                    trader: TraderDomain,
                                    customsOffice: CustomsOffice,
                                    enRouteEvents: Option[Seq[EnRouteEventDomain]]
) extends ArrivalNotificationDomain {

  val procedure: ProcedureType = ProcedureType.Normal
}

object NormalNotification {

  object Constants {
    val customsSubPlaceLength    = 17
    val notificationPlaceLength  = 35
    val customsOfficeLength      = 8
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
            CustomsOfficePage.toString                      -> Json.toJson(notification.customsOffice),
            EventsQuery.toString                            -> Json.toJson(notification.enRouteEvents),
            TraderEoriPage.toString                         -> notification.trader.eori,
            TraderNamePage.toString                         -> notification.trader.name,
            IncidentOnRoutePage.toString                    -> notification.enRouteEvents.isDefined
          )
    }
}

final case class SimplifiedNotification(
  movementReferenceNumber: MovementReferenceNumber,
  notificationPlace: String,
  notificationDate: LocalDate,
  authorisedLocation: String,
  trader: TraderDomain,
  customsOffice: CustomsOffice,
  enRouteEvents: Option[Seq[EnRouteEventDomain]],
  authedEori: EoriNumber
) extends ArrivalNotificationDomain {

  val procedure: ProcedureType = ProcedureType.Simplified
}

object SimplifiedNotification {

  object Constants {
    val notificationPlaceLength  = 35
    val approvedLocationLength   = 17
    val customsOfficeLength      = 8
    val maxNumberOfEnRouteEvents = 9
  }

  implicit lazy val writes: OWrites[SimplifiedNotification] = OWrites[SimplifiedNotification] {
    notification =>
      Json
        .obj(
          GoodsLocationPage.toString       -> GoodsLocation.AuthorisedConsigneesLocation.toString,
          AuthorisedLocationPage.toString  -> notification.authorisedLocation,
          ConsigneeNamePage.toString       -> notification.trader.name,
          ConsigneeEoriNumberPage.toString -> notification.trader.eori,
          ConsigneeAddressPage.toString -> Json.obj(
            "buildingAndStreet" -> notification.trader.streetAndNumber,
            "city"              -> notification.trader.city,
            "postcode"          -> notification.trader.postCode
          ),
          CustomsOfficePage.toString   -> Json.toJson(notification.customsOffice),
          IncidentOnRoutePage.toString -> notification.enRouteEvents.isDefined,
          EventsQuery.toString         -> Json.toJson(notification.enRouteEvents)
        )
  }
}
