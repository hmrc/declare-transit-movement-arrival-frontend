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
import models.CustomsOfficeList

import javax.inject.Inject
import play.api.data.Form
import play.api.i18n.Messages
import models.reference.CustomsOffice

class CustomsOfficeFormProvider @Inject() extends Mappings {

  def apply(
    subPlace: String,
    customsOffices: CustomsOfficeList
  )(implicit messages: Messages): Form[CustomsOffice] =
    Form(
      "value" -> text("customsOffice.error.required", Seq(subPlace))
        .verifying(messages("customsOffice.error.required", subPlace), value => customsOffices.getAll.exists(_.id == value))
        .transform[CustomsOffice](value => customsOffices.getAll.find(_.id == value).get, _.id)
    )
}

class SimplifiedCustomsOfficeFormProvider @Inject() extends Mappings {

  def apply(
    consigneeName: String,
    customsOffices: CustomsOfficeList
  )(implicit messages: Messages): Form[CustomsOffice] =
    Form(
      "value" -> text("customsOffice.simplified.error.required", Seq(consigneeName))
        .verifying(messages("customsOffice.simplified.error.required", consigneeName), value => customsOffices.getAll.exists(_.id == value))
        .transform[CustomsOffice](value => customsOffices.getAll.find(_.id == value).get, _.id)
    )
}
