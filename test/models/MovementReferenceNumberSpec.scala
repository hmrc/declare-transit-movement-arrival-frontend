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

package models

import generators.ModelGenerators
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.{EitherValues, FreeSpec, MustMatchers}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsString, JsSuccess, Json}
import play.api.mvc.PathBindable

class MovementReferenceNumberSpec extends FreeSpec with MustMatchers with ScalaCheckPropertyChecks
  with ModelGenerators with EitherValues {

  "a Movement Reference Number" - {

    val pathBindable = implicitly[PathBindable[MovementReferenceNumber]]

    "must bind from a url" in {

      forAll(arbitrary[String], arbitrary[MovementReferenceNumber]) {
        (key, mrn) =>

          pathBindable.bind(key, mrn.value).right.value mustEqual mrn
      }
    }

    "must unbind to a url" in {

      forAll(arbitrary[String], arbitrary[MovementReferenceNumber]) {
        (key, mrn) =>

          pathBindable.unbind(key, mrn) mustEqual mrn.value
      }
    }

    "must deserialise" in {

      forAll(arbitrary[String]) {
        value =>

          JsString(value).validate[MovementReferenceNumber] mustEqual JsSuccess(MovementReferenceNumber(value))
      }
    }

    "must serialise" in {

      forAll(arbitrary[MovementReferenceNumber]) {
        mrn =>

          Json.toJson(mrn) mustEqual JsString(mrn.value)
      }
    }
  }
}
