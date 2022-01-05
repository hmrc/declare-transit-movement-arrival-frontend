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

import models.messages._
import play.api.libs.json.{Json, OWrites}
import models._
import models.reference.CountryCode

final case class EnRouteEventDomain(place: String,
                                    country: CountryCode,
                                    alreadyInNcts: Boolean,
                                    eventDetails: EventDetailsDomain,
                                    seals: Option[Seq[SealDomain]]
)

object EnRouteEventDomain {

  object Constants {
    val placeLength = 35
    val sealsLength = 20
  }

  def domainEnrouteEventToEnrouteEvent(enrouteEventDomain: EnRouteEventDomain): EnRouteEvent =
    EnRouteEventDomain
      .unapply(enrouteEventDomain)
      .map {
        case _ @(place, country, alreadyInNct, eventDetails, seals) =>
          EnRouteEvent(
            place,
            country,
            alreadyInNct,
            EventDetailsDomain.eventDetailsDomainToEventDetails(eventDetails),
            seals.map(_.map(SealDomain.domainSealToSeal))
          )
      }
      .get

  implicit lazy val writes: OWrites[EnRouteEventDomain] =
    OWrites[EnRouteEventDomain] {
      event =>
        val json = Json
          .obj(
            "eventPlace"       -> event.place,
            "eventReported"    -> event.alreadyInNcts,
            "eventCountry"     -> event.country,
            "seals"            -> Json.toJson(event.seals),
            "haveSealsChanged" -> event.seals.isDefined
          ) ++
          Json.toJsObject(event.eventDetails)

        json.filterNulls
    }

}
