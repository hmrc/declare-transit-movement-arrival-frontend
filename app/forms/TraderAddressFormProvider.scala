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
import models.messages.TraderWithEori.Constants.{cityLength, postCodeLength, streetAndNumberLength}

class TraderAddressFormProvider @Inject() extends Mappings {

  def apply(): Form[Address] = Form(
    mapping(
      "buildingAndStreet" -> text("traderAddress.error.buildingAndStreet.required")
        .verifying(maxLength(streetAndNumberLength, "traderAddress.error.buildingAndStreet.length")),
      "city" -> text("traderAddress.error.city.required")
        .verifying(maxLength(cityLength, "traderAddress.error.city.length")),
      "postcode" -> text("traderAddress.error.postcode.required")
        .verifying(maxLength(postCodeLength, "traderAddress.error.postcode.length"))
    )(Address.apply)(Address.unapply)
  )
}
