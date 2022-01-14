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

package services

import base.SpecBase
import connectors.ReferenceDataConnector
import generators.Generators
import models.CountryList
import models.reference.{Country, CountryCode, CountryReferenceDataEndpoint}
import org.mockito.Matchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, verify, when}
import org.scalacheck.Arbitrary._
import org.scalatest.BeforeAndAfterEach
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CountriesServiceSpec extends SpecBase with BeforeAndAfterEach with Generators {

  private val mockRefDataConnector: ReferenceDataConnector = mock[ReferenceDataConnector]
  private val service                                      = new CountriesService(mockRefDataConnector)

  private val country1: Country       = Country(CountryCode("GB"), "United Kingdom")
  private val country2: Country       = Country(CountryCode("FR"), "France")
  private val country3: Country       = Country(CountryCode("ES"), "Spain")
  private val countries: Seq[Country] = Seq(country1, country2, country3)

  implicit val hc: HeaderCarrier = new HeaderCarrier()

  override def beforeEach(): Unit = {
    reset(mockRefDataConnector)
    super.beforeEach()
  }

  "CountriesService" - {

    "getCountries" - {
      "must return a list of sorted countries" in {

        forAll(arbitrary[CountryReferenceDataEndpoint]) {
          endpoint =>
            beforeEach()

            when(mockRefDataConnector.getCountries(any())(any(), any()))
              .thenReturn(Future.successful(countries))

            service.getCountries(endpoint).futureValue mustBe
              CountryList(Seq(country2, country3, country1))

            verify(mockRefDataConnector).getCountries(eqTo(endpoint))(any(), any())
        }
      }
    }
  }
}