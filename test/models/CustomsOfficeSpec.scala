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

import base.SpecBase
import generators.Generators
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.MustMatchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsValue, Json}

class CustomsOfficeSpec extends SpecBase with MustMatchers with ScalaCheckPropertyChecks with Generators {

  "CustomOffice" - {

    "deserialise" in {

      forAll(arbitrary[CustomsOffice]) {
        customsOffice =>
          {
            val json = customsOfficeJson(customsOffice.id, customsOffice.name, customsOffice.roles)
            json.as[CustomsOffice] mustBe customsOffice
          }
      }
    }

    "serialise" in {

      forAll(arbitrary[CustomsOffice]) {
        customsOffice =>
          {
            val json = customsOfficeJson(customsOffice.id, customsOffice.name, customsOffice.roles)
            Json.toJson(customsOffice) mustBe json
          }
      }
    }

    def customsOfficeJson(id: String, name: String, roles: Seq[String]): JsValue =
      Json.obj(
        "id"    -> id,
        "name"  -> name,
        "roles" -> roles
      )
  }

}
