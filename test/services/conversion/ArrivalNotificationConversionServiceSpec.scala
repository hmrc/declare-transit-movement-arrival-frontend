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

import java.time.LocalDate

import base.SpecBase
import generators.DomainModelGenerators
import models.GoodsLocation.BorderForceOffice
import models.domain.messages.NormalNotification
import models.{IncidentOnRoute, TraderAddress, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._

class ArrivalNotificationConversionServiceSpec extends SpecBase with ScalaCheckPropertyChecks with DomainModelGenerators {

  val service = injector.instanceOf[ArrivalNotificationConversionService]

  "ArrivalNotificationConversionService" - {

    "must return 'Normal Arrival Notification' message from valid userAnswers" in {
      forAll(arbitrary[NormalNotification], generatorTraderWithEoriAllValues, Gen.alphaNumStr) {
        case (arbArrivalNotification, trader, subPlace) =>

          val arrivalNotification: NormalNotification = arbArrivalNotification.copy(movementReferenceNumber = mrn.value)
            .copy(trader = trader)
            .copy(customsSubPlace = Some(subPlace))
            .copy(notificationDate = LocalDate.now())
            .copy(notificationPlace = "") //TODO

          val userAnswers: UserAnswers =
            emptyUserAnswers
              .set(MovementReferenceNumberPage, arrivalNotification.movementReferenceNumber).success.value
              .set(GoodsLocationPage, BorderForceOffice).success.value
              .set(PresentationOfficePage, arrivalNotification.presentationOffice).success.value
              .set(CustomsSubPlacePage, subPlace).success.value
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

    "must return 'None' from empty userAnswers" in {
      service.convertToArrivalNotification(emptyUserAnswers) mustEqual(None)
    }

    "must return 'None' from a partly filled userAnswers" in {
      forAll(arbitrary[NormalNotification], generatorTraderWithEoriAllValues) {
        case (arrivalNotification, trader) =>

        val userAnswers: UserAnswers =
          emptyUserAnswers
            .set(MovementReferenceNumberPage, arrivalNotification.movementReferenceNumber).success.value
            .set(TraderEoriPage, trader.eori).success.value
            .set(IncidentOnRoutePage, IncidentOnRoute.No).success.value

        service.convertToArrivalNotification(userAnswers) mustEqual(None)
      }
    }
  }

}
