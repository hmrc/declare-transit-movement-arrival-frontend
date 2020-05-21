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

package connectors

import config.FrontendAppConfig
import javax.inject.Inject
import models.XMLWrites._
import models.messages.{ArrivalMovementRequest, ArrivalNotificationRejectionMessage}
import models.{ArrivalId, MessageActions, MessageId}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

class ArrivalMovementConnector @Inject()(val config: FrontendAppConfig, val http: HttpClient)(implicit ec: ExecutionContext) {

  def submitArrivalMovement(arrivalMovement: ArrivalMovementRequest)(implicit hc: HeaderCarrier): Future[HttpResponse] = {

    val serviceUrl = s"${config.destinationUrl}/movements/arrivals"
    val headers    = Seq(("Content-Type", "application/xml"))

    http.POSTString[HttpResponse](serviceUrl, arrivalMovement.toXml.toString, headers)
  }

  def getSummary(arrivalId: ArrivalId)(implicit hc: HeaderCarrier): Future[MessageActions] = {

    val serviceUrl = s"${config.destinationUrl}/movements/arrivals/${arrivalId.value}/messages/summary"

    // Future.successful(MessageActions(ArrivalId("1"), Seq(MessageAction("IE008", "/movements/arrivals/1234/messages/5"))))
    http.GET[MessageActions](serviceUrl)
  }

  def getRejectionMessage(arrivalId: ArrivalId, messageId: MessageId): Future[ArrivalNotificationRejectionMessage] = ???
}
