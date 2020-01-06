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

package pages.events

import models.{TranshipmentType, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours
import pages.events.transhipments.{TranshipmentTypePage, TransportIdentityPage, TransportNationalityPage}

class EventReportedPageSpec extends PageBehaviours {

  val index = 0

  "EventReportedPage" - {

    beRetrievable[Boolean](EventReportedPage(index))

    beSettable[Boolean](EventReportedPage(index))

    beRemovable[Boolean](EventReportedPage(index))

    "cleanup" - {
      "must remove incident information when IsTranshipmentPage is true" in {
        forAll(arbitrary[UserAnswers], arbitrary[Boolean], arbitrary[String]) {
          (userAnswers, eventReportedAnswer, incidentInformationAnswer) =>
            val ua = userAnswers
              .set(IsTranshipmentPage(index), true)
              .success
              .value
              .set(IncidentInformationPage(index), incidentInformationAnswer)
              .success
              .value

            val result = ua
              .set(EventReportedPage(index), eventReportedAnswer)
              .success
              .value

            result.get(IsTranshipmentPage(index)).value mustEqual true
            result.get(IncidentInformationPage(index)) must not be defined
        }
      }

      "must remove incident information data when EventReportedPage is true, IsTranshipmentPage is false, and the user has answered information" in {
        forAll(arbitrary[UserAnswers], arbitrary[Boolean], arbitrary[String]) {
          (userAnswers, eventReportedAnswer, incidentInformation) =>
            val ua = userAnswers
              .set(IsTranshipmentPage(index), false)
              .success
              .value
              .set(IncidentInformationPage(index), incidentInformation)
              .success
              .value

            val result = ua
              .set(EventReportedPage(index), true)
              .success
              .value

            result.get(IsTranshipmentPage(index)).value mustEqual false
            result.get(IncidentInformationPage(index)) must not be defined
        }
      }

      "must not remove incident information data when EventReportedPage is false, IsTranshipmentPage is false, and the user has answered information" in {
        forAll(arbitrary[UserAnswers], arbitrary[String]) {
          (userAnswers, incidentInformation) =>
            val ua = userAnswers
              .set(IsTranshipmentPage(index), false)
              .success
              .value
              .set(IncidentInformationPage(index), incidentInformation)
              .success
              .value

            val result = ua
              .set(EventReportedPage(index), false)
              .success
              .value

            result.get(IsTranshipmentPage(index)).value mustEqual false
            result.get(IncidentInformationPage(index)) must be(defined)
        }
      }

    }

  }
}
