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

package services.conversion

import generators.MessagesModelGenerators
import models.EoriNumber
import models.messages.ArrivalMovementRequest
import models.reference.CustomsOffice
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import play.api.inject.Injector

class ArrivalNotificationDomainToArrivalMovementRequestServiceSpec
    extends AnyFreeSpec
    with Matchers
    with GuiceOneAppPerSuite
    with MessagesModelGenerators
    with ScalaCheckDrivenPropertyChecks
    with OptionValues {

  def injector: Injector = app.injector

  "SubmissionModelService" - {

    "must convert NormalNotification to ArrivalMovementRequest" in {

      forAll(arbitrary[ArrivalMovementRequest], arbitrary[CustomsOffice], arbitrary[EoriNumber]) {
        (arrivalMovementRequest, customsOffice, eoriNumber) =>
          val customsOfficeWithMatchingId: CustomsOffice = customsOffice
            .copy(id = arrivalMovementRequest.customsOfficeOfPresentation.office)

          val arrivalNotificationDomain = ArrivalMovementRequestToArrivalNotificationService
            .convertToArrivalNotification(
              arrivalMovementRequest,
              customsOfficeWithMatchingId,
              eoriNumber
            )
            .value

          val result = ArrivalNotificationDomainToArrivalMovementRequestService
            .convertToSubmissionModel(
              arrivalNotificationDomain,
              arrivalMovementRequest.meta.interchangeControlReference,
              arrivalMovementRequest.meta.timeOfPreparation
            )

          result mustBe arrivalMovementRequest
      }
    }
  }

}
