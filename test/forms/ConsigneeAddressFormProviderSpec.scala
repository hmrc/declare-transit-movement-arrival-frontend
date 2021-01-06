/*
 * Copyright 2021 HM Revenue & Customs
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
import models.Address
import org.scalacheck.Gen
import play.api.data.{Field, FormError}
import wolfendale.scalacheck.regexp.RegexpGen

import scala.util.matching.Regex

class ConsigneeAddressFormProviderSpec extends StringFieldBehaviours with SpecBase {

  val form = new ConsigneeAddressFormProvider()(consigneeName)

  val requiredKey          = "consigneeAddress.error.required"
  val lengthKey            = "consigneeAddress.error.length"
  val invalidKey           = "consigneeAddress.error.invalid"
  val inputRegex: Regex    = "^[a-zA-Z0-9&'*/.\\-? <>]*$".r
  val postCodeRegex: Regex = "^[a-zA-Z]{1,2}([0-9]{1,2}|[0-9][a-zA-Z])\\s*[0-9][a-zA-Z]{2}$".r

  ".value" - {

    ".buildingAndStreet" - {

      val fieldName = "buildingAndStreet"
      val maxLength = 35

      val validAdressOverLength: Gen[String] = for {
        num  <- Gen.chooseNum[Int](maxLength + 1, maxLength + 5)
        list <- Gen.listOfN(num, Gen.alphaNumChar)
      } yield list.mkString("")

      val args = Seq(Address.Constants.Fields.buildingAndStreetName, consigneeName)

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
        validAdressOverLength
      )

      behave like mandatoryField(
        form,
        fieldName,
        requiredError = FormError(fieldName, requiredKey, args)
      )
    }

    "must not bind strings that do not match regex" in {
      val fieldName = "buildingAndStreet"
      val maxLength = 35

      val generator: Gen[String] = RegexpGen.from(s"[!£^(){}_+=:;|`~,±]{$maxLength}")
      val expectedError          = FormError(fieldName, invalidKey, Seq(inputRegex))

      forAll(generator) {
        invalidString =>
          val result: Field = form.bind(Map(fieldName -> invalidString)).apply(fieldName)
          result.errors must contain(expectedError)
      }
    }

    ".city" - {

      val fieldName = "city"
      val maxLength = 35

      val validAddressOverLength: Gen[String] = for {
        num  <- Gen.chooseNum[Int](maxLength + 1, maxLength + 5)
        list <- Gen.listOfN(num, Gen.alphaNumChar)
      } yield list.mkString("")

      val args = Seq(Address.Constants.Fields.city, consigneeName)

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
        validAddressOverLength
      )

      behave like mandatoryField(
        form,
        fieldName,
        requiredError = FormError(fieldName, requiredKey, args)
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
