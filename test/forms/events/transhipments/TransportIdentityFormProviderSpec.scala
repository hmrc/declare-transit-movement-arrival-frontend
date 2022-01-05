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

import forms.behaviours.StringFieldBehaviours
import models.messages.VehicularTranshipment
import org.scalacheck.Gen
import play.api.data.{Field, FormError}
import wolfendale.scalacheck.regexp.RegexpGen

class TransportIdentityFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "transportIdentity.error.required"
  val lengthKey   = "transportIdentity.error.length"
  val invalidKey  = "transportIdentity.error.invalid"
  val maxLength   = VehicularTranshipment.Constants.transportIdentityLength

  val fieldName = "value"

  val form = new TransportIdentityFormProvider()()

  ".value" - {

    val validTransportIdentityOverLength: Gen[String] = for {
      num  <- Gen.chooseNum[Int](maxLength + 1, maxLength + 5)
      list <- Gen.listOfN(num, Gen.alphaNumChar)
    } yield list.mkString("")

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength)),
      validTransportIdentityOverLength
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  "must not bind strings that do not match regex" in {

    val generator: Gen[String] = RegexpGen.from(s"[!£^*(){}_+=:;|`~,±éèâñüç]{27}")
    val expectedError          = FormError(fieldName, invalidKey)

    forAll(generator) {
      invalidString =>
        val result: Field = form.bind(Map(fieldName -> invalidString)).apply(fieldName)
        result.errors must contain(expectedError)
    }
  }
}
