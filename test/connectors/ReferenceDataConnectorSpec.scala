/*
 * Copyright 2019 HM Revenue & Customs
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
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, get, urlEqualTo}
import generators.DomainModelGenerators
import helper.WireMockServerHandler
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.ExecutionContext.Implicits.global

class ReferenceDataConnectorSpec extends SpecBase with WireMockServerHandler with DomainModelGenerators with ScalaCheckPropertyChecks {

  private val startUrl = "transit-movements-trader-reference-data"

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      conf = "microservice.services.referenceData.port" -> server.port()
    )
    .build()

  lazy val connector: ReferenceDataConnector = app.injector.instanceOf[ReferenceDataConnector]

  "Reference Data" - {
    "return a successful response with Json body" in {
      server.stubFor(
        get(urlEqualTo(s"/$startUrl/customs-offices"))
          .willReturn(
            aResponse()
              .withStatus(200)
          )
      )

      connector.get.futureValue.status mustBe 200
    }
  }

}
