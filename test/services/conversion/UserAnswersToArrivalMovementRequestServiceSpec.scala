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

import base.{AppWithDefaultMockFixtures, SpecBase}
import generators.MessagesModelGenerators
import models.messages.{ArrivalMovementRequest, InterchangeControlReference}
import models.reference.CustomsOffice
import models.{EoriNumber, MovementReferenceNumber, UserAnswers}
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import repositories.InterchangeControlReferenceIdRepository

import scala.concurrent.Future

class UserAnswersToArrivalMovementRequestServiceSpec
    extends SpecBase
    with AppWithDefaultMockFixtures
    with MessagesModelGenerators
    with ScalaCheckDrivenPropertyChecks {

  val mockIcrRepo: InterchangeControlReferenceIdRepository = mock[InterchangeControlReferenceIdRepository]

  override def beforeEach(): Unit = {
    super.beforeEach()
    Mockito.reset(mockIcrRepo)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[InterchangeControlReferenceIdRepository].toInstance(mockIcrRepo))

  "UserAnswersToArrivalMovementRequestService" - {

    "must convert UserAnswers to ArrivalMovementRequest for a valid set of user answers and given an InterchangeControlReference" in {
      val service = app.injector.instanceOf[UserAnswersToArrivalMovementRequestService]

      forAll(arbitrary[ArrivalMovementRequest], arbitrary[CustomsOffice]) {
        (arrivalMovementRequest, customsOffice) =>
          val setCustomsOffice = customsOffice.copy(id = arrivalMovementRequest.customsOfficeOfPresentation.office)

          when(mockIcrRepo.nextInterchangeControlReferenceId()).thenReturn(Future.successful(arrivalMovementRequest.meta.interchangeControlReference))

          val userAnswers: UserAnswers = ArrivalMovementRequestToUserAnswersService
            .convertToUserAnswers(
              arrivalMovementRequest,
              EoriNumber(arrivalMovementRequest.trader.eori),
              MovementReferenceNumber(arrivalMovementRequest.header.movementReferenceNumber).value,
              setCustomsOffice
            )
            .value

          val result: ArrivalMovementRequest = service.convert(userAnswers).value.futureValue

          val dateOfPreparation = result.meta.dateOfPreparation
          val timeOfPreparation = result.meta.timeOfPreparation

          val expectedResult: ArrivalMovementRequest =
            arrivalMovementRequest.copy(
              meta = arrivalMovementRequest.meta.copy(dateOfPreparation = dateOfPreparation, timeOfPreparation = timeOfPreparation),
              header = arrivalMovementRequest.header.copy(notificationDate = dateOfPreparation)
            )

          result mustBe expectedResult
      }
    }

    "must return None when UserAnswers is incomplete" in {
      when(mockIcrRepo.nextInterchangeControlReferenceId()).thenReturn(Future.successful(InterchangeControlReference("", 1)))

      val service = app.injector.instanceOf[UserAnswersToArrivalMovementRequestService]

      service.convert(emptyUserAnswers) must not be defined
    }
  }
}
