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
import models.messages.TraderWithEori.Constants._

class TraderEoriFormProviderSpec extends StringFieldBehaviours {

  private val requiredKey  = "traderEori.error.required"
  private val lengthKey    = "traderEori.error.length"
  private val minLengthKey = "traderEori.error.minLength"
  private val invalidKey   = "traderEori.error.invalid"

  private val form = new TraderEoriFormProvider()()

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
      requiredError = FormError(fieldName, requiredKey)
    )

    "must not bind strings shorter than TraderWithEori.Constants.eoriMinLength characters" in {

      val expectedError = FormError(fieldName, minLengthKey, Seq(eoriMinLength))

      forAll(stringsWithMaxLength(eoriMinLength - 1)) {
        string =>
          val result = form.bind(Map(fieldName -> string)).apply(fieldName)
          result.errors must contain(expectedError)
      }
    }

    "must not bind strings longer than TraderWithEori.Constants.eoriLength characters" in {

      val expectedError = FormError(fieldName, lengthKey, Seq(eoriLength))

      forAll(stringsLongerThan(eoriLength + 1)) {
        string =>
          val result = form.bind(Map(fieldName -> string)).apply(fieldName)
          result.errors must contain(expectedError)
      }
    }

    "must not bind strings that do not match regex" in {

      val validRegex    = "[A-Z]{2}[^\n\r]{1,}"
      val expectedError = FormError(fieldName, invalidKey, Seq(validRegex))

      val genInvalidString: Gen[String] = {
        stringsWithMaxLength(eoriLength) suchThat (!_.matches("[A-Z]{2}[^\n\r]{1,}"))
      }

      forAll(genInvalidString) {
        invalidString =>
          val result: Field = form.bind(Map(fieldName -> invalidString)).apply(fieldName)
          result.errors must contain(expectedError)
      }
    }
  }
}
