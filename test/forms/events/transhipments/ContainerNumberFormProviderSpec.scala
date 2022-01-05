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

package forms.events.transhipments

import base.SpecBase
import forms.behaviours.StringFieldBehaviours
import generators.MessagesModelGenerators
import models.Index
import models.domain.ContainerDomain
import models.messages.Transhipment
import play.api.data.FormError

class ContainerNumberFormProviderSpec extends StringFieldBehaviours with MessagesModelGenerators with SpecBase {

  val requiredKey  = "containerNumber.error.required"
  val lengthKey    = "containerNumber.error.length"
  val duplicateKey = "containerNumber.error.duplicate"
  val invalidKey   = "containerNumber.error.invalid"
  val maxLength    = Transhipment.Constants.containerLength

  val form = new ContainerNumberFormProvider()

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form(containerIndex),
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form(containerIndex),
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form(containerIndex),
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    "no errors if there are existing container numbers when applying against the same index" in {

      forAll(listWithMaxLength[ContainerDomain](10)) {
        containers =>
          val containersWithoutDuplicates = containers.distinct
          val result                      = form(containerIndex, containersWithoutDuplicates).bind(Map(fieldName -> containers.head.containerNumber)).apply(fieldName)

          result.hasErrors mustEqual false
      }
    }

    "errors if there are existing container numbers and index is different from current" in {

      forAll(listWithMaxLength[ContainerDomain](10)) {
        containers =>
          val nextIndex = containers.length
          val index     = Index(nextIndex)

          val result = form(index, containers).bind(Map(fieldName -> containers.head.containerNumber)).apply(fieldName)

          result.errors mustEqual Seq(FormError(fieldName, duplicateKey))
      }
    }

    "no errors if there are no existing container number" in {

      val result = form(containerIndex, Seq.empty).bind(Map(fieldName -> container.containerNumber)).apply(fieldName)

      result.hasErrors mustEqual false
    }

  }
}
