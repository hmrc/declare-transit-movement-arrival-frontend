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

package pages.events

import models.{Index, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours

class EventReportedPageSpec extends PageBehaviours {

  val eventIndex = Index(0)

  "EventReportedPage" - {

    beRetrievable[Boolean](EventReportedPage(eventIndex))

    beSettable[Boolean](EventReportedPage(eventIndex))

    beRemovable[Boolean](EventReportedPage(eventIndex))

    "cleanup" - {
      "must remove incident information when IsTranshipmentPage is change to true" in {
        forAll(arbitrary[UserAnswers], arbitrary[Boolean], arbitrary[String]) {
          (userAnswers, eventReportedAnswer, incidentInformationAnswer) =>
            val ua = userAnswers
              .set(IsTranshipmentPage(eventIndex), false)
              .success
              .value
              .set(IncidentInformationPage(eventIndex), incidentInformationAnswer)
              .success
              .value
              .set(IsTranshipmentPage(eventIndex), true)
              .success
              .value

            val result = ua
              .set(EventReportedPage(eventIndex), eventReportedAnswer)
              .success
              .value

            result.get(IsTranshipmentPage(eventIndex)).value mustEqual true
            result.get(IncidentInformationPage(eventIndex)) must not be defined
        }
      }

      "must remove incident information data when EventReportedPage changes to true, IsTranshipmentPage is false, and the user has answered information" in {
        forAll(arbitrary[UserAnswers], arbitrary[String]) {
          (userAnswers, incidentInformation) =>
            val result = userAnswers
              .set(EventReportedPage(eventIndex), false)
              .success
              .value
              .set(IsTranshipmentPage(eventIndex), false)
              .success
              .value
              .set(IncidentInformationPage(eventIndex), incidentInformation)
              .success
              .value
              .set(EventReportedPage(eventIndex), true)
              .success
              .value

            result.get(IsTranshipmentPage(eventIndex)).value mustEqual false
            result.get(IncidentInformationPage(eventIndex)) must not be defined
        }
      }

      "must not remove incident information data when EventReportedPage is false, IsTranshipmentPage is false, and the user has answered information" in {
        forAll(arbitrary[UserAnswers], arbitrary[String]) {
          (userAnswers, incidentInformation) =>
            val ua = userAnswers
              .set(IsTranshipmentPage(eventIndex), false)
              .success
              .value
              .set(IncidentInformationPage(eventIndex), incidentInformation)
              .success
              .value

            val result = ua
              .set(EventReportedPage(eventIndex), false)
              .success
              .value

            result.get(IsTranshipmentPage(eventIndex)).value mustEqual false
            result.get(IncidentInformationPage(eventIndex)) must be(defined)
        }
      }

    }

  }
}
