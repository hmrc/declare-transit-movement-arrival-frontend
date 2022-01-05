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

import base.SpecBase
import forms.behaviours.StringFieldBehaviours
import models.domain.TraderDomain.Constants.eoriLength
import org.scalacheck.Gen
import play.api.data.{Field, FormError}
import wolfendale.scalacheck.regexp.RegexpGen

class EoriNumberFormProviderSpec extends StringFieldBehaviours with SpecBase {

  private val requiredKey = "eoriNumber.error.required"
  private val invalidKey  = "eoriNumber.error.invalid"
  private val formatKey   = "eoriNumber.error.format"

  val form = new EoriNumberFormProvider()(consigneeName)

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
      requiredError = FormError(fieldName, requiredKey, Seq(consigneeName))
    )

    "must not bind strings that do not match validation regex" in {

      val expectedError          = FormError(fieldName, invalidKey, Seq(consigneeName))
      val generator: Gen[String] = RegexpGen.from(s"[A-Za-z0-9 ]{14}")

      forAll(generator) {
        invalidString =>
          val result: Field = form.bind(Map(fieldName -> invalidString)).apply(fieldName)
          result.errors must contain(expectedError)
      }
    }

    "must not bind strings that do not match format regex" in {

      val expectedError          = FormError(fieldName, formatKey, Seq(consigneeName))
      val generator: Gen[String] = RegexpGen.from(s"[AB]{2}([0-9]){12}")

      forAll(generator) {
        invalidString =>
          val result: Field = form.bind(Map(fieldName -> invalidString)).apply(fieldName)
          result.errors must contain(expectedError)
      }
    }
  }
}
