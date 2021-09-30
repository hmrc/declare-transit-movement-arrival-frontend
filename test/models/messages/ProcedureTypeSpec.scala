/*
 * Copyright 2021 HM Revenue & Customs
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

package models.messages

import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsError, JsString, JsSuccess, Json}

class ProcedureTypeSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks {

  "must deserialise from normal procedure" in {

    JsString("normal").validate[ProcedureType] mustEqual JsSuccess(ProcedureType.Normal)
  }

  "must deserialise from simplified procedure" in {

    JsString("simplified").validate[ProcedureType] mustEqual JsSuccess(ProcedureType.Simplified)
  }

  "must fail to deserialise from an invalid string" in {

    forAll(arbitrary[String]) {
      value =>
        whenever(value != "normal" && value != "simplified") {

          JsString(value).validate[ProcedureType] mustBe a[JsError]
        }
    }
  }

  "must serialise from Normal Procedure" in {

    Json.toJson(ProcedureType.Normal) mustEqual JsString("normal")
  }

  "must serialise from Simplified Procedure" in {

    Json.toJson(ProcedureType.Simplified) mustEqual JsString("simplified")
  }
}
