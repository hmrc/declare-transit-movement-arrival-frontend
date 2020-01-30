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

import forms.behaviours.StringFieldBehaviours
import generators.{Generators, MessagesModelGenerators}
import models.messages.{Container, Transhipment}
import org.scalacheck.Arbitrary
import play.api.data.FormError
import org.scalacheck.Arbitrary.arbitrary

class ContainerNumberFormProviderSpec extends StringFieldBehaviours with MessagesModelGenerators {

  val requiredKey  = "containerNumber.error.required"
  val lengthKey    = "containerNumber.error.length"
  val duplicateKey = "containerNumber.error.duplicate"
  val maxLength    = Transhipment.Constants.containerLength

  val form = new ContainerNumberFormProvider()

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form(),
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form(),
      fieldName,
      maxLength   = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form(),
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    "errors if there are new container is duplicate" in {
      forAll(listWithMaxLength[Container](10), arbitrary[Container]) {
        case (containers, container @ Container(containerNumber)) =>
          val containersWithDuplicate: Seq[Container] = containers :+ container

          val result = form(containersWithDuplicate).bind(Map(fieldName -> containerNumber)).apply(fieldName)

          result.errors mustEqual Seq(FormError(fieldName, duplicateKey))
      }
    }

    "no errors if there are new container is not a duplicate" in {
      forAll(listWithMaxLength[Container](10), arbitrary[Container]) {
        case (containers1, container @ Container(containerNumber)) =>
          val containersNoDuplicate = containers1.filterNot(_ == container)

          val result = form(containersNoDuplicate).bind(Map(fieldName -> containerNumber)).apply(fieldName)

          result.hasErrors mustEqual false
      }
    }

  }
}
