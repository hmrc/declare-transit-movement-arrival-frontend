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
import models.Address
import org.scalacheck.Gen
import play.api.data.FormError

class TraderAddressFormProviderSpec extends StringFieldBehaviours {

  val traderName = "traderName"
  val form       = new TraderAddressFormProvider()(traderName)

  val maxLength = 35

  val validAddressStringGenOverLength: Gen[String] = for {
    num  <- Gen.chooseNum[Int](maxLength + 1, maxLength + 5)
    list <- Gen.listOfN(num, Gen.alphaNumChar)
  } yield list.mkString("")

  ".buildingAndStreet" - {

    val fieldName   = "buildingAndStreet"
    val requiredKey = "traderAddress.error.required"
    val lengthKey   = "traderAddress.error.max_length"

    val args = Seq(Address.Constants.Fields.buildingAndStreetName, traderName)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength   = maxLength,
      lengthError = FormError(fieldName, lengthKey, args),
      validAddressStringGenOverLength
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey, args)
    )
  }

  ".city" - {

    val fieldName   = "city"
    val requiredKey = "traderAddress.error.required"
    val lengthKey   = "traderAddress.error.max_length"
    val maxLength   = 35

    val args = Seq(Address.Constants.Fields.city, traderName)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength   = maxLength,
      lengthError = FormError(fieldName, lengthKey, args),
      validAddressStringGenOverLength
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey, args)
    )
  }

  ".postcode" - {

    val fieldName   = "postcode"
    val requiredKey = "traderAddress.error.postcode.required"
    val lengthKey   = "traderAddress.error.postcode.length"
    val maxLength   = 9

    val validAddressStringGenOverLength: Gen[String] = for {
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
      maxLength   = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(traderName)),
      validAddressStringGenOverLength
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey, Seq(traderName))
    )
  }
}
