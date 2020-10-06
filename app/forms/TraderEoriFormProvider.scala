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
import play.api.data.Form
import models.domain.TraderDomain.Constants._
import models.domain.TraderDomain.eoriRegex
import play.api.data.validation.{Constraint, Invalid, Valid}

import java.util.Locale

object TraderEoriUtils {
  val isoCountries     = Locale.getISOCountries.toList
  val numericOnlyRegex = "[0-9]*"
}

class TraderEoriFormProvider @Inject() extends Mappings {

  def eoriFormat(errorKey: String, args: Seq[Any]): Constraint[String] =
    Constraint {
      str =>
        // We first breakdown the input string in 2 parts.
        // The country code.
        val countryCode = str.take(2)

        // And the numeric part of the EORI number.
        val restOfString = str.drop(2)

        if (// Input country code is a valid ISO country.
            TraderEoriUtils.isoCountries.contains(countryCode) &&
            // String has to be at most 12 characters
            restOfString.length <= (eoriLength - 2) &&

            // The rest of the string has only numbers.
            restOfString.matches(TraderEoriUtils.numericOnlyRegex)) {
          Valid
        } else {
          Invalid(errorKey, args: _*)
        }
    }

  def apply(traderName: String): Form[String] =
    Form(
      "value" -> text("traderEori.error.required", Seq(traderName))
        .verifying(
          maxLength(eoriLength, "traderEori.error.length", Seq(traderName)),
          regexp(eoriRegex.r, "traderEori.error.invalid", Seq(traderName)),
          eoriFormat("traderEori.error.format", Seq(traderName))
        )
        .verifying()
    )
}
