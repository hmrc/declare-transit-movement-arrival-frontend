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
import play.api.data.FormError
import wolfendale.scalacheck.regexp.RegexpGen

class AuthorisedLocationFormProviderSpec extends StringFieldBehaviours {

  private val requiredKey = "authorisedLocation.error.required"
  private val maxLength   = 17
  private val invalidKey  = "authorisedLocation.error.invalid"
  private val lengthKey   = "authorisedLocation.error.length"

  private val form      = new AuthorisedLocationFormProvider()()
  private val fieldName = "value"

  ".value" - {

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    "must not bind strings that invalid string" in {

      val expectedError = List(FormError(fieldName, invalidKey))

      val generator: Gen[String] = RegexpGen.from(s"[!£^*(){}_+=:;|`~,±éèâñüç]{17}")

      forAll(generator) {
        invalidString =>
          val result = form.bind(Map(fieldName -> invalidString)).apply(fieldName)
          result.errors mustBe expectedError
      }
    }
  }
}
