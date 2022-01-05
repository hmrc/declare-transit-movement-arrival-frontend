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

import models.UserAnswers
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours

class IsTraderAddressPlaceOfNotificationPageSpec extends PageBehaviours {

  "IsTraderAddressPlaceOfNotificationPage" - {

    beRetrievable[Boolean](IsTraderAddressPlaceOfNotificationPage)

    beSettable[Boolean](IsTraderAddressPlaceOfNotificationPage)

    beRemovable[Boolean](IsTraderAddressPlaceOfNotificationPage)

    "must clean down 'PlaceOfNotificationPage'" - {
      "when changes to 'true'" in {
        forAll(arbitrary[UserAnswers], stringsWithMaxLength(35)) {
          (answers, placeOfNotification) =>
            val ua = answers
              .set(IsTraderAddressPlaceOfNotificationPage, false)
              .success
              .value
              .set(PlaceOfNotificationPage, placeOfNotification)
              .success
              .value

            val result = ua.set(IsTraderAddressPlaceOfNotificationPage, true).success.value

            result.get(PlaceOfNotificationPage) must not be defined
        }
      }

      "when remove is called" in {
        forAll(arbitrary[UserAnswers], stringsWithMaxLength(35)) {
          (answers, placeOfNotification) =>
            val ua = answers
              .set(IsTraderAddressPlaceOfNotificationPage, false)
              .success
              .value
              .set(PlaceOfNotificationPage, placeOfNotification)
              .success
              .value

            val result = ua.remove(IsTraderAddressPlaceOfNotificationPage).success.value

            result.get(PlaceOfNotificationPage) must not be defined
        }

      }
    }

    "must not clean down 'PlaceOfNotificationPage' when 'false' and page is defined" in {
      forAll(arbitrary[UserAnswers], stringsWithMaxLength(35)) {
        (answers, placeOfNotification) =>
          val ua = answers
            .set(PlaceOfNotificationPage, placeOfNotification)
            .success
            .value

          val result = ua.set(IsTraderAddressPlaceOfNotificationPage, false).success.value

          result.get(PlaceOfNotificationPage) mustBe defined
      }
    }
  }
}
