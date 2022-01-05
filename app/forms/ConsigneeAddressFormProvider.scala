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
import models.StringFieldRegex.stringFieldRegexAsterisk
import play.api.data.Form
import play.api.data.Forms._
import javax.inject.Inject

class ConsigneeAddressFormProvider @Inject() extends Mappings {

  def apply(authorisedConsignee: String): Form[Address] = Form(
    mapping(
      "buildingAndStreet" -> text("consigneeAddress.error.required", Seq(Address.Constants.Fields.buildingAndStreetName, authorisedConsignee))
        .verifying(
          StopOnFirstFail[String](
            maxLength(Address.Constants.buildingAndStreetLength,
                      "consigneeAddress.error.length",
                      Seq(Address.Constants.Fields.buildingAndStreetName, authorisedConsignee)
            ),
            regexp(stringFieldRegexAsterisk, "consigneeAddress.error.invalid", Seq(Address.Constants.Fields.buildingAndStreetName, authorisedConsignee))
          )
        ),
      "city" -> text("consigneeAddress.error.required", args = Seq(Address.Constants.Fields.city, authorisedConsignee))
        .verifying(
          StopOnFirstFail[String](
            maxLength(Address.Constants.cityLength, "consigneeAddress.error.length", args = Seq(Address.Constants.Fields.city, authorisedConsignee)),
            regexp(stringFieldRegexAsterisk, "consigneeAddress.error.invalid", Seq(Address.Constants.Fields.city, authorisedConsignee))
          )
        ),
      "postcode" -> text("consigneeAddress.error.postcode.required", args = Seq(authorisedConsignee))
        .verifying(
          StopOnFirstFail[String](
            maxLength(Address.Constants.postcodeLength, "consigneeAddress.error.postcode.length", args = Seq(authorisedConsignee)),
            regexp(Address.Constants.postCodeRegex, "consigneeAddress.error.postcode.invalid", args = Seq(authorisedConsignee)),
            regexp(Address.Constants.postCodeFormatRegex, "consigneeAddress.error.postcode.invalidFormat", args = Seq(authorisedConsignee))
          )
        )
    )(Address.apply)(Address.unapply)
  )
}
