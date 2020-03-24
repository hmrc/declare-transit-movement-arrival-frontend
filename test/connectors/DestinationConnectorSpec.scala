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
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, post, urlEqualTo}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import generators.MessagesModelGenerators
import helper.WireMockServerHandler
import models.messages.NormalNotification
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Application
import play.api.http.Status._
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.Future

class DestinationConnectorSpec extends SpecBase with WireMockServerHandler with MessagesModelGenerators with ScalaCheckPropertyChecks {

  private val startUrl = "transit-movements-trader-at-destination"

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      conf = "microservice.services.destination.port" -> server.port()
    )
    .build()

  lazy val connector: DestinationConnector = app.injector.instanceOf[DestinationConnector]
  private val arrivalMovementXml           = <xml>data</xml>

  "DestinationConnector" - {
    //TODO need to remove these tests that method that is deprecated
    "must return status as OK for submission of valid arrival notification" in {

      stubResponse(s"/$startUrl/arrival-notification", OK)

      forAll(arbitrary[NormalNotification]) {
        notification =>
          val result: Future[HttpResponse] = connector.submitArrivalNotification(notification)
          result.futureValue.status mustBe OK
      }
    }

    "must return status as BAD_REQUEST for submission of invalid arrival notification" in {

      stubResponse(s"/$startUrl/arrival-notification", BAD_REQUEST)

      forAll(arbitrary[NormalNotification]) {
        notification =>
          val result = connector.submitArrivalNotification(notification)
          result.futureValue.status mustBe BAD_REQUEST
      }
    }

    "must return status as INTERNAL_SERVER_ERROR for technical error incurred" in {

      stubResponse(s"/$startUrl/arrival-notification", INTERNAL_SERVER_ERROR)

      forAll(arbitrary[NormalNotification]) {
        notification =>
          val result = connector.submitArrivalNotification(notification)
          result.futureValue.status mustBe INTERNAL_SERVER_ERROR
      }
    }

    "must return status as OK for submission of valid arrival movement" in {

      stubResponse(s"/$startUrl/movements/arrivals", OK)

      val result: Future[HttpResponse] = connector.submitArrivalMovement(arrivalMovementXml)
      result.futureValue.status mustBe OK
    }

    "must return an error status when an error response is returned from submitArrivalMovement" in {

      val errorResponsesCodes: Gen[Int] = Gen.chooseNum(400, 599)

      forAll(errorResponsesCodes) {
        errorResponseCode =>
          stubResponse(s"/$startUrl/movements/arrivals", errorResponseCode)

          val result = connector.submitArrivalMovement(arrivalMovementXml)
          result.futureValue.status mustBe errorResponseCode
      }
    }
  }

  private def stubResponse(expectedUrl: String, expectedStatus: Int): StubMapping =
    server.stubFor(
      post(urlEqualTo(expectedUrl))
        .willReturn(
          aResponse()
            .withStatus(expectedStatus)
        )
    )
}
