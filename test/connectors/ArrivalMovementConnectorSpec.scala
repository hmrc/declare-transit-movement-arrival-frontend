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

import base.SpecBase
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import generators.MessagesModelGenerators
import helper.WireMockServerHandler
import models.{ArrivalId, MessageAction, MessageActions}
import models.messages.ArrivalMovementRequest
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Application
import play.api.http.Status._
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.http.HttpResponse
import org.scalacheck.Arbitrary.arbitrary
import play.api.libs.json.Json

import scala.concurrent.Future

class ArrivalMovementConnectorSpec extends SpecBase with WireMockServerHandler with MessagesModelGenerators with ScalaCheckPropertyChecks {

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      conf = "microservice.services.destination.port" -> server.port()
    )
    .build()

  lazy val connector: ArrivalMovementConnector = app.injector.instanceOf[ArrivalMovementConnector]

  private val errorResponsesCodes: Gen[Int] = Gen.chooseNum(400, 599)
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

      val json = Json.obj("arrivalId" -> 1,
                          "messages" -> Json.obj(
                            "IE007" -> "/movements/arrivals/1/messages/3",
                            "IE008" -> "/movements/arrivals/1/messages/5"
                          ))

      val messageAction = MessageActions(ArrivalId(1), MessageAction("/movements/arrivals/1/messages/3", Some("/movements/arrivals/1/messages/5")))

      "must be successful and return MessageActions" in {
        server.stubFor(
          get(urlEqualTo("/transit-movements-trader-at-destination/movements/arrivals/1/messages/summary"))
            .willReturn(
              okJson(json.toString)
            )
        )
        connector.getSummary(ArrivalId(1)).futureValue mustBe messageAction
      }

      "must return an error status when an error response is returned from getSummary" in {
        forAll(errorResponsesCodes) {
          errorResponseCode =>
            server.stubFor(
              get(urlEqualTo(s"/transit-movements-trader-at-destination/movements/arrivals/1/messages/summary"))
                .willReturn(
                  aResponse()
                    .withStatus(errorResponseCode)
                ))

            val result = connector.getSummary(ArrivalId(1))
            whenReady(result.failed) {
              _ mustBe an[Exception]
            }
        }
      }
    }
  }

  private def stubResponse(expectedStatus: Int): StubMapping =
    server.stubFor(
      post(urlEqualTo("/transit-movements-trader-at-destination/movements/arrivals"))
        .willReturn(
          aResponse()
            .withStatus(expectedStatus)
        )
    )
}
