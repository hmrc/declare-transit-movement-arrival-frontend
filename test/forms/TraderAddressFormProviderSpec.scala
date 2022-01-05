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

package forms

import forms.behaviours.StringFieldBehaviours
import models.Address
import org.scalacheck.Gen
import play.api.data.{Field, FormError}
import wolfendale.scalacheck.regexp.RegexpGen

class TraderAddressFormProviderSpec extends StringFieldBehaviours {

  val traderName                   = "traderName"
  lazy val traderAddressInvalidKey = "traderAddress.error.invalid"

  val form = new TraderAddressFormProvider()(traderName)

  val maxLength = 35

  val validAddressStringGenOverLength: Gen[String] = for {
    num  <- Gen.chooseNum[Int](maxLength + 1, maxLength + 5)
    list <- Gen.listOfN(num, Gen.alphaNumChar)
  } yield list.mkString("")

  ".buildingAndStreet" - {

    val fieldName   = "buildingAndStreet"
    val requiredKey = "traderAddress.error.required"
    val lengthKey   = "traderAddress.error.length"

    val args = Seq(Address.Constants.Fields.buildingAndStreetName, traderName)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, args),
      validAddressStringGenOverLength
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey, args)
    )

    "must not bind strings that do not match regex" in {
      val fieldName = "buildingAndStreet"
      val args      = Seq(Address.Constants.Fields.buildingAndStreetName, traderName)

      val generator: Gen[String] = RegexpGen.from(s"[!£^(){}_+=:;|`~,±<>]{${Address.Constants.buildingAndStreetLength}}")
      val expectedError          = FormError(fieldName, traderAddressInvalidKey, args)

      forAll(generator) {
        invalidString =>
          val result: Field = form.bind(Map(fieldName -> invalidString)).apply(fieldName)
          result.errors must contain(expectedError)
      }
    }
  }

  ".city" - {

    val fieldName   = "city"
    val requiredKey = "traderAddress.error.required"
    val lengthKey   = "traderAddress.error.length"
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
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, args),
      validAddressStringGenOverLength
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey, args)
    )

    "must not bind strings that do not match regex" in {
      val fieldName = "city"
      val args      = Seq(Address.Constants.Fields.city, traderName)

      val generator: Gen[String] = RegexpGen.from(s"[!£^(){}_+=:;|`~,±<>éèâñüç]{${Address.Constants.cityLength}}")
      val expectedError          = FormError(fieldName, traderAddressInvalidKey, args)

      forAll(generator) {
        invalidString =>
          val result: Field = form.bind(Map(fieldName -> invalidString)).apply(fieldName)
          result.errors must contain(expectedError)
      }
    }
    "must not bind strings that include accented/disallowed characters" in {
      val fieldName              = "city"
      val args                   = Seq(Address.Constants.Fields.city, traderName)
      val expectedError          = FormError(fieldName, traderAddressInvalidKey, args)
      val generator: Gen[String] = RegexpGen.from(s"[<>^éèâ±ñüç]{17}")

      forAll(generator) {
        invalidString =>
          val result: Field = form.bind(Map(fieldName -> invalidString)).apply(fieldName)
          result.errors must contain(expectedError)

      }
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
        maxLength = maxLength,
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
}
