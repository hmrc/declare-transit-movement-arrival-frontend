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
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, get, okJson, urlEqualTo}
import generators.MessagesModelGenerators
import helper.WireMockServerHandler
import models.reference.{Country, CountryCode, CustomsOffice}
import org.scalacheck.Gen
import org.scalatest.Assertion
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReferenceDataConnectorSpec extends SpecBase with WireMockServerHandler with MessagesModelGenerators with ScalaCheckPropertyChecks {

  private val startUrl = "transit-movements-trader-reference-data"

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      conf = "microservice.services.referenceData.port" -> server.port()
    )
    .build()

  private lazy val connector: ReferenceDataConnector = app.injector.instanceOf[ReferenceDataConnector]

  private val customsOfficeResponseJson: String =
    """
      |[
      | {
      |   "id" : "testId1",
      |   "name" : "testName1",
      |   "roles" : ["role1", "role2"],
      |   "phoneNumber" : "testPhoneNumber"
      | },
      | {
      |   "id" : "testId2",
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

  private val gbCountryJson: String =
    """
      |{
      |   "code":"GB",
      |   "state":"valid",
      |   "description":"United Kingdom"
      |}
      |""".stripMargin

  val errorResponses: Gen[Int] = Gen.chooseNum(400, 599)

  "Reference Data" - {

    "getCustomsOffices" - {
      "must return a successful future response with a sequence of CustomsOffices" in {
        server.stubFor(
          get(urlEqualTo(s"/$startUrl/customs-offices"))
            .willReturn(okJson(customsOfficeResponseJson))
        )

        val expectedResult = {
          Seq(
            CustomsOffice("testId1", "testName1", Seq("role1", "role2"), Some("testPhoneNumber")),
            CustomsOffice("testId2", "testName2", Seq("role1", "role2"), None)
          )
        }

        connector.getCustomsOffices.futureValue mustBe expectedResult
      }

      "must return an exception when an error response is returned" in {
        checkErrorResponse(s"/$startUrl/customs-offices", connector.getCustomsOffices)
      }
    }

    "getCountryList" - {

      "must return Seq of Country when successful" in {
        server.stubFor(
          get(urlEqualTo(s"/$startUrl/countries-full-list"))
            .willReturn(okJson(countryListResponseJson))
        )

        val expectedResult = Seq(
          CountryCode("GB"),
          CountryCode("AD")
        )

        connector.getCountryList.futureValue mustBe expectedResult
      }

      "must return an exception when an error response is returned" in {

        checkErrorResponse(s"/$startUrl/countries-full-list", connector.getCountryList)
      }
    }

    "getCountry" - {

      "must return a Country when successful" in {

        val code = CountryCode("GB")

        server.stubFor(
          get(urlEqualTo(s"/$startUrl/countries/$code"))
            .willReturn(okJson(gbCountryJson))
        )

        val expectedResult = Country(code, "United Kingdom")

        connector.getCountry(code).futureValue mustBe expectedResult
      }

      "must return an exception when an error response is returned" in {

        val invalidCode = CountryCode("ZZ")

        checkErrorResponse(s"/$startUrl/countries/$invalidCode", connector.getCountry(invalidCode))
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
