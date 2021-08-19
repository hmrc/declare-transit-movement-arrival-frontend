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

import forms.mappings.Mappings

import javax.inject.Inject
import models.domain.TraderDomain.Constants.eoriLength
import models.domain.TraderDomain.{eoriLengthRegex, eoriRegex, eoriUkXiRegex}
import play.api.data.Form
import uk.gov.hmrc.play.mappers.StopOnFirstFail

class EoriNumberFormProvider @Inject() extends Mappings {

  def apply(consigneeName: String): Form[String] =
    Form(
      "value" -> text("eoriNumber.error.required", args = Seq(consigneeName))
        .verifying(
          StopOnFirstFail[String](
            regexp(eoriLengthRegex.r, "eoriNumber.error.length", Seq(consigneeName)),
            regexp(eoriRegex.r, "eoriNumber.error.invalid", Seq(consigneeName)),
            regexp(eoriUkXiRegex.r, "eoriNumber.error.format", Seq(consigneeName))
          )
        )
    )
}
