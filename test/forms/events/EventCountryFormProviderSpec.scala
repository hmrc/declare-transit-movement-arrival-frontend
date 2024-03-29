/*
 * Copyright 2023 HM Revenue & Customs
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

package forms.events

import forms.behaviours.StringFieldBehaviours
import models.CountryList
import models.reference.{Country, CountryCode}
import play.api.data.FormError

class EventCountryFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "eventCountry.error.required"
  val lengthKey   = "eventCountry.error.length"
  val maxLength   = 2

  val countries = CountryList(Seq(Country(CountryCode("AD"), "Andorra")))
  val form      = new EventCountryFormProvider()(countries)

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    "not bind if country code does not exist in the country list" in {

      val boundForm = form.bind(Map("value" -> "foobar"))
      val field     = boundForm("value")
      field.errors mustNot be(empty)
    }

    "bind a country code which is in the list" in {

      val boundForm = form.bind(Map("value" -> "AD"))
      val field     = boundForm("value")
      field.errors must be(empty)
    }
  }
}
