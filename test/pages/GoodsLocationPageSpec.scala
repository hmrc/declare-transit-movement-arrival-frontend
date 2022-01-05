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

import models.GoodsLocation._
import models.{GoodsLocation, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours

class GoodsLocationPageSpec extends PageBehaviours {

  "GoodsLocationPage" - {

    beRetrievable[GoodsLocation](GoodsLocationPage)

    beSettable[GoodsLocation](GoodsLocationPage)

    beRemovable[GoodsLocation](GoodsLocationPage)

    "must remove Authorised Location Code when the user changes to Border Force office" in {

      forAll(arbitrary[UserAnswers], arbitrary[String]) {
        (answers, location) =>
          val result = answers
            .set(GoodsLocationPage, AuthorisedConsigneesLocation)
            .success
            .value
            .set(AuthorisedLocationPage, location)
            .success
            .value
            .set(GoodsLocationPage, BorderForceOffice)
            .success
            .value

          result.get(AuthorisedLocationPage) must not be defined
          result.get(ConsigneeNamePage) must not be defined
          result.get(ConsigneeEoriNumberPage) must not be defined
          result.get(ConsigneeAddressPage) must not be defined
          result.get(CustomsOfficePage) must not be defined

      }
    }

    "must remove Customs Sub Place when the user changes to Authorised Consignees Location" in {

      forAll(arbitrary[UserAnswers], arbitrary[String]) {
        (answers, location) =>
          val result = answers
            .set(GoodsLocationPage, BorderForceOffice)
            .success
            .value
            .set(AuthorisedLocationPage, location)
            .success
            .value
            .set(GoodsLocationPage, AuthorisedConsigneesLocation)
            .success
            .value

          result.get(CustomsSubPlacePage) must not be defined
          result.get(CustomsOfficePage) must not be defined
          result.get(TraderNamePage) must not be defined
          result.get(TraderEoriPage) must not be defined
          result.get(TraderAddressPage) must not be defined
          result.get(IsTraderAddressPlaceOfNotificationPage) must not be defined
          result.get(PlaceOfNotificationPage) must not be defined
          result.get(CustomsOfficePage) must not be defined

      }
    }
  }
}
