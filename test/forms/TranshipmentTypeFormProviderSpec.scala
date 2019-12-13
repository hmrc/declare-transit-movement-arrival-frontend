/*
 * Copyright 2019 HM Revenue & Customs
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

import forms.behaviours.OptionFieldBehaviours
import forms.events.transhipments.TranshipmentTypeFormProvider
import models.TranshipmentType
import play.api.data.FormError

class TranshipmentTypeFormProviderSpec extends OptionFieldBehaviours {

  val form = new TranshipmentTypeFormProvider()()

  ".value" - {

    val fieldName   = "value"
    val requiredKey = "transhipmentType.error.required"

    behave like optionsField[TranshipmentType](
      form,
      fieldName,
      validValues  = TranshipmentType.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
