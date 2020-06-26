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

import base.SpecBase
import connectors.ArrivalMovementConnector
import generators.MessagesModelGenerators
import models.messages.ArrivalMovementRequest
import models.{ArrivalId, MessagesLocation, MessagesSummary}
import org.mockito.Matchers.any
import org.mockito.Mockito.{reset, _}
import org.scalacheck.Arbitrary.arbitrary
import play.api.inject.bind

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ArrivalNotificationMessageServiceSpec extends SpecBase with MessagesModelGenerators {

  val mockConnector: ArrivalMovementConnector = mock[ArrivalMovementConnector]

  override def beforeEach: Unit = {
    super.beforeEach
    reset(mockConnector)
  }

  private val arrivalId = ArrivalId(1)

  "ArrivalNotificationMessageService" - {
    "must return ArrivalMovementRequest model for the input arrivalId" in {
      val messagesSummary =
        MessagesSummary(arrivalId, MessagesLocation(s"/movements/arrivals/${arrivalId.value}/messages/3", Some("/movements/arrivals/1234/messages/5")))
      val arrivalMovementRequest: ArrivalMovementRequest = arbitrary[ArrivalMovementRequest].sample.value

      when(mockConnector.getSummary(any())(any())).thenReturn(Future.successful(Some(messagesSummary)))
      when(mockConnector.getArrivalNotificationMessage(any())(any()))
        .thenReturn(Future.successful(Some(arrivalMovementRequest)))

      val application = applicationBuilder(Some(emptyUserAnswers))
        .overrides(bind[ArrivalMovementConnector].toInstance(mockConnector))
        .build()
      val arrivalMovementMessageService = application.injector.instanceOf[ArrivalNotificationMessageService]

      arrivalMovementMessageService.getArrivalNotificationMessage(arrivalId).futureValue mustBe Some(arrivalMovementRequest)

    }

    "must return None when getSummary call fails to get MessagesSummary" in {
      when(mockConnector.getSummary(any())(any())).thenReturn(Future.successful(None))

      val application = applicationBuilder(Some(emptyUserAnswers))
        .overrides(bind[ArrivalMovementConnector].toInstance(mockConnector))
        .build()
      val arrivalRejectionService = application.injector.instanceOf[ArrivalRejectionService]

      arrivalRejectionService.arrivalRejectionMessage(arrivalId).futureValue mustBe None
    }
  }

}
