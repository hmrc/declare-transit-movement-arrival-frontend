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
import play.api.data.FormError

class TraderAddressFormProviderSpec extends StringFieldBehaviours {

  val traderName = "trader_name"
  val form       = new TraderAddressFormProvider()(traderName)

  ".buildingAndStreet" - {

    val fieldName   = "buildingAndStreet"
    val requiredKey = "traderAddress.error.buildingAndStreet.required"
    val lengthKey   = "traderAddress.error.buildingAndStreet.length"
    val maxLength   = 35

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength   = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq("building and street name", traderName))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey, Seq(traderName))
    )
  }

  ".city" - {

    val fieldName   = "city"
    val requiredKey = "traderAddress.error.city.required"
    val lengthKey   = "traderAddress.error.city.length"
    val maxLength   = 35

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength   = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq("city", traderName))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey, Seq(traderName))
    )
  }

  ".postcode" - {

    val fieldName   = "postcode"
    val requiredKey = "traderAddress.error.postcode.required"
    val lengthKey   = "traderAddress.error.postcode.length"
    val maxLength   = 9

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength   = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
