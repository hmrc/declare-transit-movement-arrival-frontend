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

package forms

import forms.behaviours.StringFieldBehaviours
import models.domain.TraderDomain.Constants._
import org.scalacheck.Gen
import play.api.data.{Field, FormError}
import wolfendale.scalacheck.regexp.RegexpGen

class TraderEoriFormProviderSpec extends StringFieldBehaviours {

  private val requiredKey = "traderEori.error.required"
  private val lengthKey   = "traderEori.error.length"
  private val invalidKey  = "traderEori.error.invalid"
  val traderName          = "traderName"

  private val form = new TraderEoriFormProvider()(traderName)

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(eoriLength)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey, Seq(traderName))
    )

    "must not bind strings longer than TraderWithEori.Constants.eoriLength characters" in {

      val expectedError = FormError(fieldName, lengthKey, Seq(traderName))

      forAll(stringsLongerThan(eoriLength + 1)) {
        string =>
          val result = form.bind(Map(fieldName -> string)).apply(fieldName)
          result.errors must contain(expectedError)
      }
    }

    "must not bind strings that do not match regex" in {

      val expectedError          = FormError(fieldName, invalidKey, Seq(traderName))
      val generator: Gen[String] = RegexpGen.from(s"[!£^*(){}_+=:;|`~,±üçñèé]{35}")

      forAll(generator) {
        invalidString =>
          val result: Field = form.bind(Map(fieldName -> invalidString)).apply(fieldName)
          result.errors must contain(expectedError)
      }
    }
  }
}
