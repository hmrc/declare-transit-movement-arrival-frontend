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
import org.scalacheck.Gen
import play.api.data.FormError

class ConsigneeAddressFormProviderSpec extends StringFieldBehaviours with SpecBase {

  val form = new ConsigneeAddressFormProvider()(consigneeName)

  ".value" - {

    ".buildingAndStreet" - {

      val fieldName   = "buildingAndStreet"
      val requiredKey = "consigneeAddress.error.buildingAndStreet.required"
      val lengthKey   = "consigneeAddress.error.buildingAndStreet.length"
      val maxLength   = 35

      val validAdressOverLength: Gen[String] = for {
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
        lengthError = FormError(fieldName, lengthKey, Seq("building and street name", consigneeName)),
        validAdressOverLength
      )

      behave like mandatoryField(
        form,
        fieldName,
        requiredError = FormError(fieldName, requiredKey, Seq(consigneeName))
      )
    }

    ".city" - {

      val fieldName   = "city"
      val requiredKey = "consigneeAddress.error.city.required"
      val lengthKey   = "consigneeAddress.error.max_length"
      val maxLength   = 35

      val validAdressOverLength: Gen[String] = for {
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
        lengthError = FormError(fieldName, lengthKey, Seq("city", consigneeName)),
        validAdressOverLength
      )

      behave like mandatoryField(
        form,
        fieldName,
        requiredError = FormError(fieldName, requiredKey, Seq(consigneeName))
      )
    }

    ".postcode" - {

      val fieldName   = "postcode"
      val requiredKey = "consigneeAddress.error.postcode.required"
      val lengthKey   = "consigneeAddress.error.postcode.length"
      val maxLength   = 9

      val validAdressOverLength: Gen[String] = for {
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
        lengthError = FormError(fieldName, lengthKey, Seq(consigneeName)),
        validAdressOverLength
      )

      behave like mandatoryField(
        form,
        fieldName,
        requiredError = FormError(fieldName, requiredKey, Seq(consigneeName))
      )
    }
  }
}
