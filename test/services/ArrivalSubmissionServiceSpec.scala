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

package services

import java.time.LocalDate

import base.SpecBase
import connectors.ArrivalMovementConnector
import generators.MessagesModelGenerators
import models.domain.{NormalNotification, TraderDomain}
import models.messages.InterchangeControlReference
import models.reference.CustomsOffice
import models.{ArrivalId, EoriNumber}
import org.mockito.Matchers.any
import org.mockito.Mockito._
import org.scalacheck.Arbitrary.arbitrary
import play.api.http.Status._
import play.api.inject.bind
import repositories.InterchangeControlReferenceIdRepository
import services.conversion.UserAnswersToArrivalNotificationDomain
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.Future

class ArrivalSubmissionServiceSpec extends SpecBase with MessagesModelGenerators {

  private val mockConverterService                  = mock[UserAnswersToArrivalNotificationDomain]
  private val mockArrivalMovementConnector          = mock[ArrivalMovementConnector]
  private val mockInterchangeControllerReference    = mock[InterchangeControlReferenceIdRepository]
  private val mockArrivalNotificationMessageService = mock[ArrivalNotificationMessageService]

  private val traderWithoutEori  = TraderDomain("", "", "", "", "", "")
  private val normalNotification = NormalNotification(mrn, "", LocalDate.now(), "", traderWithoutEori, CustomsOffice("", "", Seq.empty, None), None)

  override def beforeEach: Unit = {
    super.beforeEach()
    reset(mockArrivalMovementConnector)
    reset(mockInterchangeControllerReference)
    reset(mockArrivalNotificationMessageService)
    reset(mockConverterService)
  }

  "ArrivalSubmissionService" - {

    "submit" - {

      "must return None on submission of invalid data" in {
        when(mockConverterService.convertToArrivalNotification(any()))
          .thenReturn(None)

        val application = applicationBuilder(Some(emptyUserAnswers))
          .overrides(bind[UserAnswersToArrivalNotificationDomain].toInstance(mockConverterService))
          .build()

        val arrivalNotificationService = application.injector.instanceOf[ArrivalSubmissionService]

        arrivalNotificationService.submit(emptyUserAnswers).futureValue mustBe None
      }

      "must create arrival notification for valid xml input" in {

        when(mockInterchangeControllerReference.nextInterchangeControlReferenceId())
          .thenReturn(Future.successful(InterchangeControlReference("date", 0)))

        when(mockConverterService.convertToArrivalNotification(any()))
          .thenReturn(Some(normalNotification))

        when(mockArrivalMovementConnector.submitArrivalMovement(any())(any()))
          .thenReturn(Future.successful(HttpResponse(ACCEPTED)))

        val application = applicationBuilder(Some(emptyUserAnswers))
          .overrides(bind[InterchangeControlReferenceIdRepository].toInstance(mockInterchangeControllerReference))
          .overrides(bind[UserAnswersToArrivalNotificationDomain].toInstance(mockConverterService))
          .overrides(bind[ArrivalMovementConnector].toInstance(mockArrivalMovementConnector))
          .build()

        val arrivalNotificationService = application.injector.instanceOf[ArrivalSubmissionService]

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
          .thenReturn(Future.successful(HttpResponse(ACCEPTED)))

        val application = applicationBuilder(Some(userAnswersWithArrivalId))
          .overrides(bind[InterchangeControlReferenceIdRepository].toInstance(mockInterchangeControllerReference))
          .overrides(bind[UserAnswersToArrivalNotificationDomain].toInstance(mockConverterService))
          .overrides(bind[ArrivalMovementConnector].toInstance(mockArrivalMovementConnector))
          .build()

        val arrivalNotificationService = application.injector.instanceOf[ArrivalSubmissionService]

        val response = arrivalNotificationService.submit(userAnswersWithArrivalId).futureValue.get
        response.status mustBe ACCEPTED
        verify(mockArrivalMovementConnector, times(1)).updateArrivalMovement(any(), any())(any())
      }
    }
  }
}
