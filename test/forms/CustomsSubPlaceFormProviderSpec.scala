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

package forms

import forms.behaviours.StringFieldBehaviours
import org.scalacheck.Gen
import play.api.data.{Field, FormError}
import wolfendale.scalacheck.regexp.RegexpGen

class CustomsSubPlaceFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "customsSubPlace.error.required"
  val lengthKey   = "customsSubPlace.error.length"
  val invalidKey  = "customsSubPlace.error.invalid"
  val maxLength   = 17

  val validCustomsSubPlaceOverLength: Gen[String] = for {
    num  <- Gen.chooseNum[Int](maxLength + 1, maxLength + 5)
    list <- Gen.listOfN(num, Gen.alphaNumChar)
  } yield list.mkString("")

  val form = new CustomsSubPlaceFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength)),
      validCustomsSubPlaceOverLength
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    "return error when given a non-ASCII character" in {

      forAll(extendedAsciiWithMaxLength(maxLength)) {
        invalidCharacters =>
          val invalidCharacterError = FormError(fieldName, invalidKey)

          val result = form.bind(Map(fieldName -> invalidCharacters)).apply(fieldName)
          result.errors mustEqual Seq(invalidCharacterError)
      }
    }

    "must not bind strings that do not match regex" in {

      val expectedError          = FormError(fieldName, invalidKey)
      val generator: Gen[String] = RegexpGen.from(s"[!£^*(){}_+=:;|`~,±üçñèé@]{17}")

      forAll(generator) {
        invalidString =>
          val result: Field = form.bind(Map(fieldName -> invalidString)).apply(fieldName)
          result.errors must contain(expectedError)
      }
    }
  }
}
