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
import models.{Index, UserAnswers}
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

  // TODO: There is technical debt on how we construct these UserAnswers and it's dependent method
  def userAnswerBuilder(trader: TraderDomain,
                        normalNotification: NormalNotification,
                        isIncidentOnRoute: Boolean,
                        routeEvent1: EnRouteEventDomain): UserAnswers = {

    val eventIndex: Index = Index(0)

    // format: off
    ArrivalNotificationConversionServiceSpec.createNormalNotification(emptyUserAnswers)(trader, normalNotification, isIncidentOnRoute)
      .set(IsTranshipmentPage(eventIndex), false).success.value
      .set(EventPlacePage(eventIndex), routeEvent1.place).success.value
      .set(EventCountryPage(eventIndex), routeEvent1.country).success.value
      .set(EventReportedPage(eventIndex), routeEvent1.alreadyInNcts).success.value

    // format: on
  }

  "UserAnswersToArrivalMovementRequestService" - {

    "must convert UserAnswers to ArrivalMovementRequest for a valid set of user answers and given an InterchangeControlReference" in {
      val app = applicationWithMockIcr

      running(app) {
        val service = app.injector.instanceOf[UserAnswersToArrivalMovementRequestService]

        forAll(arbitrary[TraderDomain], arbitrary[NormalNotification], arbitrary[EnRouteEventDomain]) {
          (trader, normalNotification, enRouteEvent) =>
            val userAnswers = userAnswerBuilder(trader, normalNotification, true, enRouteEvent)

            when(mockIcrRepo.nextInterchangeControlReferenceId()).thenReturn(Future.successful(InterchangeControlReference("", 1)))

            service.convert(userAnswers).value.futureValue mustBe a[ArrivalMovementRequest] // TODO: Make this a round trip test
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
