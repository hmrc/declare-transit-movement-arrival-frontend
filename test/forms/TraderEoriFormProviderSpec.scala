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

package forms

import forms.behaviours.StringFieldBehaviours
import org.scalacheck.Gen
import play.api.data.{Field, FormError}
import wolfendale.scalacheck.regexp.RegexpGen

class TraderEoriFormProviderSpec extends StringFieldBehaviours {

  private val requiredKey = "traderEori.error.required"
  private val lengthKey   = "traderEori.error.length"
  private val invalidKey  = "traderEori.error.invalid"
  private val maxLength   = 17

  private val form = new TraderEoriFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    "must not bind strings longer than 17 characters" in {

      val genInvalidLengthString = RegexpGen.from("[A-Z]{2}[^\n\r]{15,}")
      val expectedError          = FormError(fieldName, lengthKey, Seq(maxLength))

      forAll(genInvalidLengthString) {
        string =>
          val result = form.bind(Map(fieldName -> string)).apply(fieldName)
          result.errors mustEqual Seq(expectedError)
      }
    }

    "must not bind strings that do not match regex" in {

      val validRegex    = "[A-Z]{2}[^\n\r]{1,}"
      val expectedError = FormError(fieldName, invalidKey, Seq(validRegex))

      val genInvalidString: Gen[String] = {
        stringsWithMaxLength(maxLength) suchThat (!_.matches("[A-Z]{2}[^\n\r]{15}"))
      }

      forAll(genInvalidString) {
        invalidString =>
          val result: Field = form.bind(Map(fieldName -> invalidString)).apply(fieldName)
          result.errors mustEqual Seq(expectedError)
      }
    }
  }
}
