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

import base.{AppWithDefaultMockFixtures, SpecBase}
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, get, okJson, urlEqualTo}
import generators.MessagesModelGenerators
import helper.WireMockServerHandler
import models.reference._
import org.scalacheck.Gen
import org.scalatest.Assertion
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReferenceDataConnectorSpec
    extends SpecBase
    with AppWithDefaultMockFixtures
    with WireMockServerHandler
    with MessagesModelGenerators
    with ScalaCheckPropertyChecks {

  private val startUrl = "transit-movements-trader-reference-data"
  private val country  = CountryCode("GB")

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .configure(conf = "microservice.services.referenceData.port" -> server.port())

  private lazy val connector: ReferenceDataConnector = app.injector.instanceOf[ReferenceDataConnector]

  implicit private val hc: HeaderCarrier = HeaderCarrier()

  private val customsOfficeResponseJson: String =
    """
      |[
      | {
      |   "id" : "GBtestId1",
      |   "name" : "testName1",
      |   "roles" : ["role1", "role2"],
      |   "phoneNumber" : "testPhoneNumber"
      | },
      | {
      |   "id" : "GBtestId2",
      |   "name" : "testName2",
      |   "roles" : ["role1", "role2"]
      | }
      |]
      |""".stripMargin

  private val countryListResponseJson: String =
    """
      |[
      | {
      |   "code":"GB",
      |   "state":"valid",
      |   "description":"United Kingdom"
      | },
      | {
      |   "code":"AD",
      |   "state":"valid",
      |   "description":"Andorra"
      | }
      |]
      |""".stripMargin

  val errorResponses: Gen[Int] = Gen.chooseNum(400, 599)

  "Reference Data" - {

    "getCustomsOffices" - {
      "must return a successful future response with a sequence of CustomsOffices" in {
        server.stubFor(
          get(urlEqualTo(s"/$startUrl/customs-offices"))
            .willReturn(okJson(customsOfficeResponseJson))
        )

        val expectedResult =
          Seq(
            CustomsOffice("GBtestId1", Some("testName1"), Some("testPhoneNumber")),
            CustomsOffice("GBtestId2", Some("testName2"), None)
          )

        connector.getCustomsOffices.futureValue mustBe expectedResult
      }

      "must return an exception when an error response is returned" in {
        checkErrorResponse(s"/$startUrl/customs-offices", connector.getCustomsOffices)
      }
    }

    "getCustomsOfficesOfTheCountry" - {
      "must return a successful future response with a sequence of CustomsOffices" in {
        server.stubFor(
          get(urlEqualTo(s"/$startUrl/customs-offices/GB"))
            .willReturn(okJson(customsOfficeResponseJson))
        )

        val expectedResult = Seq(
          CustomsOffice("GBtestId1", Some("testName1"), Some("testPhoneNumber")),
          CustomsOffice("GBtestId2", Some("testName2"), None)
        )

        connector.getCustomsOfficesForCountry(country).futureValue mustBe expectedResult
      }

      "must return an exception when an error response is returned" in {
        checkErrorResponse(s"/$startUrl/customs-offices/$country", connector.getCustomsOfficesForCountry(country))
      }
    }

    "getCountries" - {

      "for all countries must" - {

        "return Seq of Country when successful" in {
          server.stubFor(
            get(urlEqualTo(s"/$startUrl/countries"))
              .willReturn(okJson(countryListResponseJson))
          )

          val expectedResult: Seq[Country] = Seq(
            Country(CountryCode("GB"), "United Kingdom"),
            Country(CountryCode("AD"), "Andorra")
          )

          connector.getCountries(Nil).futureValue mustEqual expectedResult
        }

        "return an exception when an error response is returned" in {
          checkErrorResponse(s"/$startUrl/countries", connector.getCountries(Nil))
        }
      }

      "for transit countries must" - {

        val queryParameters = Seq("membership" -> "ctc")

        "return Seq of Country when successful" in {
          server.stubFor(
            get(urlEqualTo(s"/$startUrl/countries?membership=ctc"))
              .willReturn(okJson(countryListResponseJson))
          )

          val expectedResult: Seq[Country] = Seq(
            Country(CountryCode("GB"), "United Kingdom"),
            Country(CountryCode("AD"), "Andorra")
          )

          connector.getCountries(queryParameters).futureValue mustEqual expectedResult
        }

        "return an exception when an error response is returned" in {
          checkErrorResponse(s"/$startUrl/countries?membership=ctc", connector.getCountries(queryParameters))
        }
      }

    }

  }

  private def checkErrorResponse(url: String, result: Future[_]): Assertion =
    forAll(errorResponses) {
      errorResponse =>
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(
              aResponse()
                .withStatus(errorResponse)
            )
        )

        whenReady(result.failed) {
          _ mustBe an[Exception]
        }
    }
}
