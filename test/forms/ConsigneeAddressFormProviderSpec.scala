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

import base.SpecBase
import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class ConsigneeAddressFormProviderSpec extends StringFieldBehaviours with SpecBase {

  val form = new ConsigneeAddressFormProvider()(consigneeName)

  ".value" - {

    ".buildingAndStreet" - {

      val fieldName   = "buildingAndStreet"
      val requiredKey = "consigneeAddress.error.buildingAndStreet.required"
      val lengthKey   = "consigneeAddress.error.buildingAndStreet.length"
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
        lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
      )

      behave like mandatoryField(
        form,
        fieldName,
        requiredError = FormError(fieldName, requiredKey)
      )
    }

    ".city" - {

      val fieldName   = "city"
      val requiredKey = "consigneeAddress.error.city.required"
      val lengthKey   = "consigneeAddress.error.city.length"
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
        lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
      )

      behave like mandatoryField(
        form,
        fieldName,
        requiredError = FormError(fieldName, requiredKey)
      )
    }

    ".postcode" - {

      val fieldName   = "postcode"
      val requiredKey = "consigneeAddress.error.postcode.required"
      val lengthKey   = "consigneeAddress.error.postcode.length"
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
}
