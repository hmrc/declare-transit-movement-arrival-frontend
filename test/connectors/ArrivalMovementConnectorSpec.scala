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

import java.time.LocalDate

import base.SpecBase
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import generators.MessagesModelGenerators
import helper.WireMockServerHandler
import models.messages.ErrorType.DuplicateMrn
import models.messages._
import models.{ArrivalId, MessagesLocation, MessagesSummary}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Application
import play.api.http.Status._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.Future
import scala.xml.NodeSeq

class ArrivalMovementConnectorSpec extends SpecBase with WireMockServerHandler with MessagesModelGenerators with ScalaCheckPropertyChecks {

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      conf = "microservice.services.destination.port" -> server.port()
    )
    .build()

  lazy val connector: ArrivalMovementConnector = app.injector.instanceOf[ArrivalMovementConnector]

  private val errorResponsesCodes: Gen[Int] = Gen.chooseNum(400, 599)
  private val arrivalId                     = ArrivalId(1)

  "ArrivalMovementConnector" - {

    "submitArrivalMovement" - {

      "must return status as OK for submission of valid arrival movement" in {

        stubResponse(ACCEPTED)

        forAll(arbitrary[ArrivalMovementRequest]) {
          arrivalMovementRequest =>
            val result: Future[HttpResponse] = connector.submitArrivalMovement(arrivalMovementRequest)
            result.futureValue.status mustBe ACCEPTED
        }
      }

      "must return an error status when an error response is returned from submitArrivalMovement" in {
        forAll(arbitrary[ArrivalMovementRequest], errorResponsesCodes) {
          (arrivalMovementRequest, errorResponseCode) =>
            stubResponse(errorResponseCode)

            val result = connector.submitArrivalMovement(arrivalMovementRequest)
            result.futureValue.status mustBe errorResponseCode
        }
      }
    }

    "getSummary" - {

      "must be return summary of messages" in {
        val json = Json.obj(
          "arrivalId" -> arrivalId.value,
          "messages" -> Json.obj(
            "IE007" -> s"/movements/arrivals/${arrivalId.value}/messages/3",
            "IE008" -> s"/movements/arrivals/${arrivalId.value}/messages/5"
          )
        )

        val messageAction =
          MessagesSummary(arrivalId,
                          MessagesLocation(s"/movements/arrivals/${arrivalId.value}/messages/3", Some(s"/movements/arrivals/${arrivalId.value}/messages/5")))

        server.stubFor(
          get(urlEqualTo(s"/transit-movements-trader-at-destination/movements/arrivals/${arrivalId.value}/messages/summary"))
            .willReturn(
              okJson(json.toString)
            )
        )
        connector.getSummary(arrivalId).futureValue mustBe Some(messageAction)
      }

      "must return 'None' when an error response is returned from getSummary" in {
        forAll(errorResponsesCodes) {
          errorResponseCode: Int =>
            stubGetResponse(errorResponseCode, "/transit-movements-trader-at-destination/movements/arrivals/1/messages/summary")

            connector.getSummary(ArrivalId(1)).futureValue mustBe None
        }
      }
    }

    "getRejectionMessage" - {
      "must return valid 'rejection message'" in {
        val rejectionLocation     = s"/transit-movements-trader-at-destination/movements/arrivals/${arrivalId.value}/messages/1"
        val genRejectionError     = arbitrary[ErrorType].sample.value
        val rejectionXml: NodeSeq = <CC008A>
          <HEAHEA><DocNumHEA5>19IT021300100075E9</DocNumHEA5>
            <ArrRejDatHEA142>20191018</ArrRejDatHEA142>
            <ArrRejReaHEA242>Invalid IE007 Message</ArrRejReaHEA242>
          </HEAHEA>
          <FUNERRER1>
            <ErrTypER11>{genRejectionError.code}</ErrTypER11>
            <ErrPoiER12>Message type</ErrPoiER12>
            <OriAttValER14>GB007A</OriAttValER14>
        </FUNERRER1>
        </CC008A>

        val json = Json.obj("message" -> rejectionXml.toString())

        server.stubFor(
          get(urlEqualTo(rejectionLocation))
            .willReturn(
              okJson(json.toString)
            )
        )
        val expectedResult = Some(
          ArrivalNotificationRejectionMessage(
            "19IT021300100075E9",
            LocalDate.of(2019, 10, 18),
            None,
            Some("Invalid IE007 Message"),
            List(FunctionalError(genRejectionError, ErrorPointer("Message type"), None, Some("GB007A")))
          ))
        connector.getRejectionMessage(rejectionLocation).futureValue mustBe expectedResult
      }

      "must return None for malformed xml'" in {
        val rejectionLocation     = s"/transit-movements-trader-at-destination/movements/arrivals/${arrivalId.value}/messages/1"
        val rejectionXml: NodeSeq = <CC008A>
          <HEAHEA><DocNumHEA5>19IT021300100075E9</DocNumHEA5>
          </HEAHEA>
        </CC008A>

        val json = Json.obj("message" -> rejectionXml.toString())

        server.stubFor(
          get(urlEqualTo(rejectionLocation))
            .willReturn(
              okJson(json.toString)
            )
        )

        connector.getRejectionMessage(rejectionLocation).futureValue mustBe None
      }

      "must return None when an error response is returned from getRejectionMessage" in {
        val rejectionLocation: String = "/transit-movements-trader-at-destination/movements/arrivals/1/messages/1"
        forAll(errorResponsesCodes) {
          errorResponseCode =>
            stubGetResponse(errorResponseCode, rejectionLocation)

            connector.getRejectionMessage(rejectionLocation).futureValue mustBe None
        }
      }
    }
  }

  private def stubGetResponse(errorResponseCode: Int, serviceUrl: String) =
    server.stubFor(
      get(urlEqualTo(serviceUrl))
        .willReturn(
          aResponse()
            .withStatus(errorResponseCode)
        ))

  private def stubResponse(expectedStatus: Int): StubMapping =
    server.stubFor(
      post(urlEqualTo("/transit-movements-trader-at-destination/movements/arrivals"))
        .willReturn(
          aResponse()
            .withStatus(expectedStatus)
        )
    )
}
