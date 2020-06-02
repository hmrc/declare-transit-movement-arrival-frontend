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

import com.lucidchart.open.xtract.XmlReader
import config.FrontendAppConfig
import javax.inject.Inject
import models.XMLWrites._
import models.messages.{ArrivalMovementRequest, ArrivalNotificationRejectionMessage}
import models.{ArrivalId, MessagesSummary, MovementReferenceNumber, ResponseMovementMessage}
import uk.gov.hmrc.http.{HeaderCarrier, HttpErrorFunctions, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}
import scala.xml.NodeSeq

class ArrivalMovementConnector @Inject()(val config: FrontendAppConfig, val http: HttpClient)(implicit ec: ExecutionContext) extends HttpErrorFunctions {

  def submitArrivalMovement(arrivalMovement: ArrivalMovementRequest)(implicit hc: HeaderCarrier): Future[HttpResponse] = {

    val serviceUrl = s"${config.destinationUrl}/movements/arrivals"
    val headers    = Seq(("Content-Type", "application/xml"))

    http.POSTString[HttpResponse](serviceUrl, arrivalMovement.toXml.toString, headers)
  }

  def getSummary(arrivalId: ArrivalId)(implicit hc: HeaderCarrier): Future[Option[MessagesSummary]] = {

    val serviceUrl: String = s"${config.destinationUrl}/movements/arrivals/${arrivalId.value}/messages/summary"
    http.GET[HttpResponse](serviceUrl) map {
      case responseMessage if is2xx(responseMessage.status) => Some(responseMessage.json.as[MessagesSummary])
      case _                                                => None
    }
  }

  def getRejectionMessage(rejectionLocation: String)(implicit hc: HeaderCarrier): Future[Option[ArrivalNotificationRejectionMessage]] = {
    val serviceUrl = s"${config.baseDestinationUrl}$rejectionLocation"
    http.GET[HttpResponse](serviceUrl) map {
      case responseMessage if is2xx(responseMessage.status) =>
        val message: NodeSeq = responseMessage.json.as[ResponseMovementMessage].message
        XmlReader.of[ArrivalNotificationRejectionMessage].read(message).toOption
      case _ => None
    }
  }

  //TODO MovementReferenceNumber will be removed when we implement xml reads for ArrivalMovementRequest
  def getArrivalNotificationMessage(location: String)(implicit hc: HeaderCarrier): Future[Option[(NodeSeq, MovementReferenceNumber)]] = {
    val serviceUrl = s"${config.baseDestinationUrl}$location"
    http.GET[HttpResponse](serviceUrl) map {
      case responseMessage if is2xx(responseMessage.status) =>
        val xml = responseMessage.json.as[ResponseMovementMessage].message

        MovementReferenceNumber(xml.\\("DocNumHEA5").text) map {
          mrn =>
            (responseMessage.json.as[ResponseMovementMessage].message, mrn)
        }
      case _ =>
        None
    }
  }

  def updateArrivalMovement(arrivalId: ArrivalId, arrivalMovement: ArrivalMovementRequest)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    val serviceUrl = s"${config.destinationUrl}/movements/arrivals/${arrivalId.value}"
    val headers    = Seq(("Content-Type", "application/xml"))

    http.PUTString[HttpResponse](serviceUrl, arrivalMovement.toXml.toString, headers)
  }
}
