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

import forms.mappings.Mappings
import javax.inject.Inject
import models.Address
import play.api.data.Form
import play.api.data.Forms._
import models.domain.TraderDomain.Constants.{cityLength, postCodeLength, streetAndNumberLength}

import scala.util.matching.Regex

class TraderAddressFormProvider @Inject() extends Mappings {

  /**
    * letters a to z
    * numbers 0 to 9
    * ampersands (&)
    * apostrophes
    * asterisks,
    * forward slashes
    * full stops
    * hyphens
    * question marks
    * and greater than (>) and less than (<) signs
    */
  val inputRegex: Regex = "[a-zA-Z0-9&'*/.\\-?<>]*".r

  def apply(traderName: String): Form[Address] = Form(
    mapping(
      "buildingAndStreet" -> text("traderAddress.error.buildingAndStreet.required", Seq(traderName))
        .verifying(
          maxLength(
            streetAndNumberLength,
            "traderAddress.error.buildingAndStreet.length",
            Seq("building and street name", traderName)
          )
        )
        .verifying(
          minLength(
            1,
            "traderAddress.error.empty",
            Seq("building and street name", traderName)
          )
        )
        .verifying(
          regexp(
            inputRegex,
            "traderAddress.error.invalid",
            Seq("building and street name", traderName)
          )
        ),
      "city" -> text("traderAddress.error.city.required", args = Seq(traderName))
        .verifying(
          maxLength(cityLength, "traderAddress.error.max_length", args = Seq("city", traderName))
        )
        .verifying(
          minLength(1, "traderAddress.error.empty", Seq("city", traderName))
        )
        .verifying(
          regexp(
            inputRegex,
            "traderAddress.error.invalid",
            Seq("city", traderName)
          )
        ),
      "postcode" -> text("traderAddress.error.postcode.required", args = Seq(traderName))
        .verifying(maxLength(postCodeLength, "traderAddress.error.postcode.length", args = Seq(traderName)))
        .verifying(minLength(1, "traderAddress.error.empty", args = Seq("postcode", traderName)))
        .verifying(regexp("[a-zA-Z0-9]*".r, "traderAddress.error.postcode.invalid", args = Seq(traderName)))
    )(Address.apply)(Address.unapply)
  )
}
