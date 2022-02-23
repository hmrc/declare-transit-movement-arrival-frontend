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

package services

import base.{AppWithDefaultMockFixtures, SpecBase}
import connectors.ArrivalMovementConnector
import generators.MessagesModelGenerators
import models.ArrivalId
import models.domain.{NormalNotification, TraderDomain}
import models.messages.InterchangeControlReference
import models.reference.CustomsOffice
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.http.Status._
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import repositories.InterchangeControlReferenceIdRepository
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import java.time.LocalDate
import conversion.UserAnswersToArrivalNotificationDomain
import scala.concurrent.Future

class ArrivalSubmissionServiceSpec extends SpecBase with AppWithDefaultMockFixtures with MessagesModelGenerators {

  private val mockConverterService                  = mock[UserAnswersToArrivalNotificationDomain]
  private val mockArrivalMovementConnector          = mock[ArrivalMovementConnector]
  private val mockInterchangeControllerReference    = mock[InterchangeControlReferenceIdRepository]
  private val mockArrivalNotificationMessageService = mock[ArrivalNotificationMessageService]

  private val traderWithoutEori  = TraderDomain("", "", "", "", "", "")
  private val normalNotification = NormalNotification(mrn, "", LocalDate.now(), "", traderWithoutEori, CustomsOffice("", Some(""), None), None)

  implicit private val hc: HeaderCarrier = HeaderCarrier()

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockArrivalMovementConnector)
    reset(mockInterchangeControllerReference)
    reset(mockArrivalNotificationMessageService)
    reset(mockConverterService)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[UserAnswersToArrivalNotificationDomain].toInstance(mockConverterService),
        bind[ArrivalMovementConnector].toInstance(mockArrivalMovementConnector),
        bind[InterchangeControlReferenceIdRepository].toInstance(mockInterchangeControllerReference),
        bind[ArrivalNotificationMessageService].toInstance(mockArrivalNotificationMessageService)
      )

  "ArrivalSubmissionService" - {

    "submit" - {

      "must return None on submission of invalid data" in {
        when(mockConverterService.convertToArrivalNotification(any()))
          .thenReturn(None)

        setExistingUserAnswers(emptyUserAnswers)

        val arrivalNotificationService = app.injector.instanceOf[ArrivalSubmissionService]

        arrivalNotificationService.submit(emptyUserAnswers).futureValue mustBe None
      }

      "must create arrival notification for valid xml input" in {

        when(mockInterchangeControllerReference.nextInterchangeControlReferenceId())
          .thenReturn(Future.successful(InterchangeControlReference("date", 0)))

        when(mockConverterService.convertToArrivalNotification(any()))
          .thenReturn(Some(normalNotification))

        when(mockArrivalMovementConnector.submitArrivalMovement(any())(any()))
          .thenReturn(Future.successful(HttpResponse(ACCEPTED, "")))

        setExistingUserAnswers(emptyUserAnswers)

        val arrivalNotificationService = app.injector.instanceOf[ArrivalSubmissionService]

        val response = arrivalNotificationService.submit(emptyUserAnswers).futureValue.get
        response.status mustBe ACCEPTED
        verify(mockArrivalMovementConnector, times(1)).submitArrivalMovement(any())(any())
      }

      "must update arrival notification for valid xml input" in {

        val userAnswersWithArrivalId = emptyUserAnswers.copy(arrivalId = Some(ArrivalId(1)))

        when(mockInterchangeControllerReference.nextInterchangeControlReferenceId())
          .thenReturn(Future.successful(InterchangeControlReference("date", 0)))

        when(mockConverterService.convertToArrivalNotification(any()))
          .thenReturn(Some(normalNotification))

        when(mockArrivalMovementConnector.updateArrivalMovement(any(), any())(any()))
          .thenReturn(Future.successful(HttpResponse(ACCEPTED, "")))

        setExistingUserAnswers(userAnswersWithArrivalId)

        val arrivalNotificationService = app.injector.instanceOf[ArrivalSubmissionService]

        val response = arrivalNotificationService.submit(userAnswersWithArrivalId).futureValue.get
        response.status mustBe ACCEPTED
        verify(mockArrivalMovementConnector, times(1)).updateArrivalMovement(any(), any())(any())
      }
    }
  }
}
