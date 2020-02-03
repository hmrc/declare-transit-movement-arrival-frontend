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

package forms.events.transhipments

import base.SpecBase
import forms.behaviours.StringFieldBehaviours
import generators.MessagesModelGenerators
import models.messages.{Container, Transhipment}
import play.api.data.FormError

class ContainerNumberFormProviderSpec extends StringFieldBehaviours with MessagesModelGenerators with SpecBase {

  val requiredKey  = "containerNumber.error.required"
  val lengthKey    = "containerNumber.error.length"
  val duplicateKey = "containerNumber.error.duplicate"
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
      maxLength   = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form(containerIndex),
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    "errors if there are existing container numbers" in {

      forAll(listWithMaxLength[Container](10)) {
        containers =>
          val result = form(containerIndex, containers).bind(Map(fieldName -> containers.head.containerNumber)).apply(fieldName)

          result.errors mustEqual Seq(FormError(fieldName, duplicateKey))
      }
    }

    "no errors if there are no existing container number" in {
      forAll(listWithMaxLength[Container](10)) {
        containers =>
          val containersWithDuplicatesRemoved = {
            containers.toSet.filterNot(_.containerNumber == container.containerNumber).toSeq
          }

          val result = {
            form(containerIndex, containersWithDuplicatesRemoved)
              .bind(Map(fieldName -> container.containerNumber))
              .apply(fieldName)
          }

          result.hasErrors mustEqual false
      }
    }
  }
}
