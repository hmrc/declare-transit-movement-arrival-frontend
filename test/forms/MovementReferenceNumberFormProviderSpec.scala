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
import models.MovementReferenceNumber
import org.scalacheck.Arbitrary.arbitrary
import play.api.data.FormError

class MovementReferenceNumberFormProviderSpec extends StringFieldBehaviours {

  val requiredKey         = "movementReferenceNumber.error.required"
  val invalidKey          = "movementReferenceNumber.error.invalid"
  val invalidCharacterKey = "movementReferenceNumber.error.invalidCharacter"

  val form = new MovementReferenceNumberFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      arbitrary[MovementReferenceNumber].map(_.toString)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    "must not bind when value is invalid format" in {

      forAll(alphaStringsWithMaxLength(MovementReferenceNumber.Constants.length)) {
        invalidMrn =>
          whenever(invalidMrn != "" && MovementReferenceNumber(invalidMrn).isEmpty) {

            val result = form.bind(Map("value" -> invalidMrn))
            result.errors must contain(FormError("value", invalidKey))
          }
      }
    }

    "must not bind when value contains an invalid character" in {

      forAll(nonEmptyString) {
        value =>
          whenever(
            MovementReferenceNumber(value).isEmpty &&
              !value.matches(MovementReferenceNumber.Constants.validCharactersRegex)
          ) {
            val result = form.bind(Map("value" -> value))
            result.errors must contain(FormError("value", invalidCharacterKey))
          }
      }
    }

  }
}
