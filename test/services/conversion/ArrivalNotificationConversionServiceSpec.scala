/*
 * Copyright 2019 HM Revenue & Customs
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

package services.conversion

import base.SpecBase
import generators.{DomainModelGenerators, Generators, UserAnswersGenerator}
import models.GoodsLocation.BorderForceOffice
import models.{IncidentOnRoute, TraderAddress, UserAnswers}
import models.domain.messages.NormalNotification
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.{CustomsSubPlacePage, GoodsLocationPage, IncidentOnRoutePage, MovementReferenceNumberPage, PresentationOfficePage, TraderAddressPage, TraderEoriPage, TraderNamePage}

class ArrivalNotificationConversionServiceSpec extends SpecBase with ScalaCheckPropertyChecks
  with Generators with DomainModelGenerators with UserAnswersGenerator {

  val service = injector.instanceOf[ArrivalNotificationConversionService]

  "convertToArrivalNotification" - {

    "constructs an Arrival Notification message from a empty userAnswers" in {
      service.convertToArrivalNotification(emptyUserAnswers) mustEqual(None)
    }
    "constructs an Arrival Notification message from a userAnswers" in {
      forAll(arbitrary[NormalNotification], generatorTraderWithEoriAllValues, Gen.alphaNumStr) {
        case (arbArrivalNotification, trader, subplace) =>

          val arrivalNotification: NormalNotification = arbArrivalNotification.copy(trader = trader).copy(customsSubPlace = Some(subplace))

          val userAnswers: UserAnswers =
            emptyUserAnswers
              .set(MovementReferenceNumberPage, arrivalNotification.movementReferenceNumber).success.value
              .set(GoodsLocationPage, BorderForceOffice).success.value
              .set(PresentationOfficePage, arrivalNotification.presentationOffice).success.value
              .set(CustomsSubPlacePage, subplace).success.value
              .set(TraderNamePage, trader.name.value).success.value
              .set(TraderAddressPage, TraderAddress(
                buildingAndStreet = trader.streetAndNumber.value,
                city = trader.city.value,
                postcode = trader.postCode.value)
              ).success.value
              .set(TraderEoriPage, trader.eori).success.value
              .set(IncidentOnRoutePage, IncidentOnRoute.No).success.value

          service.convertToArrivalNotification(userAnswers).value mustEqual arrivalNotification
      }
    }
  }

}
