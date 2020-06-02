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

import connectors.ArrivalMovementConnector
import javax.inject.Inject
import models.{ArrivalId, MovementReferenceNumber}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}
import scala.xml.NodeSeq

class ArrivalNotificationMessageService @Inject()(arrivalMovementConnector: ArrivalMovementConnector) {

  def getArrivalNotificationMessage(arrivalId: ArrivalId)(implicit hc: HeaderCarrier,
                                                          ec: ExecutionContext): Future[Option[(NodeSeq, MovementReferenceNumber)]] =
    arrivalMovementConnector.getSummary(arrivalId) flatMap {
      case Some(summary) =>
        arrivalMovementConnector.getArrivalNotificationMessage(summary.messagesLocation.arrivalNotification)
      case _ => Future.successful(None)
    }
}