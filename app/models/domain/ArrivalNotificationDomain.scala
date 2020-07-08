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

import java.time.LocalDate

import models.messages.{ProcedureType, Trader}
import models.reference.CustomsOffice
import models.{GoodsLocation, MovementReferenceNumber}
import pages._
import play.api.libs.json._
import queries.EventsQuery

import scala.language.implicitConversions

sealed trait ArrivalNotificationDomain {
  def trader: TraderDomain
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
                                    presentationOfficeId: String,
                                    presentationOfficeName: String,
                                    enRouteEvents: Option[Seq[EnRouteEventDomain]])
    extends ArrivalNotificationDomain {

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
  movementReferenceNumber: MovementReferenceNumber,
  notificationPlace: String,
  notificationDate: LocalDate,
  approvedLocation: String,
  trader: TraderDomain,
  presentationOfficeId: String,
  presentationOfficeName: String,
  enRouteEvents: Option[Seq[EnRouteEventDomain]]
) extends ArrivalNotificationDomain {

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
            GoodsLocationPage.toString             -> GoodsLocation.AuthorisedConsigneesLocation.toString,
            AuthorisedLocationPage.toString        -> notification.notificationPlace,
            ConsigneeNamePage.toString             -> notification.trader.name,
            ConsigneeEoriConfirmationPage.toString -> false, //TODO have a word with design, can we just show the EORI number page?
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
  }
}
