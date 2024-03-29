/*
 * Copyright 2023 HM Revenue & Customs
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
import models.ArrivalId
import models.messages.ArrivalNotificationRejectionMessage
import uk.gov.hmrc.http.HeaderCarrier
import logging.Logging

import scala.concurrent.{ExecutionContext, Future}

class ArrivalRejectionService @Inject() (arrivalMovementConnector: ArrivalMovementConnector) extends Logging {

  def arrivalRejectionMessage(arrivalId: ArrivalId)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[ArrivalNotificationRejectionMessage]] =
    arrivalMovementConnector.getSummary(arrivalId) flatMap {
      case Some(summary) =>
        summary.messagesLocation.arrivalRejection match {
          case Some(rejectionLocation) => arrivalMovementConnector.getRejectionMessage(rejectionLocation)
          case _ =>
            logger.error(s"[arrivalRejectionMessage] no arrivalRejection found for arrivalId ${arrivalId.value}")
            Future.successful(None)
        }
      case _ => Future.successful(None)
    }

}
