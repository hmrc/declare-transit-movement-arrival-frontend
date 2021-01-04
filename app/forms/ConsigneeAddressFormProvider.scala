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

import javax.inject.Inject
import forms.mappings.Mappings
import models.Address
import play.api.data.Form
import play.api.data.Forms._
import models.domain.TraderDomain.Constants.{cityLength, postCodeLength, streetAndNumberLength}
import models.domain.TraderDomain.inputRegex

class ConsigneeAddressFormProvider @Inject() extends Mappings {

  def apply(authorisedConsignee: String): Form[Address] = Form(
    mapping(
      "buildingAndStreet" -> text(
        "consigneeAddress.error.required",
        Seq(Address.Constants.Fields.buildingAndStreetName, authorisedConsignee)
      ).verifying(
          maxLength(
            streetAndNumberLength,
            "consigneeAddress.error.max_length",
            Seq(Address.Constants.Fields.buildingAndStreetName, authorisedConsignee)
          )
        )
        .verifying(
          minLength(
            1,
            "consigneeAddress.error.empty",
            Seq(Address.Constants.Fields.buildingAndStreetName, authorisedConsignee)
          )
        )
        .verifying(
          regexp(
            inputRegex,
            "consigneeAddress.error.invalid",
            Seq(Address.Constants.Fields.buildingAndStreetName, authorisedConsignee)
          )
        ),
      "city" -> text("consigneeAddress.error.required", args = Seq(Address.Constants.Fields.city, authorisedConsignee))
        .verifying(
          maxLength(cityLength, "consigneeAddress.error.max_length", args = Seq(Address.Constants.Fields.city, authorisedConsignee))
        )
        .verifying(
          minLength(1, "consigneeAddress.error.empty", Seq(Address.Constants.Fields.city, authorisedConsignee))
        )
        .verifying(
          regexp(
            inputRegex,
            "consigneeAddress.error.invalid",
            Seq("city", authorisedConsignee)
          )
        ),
      "postcode" -> text("consigneeAddress.error.postcode.required", args = Seq(authorisedConsignee))
        .verifying(maxLength(postCodeLength, "consigneeAddress.error.postcode.length", args = Seq(authorisedConsignee)))
        .verifying(minLength(1, "consigneeAddress.error.empty", args = Seq(Address.Constants.Fields.postcode, authorisedConsignee)))
        .verifying(regexp("[\\sa-zA-Z0-9]*".r, "consigneeAddress.error.postcode.invalid", args = Seq(authorisedConsignee)))
    )(Address.apply)(Address.unapply)
  )
}
