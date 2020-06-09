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

package pages

import base.SpecBase
import models.UserAnswers
import pages.behaviours.PageBehaviours
import org.scalacheck.Arbitrary.arbitrary

class ConsigneeEoriConfirmationPageSpec extends PageBehaviours with SpecBase {

  "EoriConfirmationPage" - {

    beRetrievable[Boolean](ConsigneeEoriConfirmationPage)

    beSettable[Boolean](ConsigneeEoriConfirmationPage)

    beRemovable[Boolean](ConsigneeEoriConfirmationPage)

    "cleanup" - {
      "remove EoriNumberPage when true" in {
        forAll(arbitrary[UserAnswers]) {
          userAnswers =>
            val result = userAnswers
              .set(ConsigneeEoriConfirmationPage, false)
              .success
              .value
              .set(ConsigneeEoriNumberPage, eoriNumber)
              .success
              .value
              .set(ConsigneeEoriConfirmationPage, true)
              .success
              .value
            result.get(ConsigneeEoriNumberPage) must not be defined
        }
      }
      "not remove EoriNumberPage when false" in {
        forAll(arbitrary[UserAnswers]) {
          userAnswers =>
            val result = userAnswers
              .set(ConsigneeEoriConfirmationPage, true)
              .success
              .value
              .set(ConsigneeEoriNumberPage, eoriNumber)
              .success
              .value
              .set(ConsigneeEoriConfirmationPage, false)
              .success
              .value
            result.get(ConsigneeEoriNumberPage) mustBe defined
        }
      }
    }
  }
}