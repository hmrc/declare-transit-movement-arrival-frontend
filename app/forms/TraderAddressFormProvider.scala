/*
 * Copyright 2019 HM Revenue & Customs
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

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form
import play.api.data.Forms._
import models.TraderAddress

class TraderAddressFormProvider @Inject() extends Mappings {

  def apply(): Form[TraderAddress] = Form(
    mapping(
      "buildingAndStreet" -> text("traderAddress.error.buildingAndStreet.required")
          .verifying(maxLength(35, "traderAddress.error.buildingAndStreet.length")),
      "city" -> text("traderAddress.error.city.required")
          .verifying(maxLength(35, "traderAddress.error.city.length")),
      "postcode" -> text("traderAddress.error.postcode.required")
          .verifying(maxLength(9, "traderAddress.error.postcode.length"))
    )(TraderAddress.apply)(TraderAddress.unapply)
  )
}
