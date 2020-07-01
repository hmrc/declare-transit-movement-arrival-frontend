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

package forms.events.seals

import base.SpecBase
import forms.behaviours.StringFieldBehaviours
import generators.MessagesModelGenerators
import models.Index
import models.domain.SealDomain
import models.messages.Seal
import play.api.data.FormError

class SealIdentityFormProviderSpec extends StringFieldBehaviours with MessagesModelGenerators with SpecBase {

  val requiredKey  = "sealIdentity.error.required"
  val lengthKey    = "sealIdentity.error.length"
  val duplicateKey = "sealIdentity.error.duplicate"
  val maxLength    = 20
  val fieldName    = "value"

  val form = new SealIdentityFormProvider()

  ".value" - {

    behave like fieldThatBindsValidData(
      form(sealIndex),
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form(sealIndex),
      fieldName,
      maxLength   = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form(sealIndex),
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  "no errors if there are existing seal numbers or marks when applying against the same index" in {

    forAll(listWithMaxLength[SealDomain](10)) {
      seals =>
        val result = form(sealIndex, seals).bind(Map(fieldName -> seals.head.numberOrMark)).apply(fieldName)

        result.hasErrors mustEqual false
    }
  }

  "errors if there are existing seal numbers or marks and index is different from current" in {

    forAll(listWithMaxLength[SealDomain](10)) {
      seals =>
        val nextIndex = seals.length
        val index     = Index(nextIndex)
        val result    = form(index, seals).bind(Map(fieldName -> seals.head.numberOrMark)).apply(fieldName)

        result.errors mustEqual Seq(FormError(fieldName, duplicateKey))
    }
  }

  "no errors if there are no existing seal numbers or marks" in {
    forAll(listWithMaxLength[SealDomain](10)) {
      seals =>
        val sealsWithDuplicatesRemoved = seals.toSet.filterNot(_.numberOrMark == seal.numberOrMark).toSeq
        val result                     = form(sealIndex, sealsWithDuplicatesRemoved).bind(Map(fieldName -> seal.numberOrMark)).apply(fieldName)

        result.hasErrors mustEqual false
    }
  }

}
