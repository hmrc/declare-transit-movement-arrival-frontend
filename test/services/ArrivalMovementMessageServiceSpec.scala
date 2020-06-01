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
import models.{ArrivalId, MessagesLocation, MessagesSummary}
import org.mockito.Matchers.any
import org.mockito.Mockito._
import play.api.inject.bind
import org.mockito.Mockito.reset

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.xml.NodeSeq

class ArrivalMovementMessageServiceSpec extends SpecBase {

  val mockConnector: ArrivalMovementConnector = mock[ArrivalMovementConnector]

  override def beforeEach: Unit = {
    super.beforeEach
    reset(mockConnector)
  }

  private val arrivalId = ArrivalId(1)

  "ArrivalMovementMessageService" - {
    "must return NodeSeq for the input arrivalId" in {
      val messagesSummary =
        MessagesSummary(arrivalId, MessagesLocation(s"/movements/arrivals/${arrivalId.value}/messages/3", Some("/movements/arrivals/1234/messages/5")))

      when(mockConnector.getSummary(any())(any())).thenReturn(Future.successful(Some(messagesSummary)))
      when(mockConnector.getArrivalNotificationMessage(any())(any()))
        .thenReturn(Future.successful(Some(NodeSeq.Empty)))

      val application = applicationBuilder(Some(emptyUserAnswers))
        .overrides(bind[ArrivalMovementConnector].toInstance(mockConnector))
        .build()
      val arrivalMovementMessageService = application.injector.instanceOf[ArrivalMovementMessageService]

      arrivalMovementMessageService.getArrivalNotificationMessage(arrivalId).futureValue mustBe Some(NodeSeq.Empty)

    }

    "must return None when getSummary call fails to get MessagesSummary" in {
      val messagesSummary =
        MessagesSummary(arrivalId, MessagesLocation(s"/movements/arrivals/${arrivalId.value}/messages/3", None))
      when(mockConnector.getSummary(any())(any())).thenReturn(Future.successful(None))

      val application = applicationBuilder(Some(emptyUserAnswers))
        .overrides(bind[ArrivalMovementConnector].toInstance(mockConnector))
        .build()
      val arrivalRejectionService = application.injector.instanceOf[ArrivalRejectionService]

      arrivalRejectionService.arrivalRejectionMessage(arrivalId).futureValue mustBe None
    }
  }

}
