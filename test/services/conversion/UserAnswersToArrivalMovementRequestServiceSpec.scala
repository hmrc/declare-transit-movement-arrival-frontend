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

package services.conversion

import base.SpecBase
import generators.MessagesModelGenerators
import models.domain.{EnRouteEventDomain, NormalNotification, TraderDomain}
import models.messages.{ArrivalMovementRequest, InterchangeControlReference}
import models.{EoriNumber, Index, MovementReferenceNumber, NormalProcedureFlag, UserAnswers}
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import pages.events.{EventCountryPage, EventPlacePage, EventReportedPage, IsTranshipmentPage}
import play.api.inject.bind
import play.api.test.Helpers.running
import repositories.InterchangeControlReferenceIdRepository

import scala.concurrent.Future

class UserAnswersToArrivalMovementRequestServiceSpec extends SpecBase with MessagesModelGenerators with ScalaCheckDrivenPropertyChecks {

  val mockIcrRepo = mock[InterchangeControlReferenceIdRepository]

  override def beforeEach = {
    super.beforeEach()
    Mockito.reset(mockIcrRepo)
  }

  private def applicationWithMockIcr =
    applicationBuilder(None).overrides(bind[InterchangeControlReferenceIdRepository].toInstance(mockIcrRepo)).build()

  "UserAnswersToArrivalMovementRequestService" - {

    "must convert UserAnswers to ArrivalMovementRequest for a valid set of user answers and given an InterchangeControlReference" in {
      val app = applicationWithMockIcr

      running(app) {
        val service = app.injector.instanceOf[UserAnswersToArrivalMovementRequestService]

        forAll(arbitrary[ArrivalMovementRequest]) {
          arrivalMovementRequest =>
            when(mockIcrRepo.nextInterchangeControlReferenceId()).thenReturn(Future.successful(arrivalMovementRequest.meta.interchangeControlReference))

            val userAnswers: UserAnswers = ArrivalMovementRequestToUserAnswersService
              .convertToUserAnswers(
                arrivalMovementRequest,
                EoriNumber(arrivalMovementRequest.trader.eori),
                MovementReferenceNumber(arrivalMovementRequest.header.movementReferenceNumber).value
              )
              .value

            val result: ArrivalMovementRequest = service.convert(userAnswers).value.futureValue

            val dateOfPreperation = result.meta.dateOfPreparation
            val timeOfPreperation = result.meta.timeOfPreparation

            val expectedResult: ArrivalMovementRequest =
              arrivalMovementRequest.copy(
                meta   = arrivalMovementRequest.meta.copy(dateOfPreparation  = dateOfPreperation, timeOfPreparation = timeOfPreperation),
                header = arrivalMovementRequest.header.copy(notificationDate = dateOfPreperation)
              )

            result mustBe expectedResult
        }
      }

    }

    "must return None when UserAnswers is incomplete" in {
      val app = applicationWithMockIcr

      running(app) {
        when(mockIcrRepo.nextInterchangeControlReferenceId()).thenReturn(Future.successful(InterchangeControlReference("", 1)))

        val service = app.injector.instanceOf[UserAnswersToArrivalMovementRequestService]

        service.convert(emptyUserAnswers) must not be (defined)

      }
    }

  }
}
