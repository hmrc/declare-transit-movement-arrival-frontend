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

package models

import base.SpecBase
import generators.MessagesModelGenerators
import models.reference.{Country, CountryCode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class CountryListSpec extends SpecBase with ScalaCheckPropertyChecks with MessagesModelGenerators {

  "CountryList" - {

    "getCountry" - {
      "must return correct country when in the fullList" in {

        forAll(arbitrary[Country]) {
          country =>
            val genUniqueCountryVector = arbitrary[Vector[Country]] retryUntil (!_.exists(_.code == country.code))

            forAll(genUniqueCountryVector) {
              countries =>
                val fullList: Vector[Country] = countries :+ country
                CountryList(fullList).getCountry(country.code).value mustEqual country
            }
        }
      }

      "must return None when no country has a matching countryCode" in {

        forAll(arbitrary[Vector[Country]]) {
          countries =>
            val genCountry: Gen[Country] = arbitrary[Country].retryUntil(
              value => !countries.exists(_.code == value.code)
            )
            forAll(genCountry) {
              country =>
                CountryList(countries).getCountry(country.code) mustBe None
            }
        }
      }
    }

    "equals" - {

      "returns true if both CountryLists are the same" in {
        val c1 = CountryList(Seq(Country(CountryCode("a"), "a")))
        val c2 = CountryList(Seq(Country(CountryCode("a"), "a")))
        c1 == c2 mustEqual true
      }

      "returns false if the rhs is not a CountryList" in {
        CountryList(Seq()) == 1 mustEqual false
      }

      "returns false if the rhs has a different list of countries" in {
        val c1 = CountryList(Seq(Country(CountryCode("a"), "a")))
        val c2 = CountryList(Seq(Country(CountryCode("b"), "b")))
        c1 == c2 mustEqual false
      }

      "returns false if the rhs has a different list of countries with duplicates" in {
        val c1 = CountryList(Seq(Country(CountryCode("a"), "a"), Country(CountryCode("a"), "a")))
        val c2 = CountryList(Seq(Country(CountryCode("a"), "a")))
        c1 == c2 mustEqual false
      }
    }
  }

}
