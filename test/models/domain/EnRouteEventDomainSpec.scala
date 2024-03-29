/*
 * Copyright 2023 HM Revenue & Customs
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
import models._
import models.messages.EnRouteEvent
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json

class EnRouteEventDomainSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with MessagesModelGenerators with OptionValues {

  "must serialise" in {

    forAll(arbitrary[EnRouteEventDomain]) {
      enRouteEvent =>
        val json = Json.obj(
          "eventPlace"       -> enRouteEvent.place,
          "eventReported"    -> enRouteEvent.alreadyInNcts,
          "eventCountry"     -> enRouteEvent.country,
          "seals"            -> Json.toJson(enRouteEvent.seals),
          "haveSealsChanged" -> enRouteEvent.seals.isDefined
        ) ++ Json.toJsObject(enRouteEvent.eventDetails)

        Json.toJson(enRouteEvent) mustEqual json.filterNulls
    }
  }

  "must convert to EnRouteEvent model" in {

    forAll(arbitrary[EnRouteEventDomain]) {
      enRouteEventDomain =>
        EnRouteEventDomain.domainEnrouteEventToEnrouteEvent(enRouteEventDomain) mustBe an[EnRouteEvent]
    }
  }
}
