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

package models

import base.SpecBase
import generators.MessagesModelGenerators
import models.reference.Country
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks


class CountryListSpec extends SpecBase with ScalaCheckPropertyChecks with MessagesModelGenerators {

  "CountryList" - {

    "fullList" - {

      "must return all countries" in {

        forAll(arbitrary[Vector[Country]]) {
          countries =>
            CountryList(countries).fullList mustBe countries
        }
      }
    }

    "getCountry" - {
      "must return correct country when in the fullList" in {

        forAll(arbitrary[Vector[Country]], arbitrary[Country]) {
          (countries, country) =>
            val fullList: Vector[Country] = countries :+ country
            CountryList(fullList).getCountry(country.code).value mustBe country
        }
      }

      "must return None when no country has a matching countryCode" in {

        forAll(arbitrary[Vector[Country]]) {
          countries =>
            val genCountry: Gen[Country] = arbitrary[Country].suchThat(!countries.contains(_))
            forAll(genCountry) {
              country =>
                CountryList(countries).getCountry(country.code).value mustBe None
            }
        }
      }
    }
  }

}
