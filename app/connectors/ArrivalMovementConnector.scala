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

package connectors

import com.lucidchart.open.xtract.{ParseFailure, ParseSuccess, PartialParseSuccess, XmlReader}
import config.FrontendAppConfig

import javax.inject.Inject
import models.XMLWrites._
import models.messages.{ArrivalMovementRequest, ArrivalNotificationRejectionMessage}
import models.{ArrivalId, MessagesSummary, ResponseMovementMessage}
import play.api.http.HeaderNames
import uk.gov.hmrc.http.{HeaderCarrier, HttpErrorFunctions, HttpResponse}

import scala.concurrent.{ExecutionContext, Future}
import scala.xml.NodeSeq
import logging.Logging
import uk.gov.hmrc.http.HttpClient

class ArrivalMovementConnector @Inject() (val config: FrontendAppConfig, val http: HttpClient)(implicit ec: ExecutionContext)
    extends HttpErrorFunctions
    with Logging {

  private val channel: String = "web"

  def submitArrivalMovement(arrivalMovement: ArrivalMovementRequest)(implicit hc: HeaderCarrier): Future[HttpResponse] = {

    val serviceUrl = s"${config.destinationUrl}/movements/arrivals"
    val headers    = Seq(ContentTypeHeader("application/xml"), ChannelHeader(channel))

    http.POSTString[HttpResponse](serviceUrl, arrivalMovement.toXml.toString, headers)
  }

  def getSummary(arrivalId: ArrivalId)(implicit hc: HeaderCarrier): Future[Option[MessagesSummary]] = {

    val serviceUrl: String = s"${config.destinationUrl}/movements/arrivals/${arrivalId.value}/messages/summary"
    val header             = hc.withExtraHeaders(ChannelHeader(channel))
    http.GET[HttpResponse](serviceUrl)(httpReads, header, ec) map {
      case responseMessage if is2xx(responseMessage.status) => Some(responseMessage.json.as[MessagesSummary])
      case response =>
        logger.error(s"[getSummary] unexpected response code (${response.status}) returned when getting summary for arrival id: ${arrivalId.value} ")
        None
    }
  }

  def getRejectionMessage(rejectionLocation: String)(implicit hc: HeaderCarrier): Future[Option[ArrivalNotificationRejectionMessage]] = {
    val serviceUrl = s"${config.baseDestinationUrl}$rejectionLocation"
    val header     = hc.withExtraHeaders(ChannelHeader(channel))
    http.GET[HttpResponse](serviceUrl)(httpReads, header, ec) map {
      case responseMessage if is2xx(responseMessage.status) =>
        val message: NodeSeq = responseMessage.json.as[ResponseMovementMessage].message
        XmlReader.of[ArrivalNotificationRejectionMessage].read(message) match {
          case ParseFailure(_) =>
            logger.error("[getRejectionMessage] could not parse body into an ArrivalNotificationRejectionMessage")
            Option.empty[ArrivalNotificationRejectionMessage]
          case PartialParseSuccess(get, _) =>
            logger.info("[getRejectionMessage] successfully parsed ArrivalNotificationRejectionMessage with some errors")
            Some(get)
          case ParseSuccess(get) => Some(get)
        }
      case response =>
        logger.error(s"[getRejectionMessage] received an unexpected status (${response.status}) when attempting to retrieve the rejection message")
        None
    }
  }

  def getArrivalNotificationMessage(location: String)(implicit hc: HeaderCarrier): Future[Option[ArrivalMovementRequest]] = {
    val serviceUrl = s"${config.baseDestinationUrl}$location"
    val header     = hc.withExtraHeaders(ChannelHeader(channel))
    http.GET[HttpResponse](serviceUrl)(httpReads, header, ec) map {
      case responseMessage if is2xx(responseMessage.status) =>
        val xml = responseMessage.json.as[ResponseMovementMessage].message
        XmlReader.of[ArrivalMovementRequest].read(xml).toOption
      case _ =>
        None
    }
  }

  def updateArrivalMovement(arrivalId: ArrivalId, arrivalMovementRequest: ArrivalMovementRequest)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    val serviceUrl = s"${config.destinationUrl}/movements/arrivals/${arrivalId.value}"
    val headers    = Seq(ChannelHeader(channel), ("Content-Type", "application/xml"))

    http.PUTString[HttpResponse](serviceUrl, arrivalMovementRequest.toXml.toString(), headers)
  }

  object ChannelHeader {
    def apply(value: String): (String, String) = ("Channel", value)
  }

  object ContentTypeHeader {
    def apply(value: String): (String, String) = (HeaderNames.CONTENT_TYPE, value)
  }

  object AuthorizationHeader {
    def apply(value: String): (String, String) = (HeaderNames.AUTHORIZATION, value)
  }
}
