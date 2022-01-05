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

package pages

import models.reference.CountryCode
import models.{Index, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours
import pages.events.{EventCountryPage, EventPlacePage, EventReportedPage}
import queries.EventsQuery

class IncidentOnRoutePageSpec extends PageBehaviours {

  private val eventIndex = Index(0)
  "IncidentOnRoutePage" - {

    beRetrievable[Boolean](IncidentOnRoutePage)

    beSettable[Boolean](IncidentOnRoutePage)

    beRemovable[Boolean](IncidentOnRoutePage)

    "must remove incident on route pages when user selects option 'No' for incidents on route question" in {
      forAll(arbitrary[UserAnswers], arbitrary[CountryCode], stringsWithMaxLength(35), arbitrary[Boolean]) {
        (answers, eventCountry, eventPlace, eventReported) =>
          val ua = answers
            .set(IncidentOnRoutePage, true)
            .success
            .value
            .set(EventCountryPage(eventIndex), eventCountry)
            .success
            .value
            .set(EventPlacePage(eventIndex), eventPlace)
            .success
            .value
            .set(EventReportedPage(eventIndex), eventReported)
            .success
            .value

          val result = ua.set(IncidentOnRoutePage, false).success.value

          result.get(EventsQuery) must not be defined
      }
    }

    "must not remove incident on route pages when user selects option 'Yes' for incidents on route question" in {
      forAll(arbitrary[UserAnswers], arbitrary[CountryCode], stringsWithMaxLength(35), arbitrary[Boolean]) {
        (answers, eventCountry, eventPlace, eventReported) =>
          val ua = answers
            .set(IncidentOnRoutePage, true)
            .success
            .value
            .set(EventCountryPage(eventIndex), eventCountry)
            .success
            .value
            .set(EventPlacePage(eventIndex), eventPlace)
            .success
            .value
            .set(EventReportedPage(eventIndex), eventReported)
            .success
            .value

          val result = ua.set(IncidentOnRoutePage, true).success.value

          result.get(EventCountryPage(eventIndex)) must be(defined)
          result.get(EventPlacePage(eventIndex)) must be(defined)
          result.get(EventReportedPage(eventIndex)) must be(defined)
      }

    }

    "must remove incident on route pages when incidents on route question is removed" in {
      forAll(arbitrary[UserAnswers], arbitrary[CountryCode], stringsWithMaxLength(35), arbitrary[Boolean]) {
        (answers, eventCountry, eventPlace, eventReported) =>
          val ua = answers
            .set(IncidentOnRoutePage, true)
            .success
            .value
            .set(EventCountryPage(eventIndex), eventCountry)
            .success
            .value
            .set(EventPlacePage(eventIndex), eventPlace)
            .success
            .value
            .set(EventReportedPage(eventIndex), eventReported)
            .success
            .value

          val result = ua.remove(IncidentOnRoutePage).success.value

          result.get(EventsQuery) must not be defined
      }

    }

  }
}
