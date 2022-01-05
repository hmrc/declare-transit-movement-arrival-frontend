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

import forms.mappings.Mappings
import models.Address
import models.Address.Constants.Fields.city
import models.StringFieldRegex.stringFieldRegex
import models.domain.TraderDomain.Constants.{cityLength, postCodeLength, streetAndNumberLength}
import play.api.data.Form
import play.api.data.Forms._

import javax.inject.Inject

// format: off
class TraderAddressFormProvider @Inject() extends Mappings {

  def apply(traderName: String): Form[Address] = Form(
    mapping(
      "buildingAndStreet" -> text(
        "traderAddress.error.required",
        Seq(Address.Constants.Fields.buildingAndStreetName, traderName)
      ).verifying(maxLength(streetAndNumberLength,"traderAddress.error.length",
            Seq(Address.Constants.Fields.buildingAndStreetName, traderName)
          )
        )
        .verifying(minLength(1,"traderAddress.error.empty",
            Seq(Address.Constants.Fields.buildingAndStreetName, traderName)
          )
        )
        .verifying(regexp(stringFieldRegex,"traderAddress.error.invalid",
            Seq(Address.Constants.Fields.buildingAndStreetName, traderName)
          )
        ),
      "city" -> text("traderAddress.error.required", args = Seq(Address.Constants.Fields.city, traderName))
        .verifying(maxLength(cityLength, "traderAddress.error.length", args = Seq(Address.Constants.Fields.city, traderName))
        )
        .verifying(minLength(1, "traderAddress.error.required", Seq(Address.Constants.Fields.city, traderName))
        )
        .verifying(regexp(stringFieldRegex,"traderAddress.error.invalid", Seq(city, traderName))
        ),
      "postcode" -> text("traderAddress.error.postcode.required", args = Seq(traderName))
        .verifying(maxLength(postCodeLength, "traderAddress.error.postcode.length", args = Seq(traderName)))
        .verifying(minLength(1, "traderAddress.error.empty", args = Seq(Address.Constants.Fields.postcode, traderName)))
        .verifying(regexp("[\\sa-zA-Z0-9]*".r, "traderAddress.error.postcode.invalid", args = Seq(traderName)))
    )(Address.apply)(Address.unapply)
  )
  // format: on
}
