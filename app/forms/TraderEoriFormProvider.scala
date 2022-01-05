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
import models.domain.TraderDomain.{eoriLengthRegex, eoriRegex, eoriUkXiRegex}
import play.api.data.Form
import javax.inject.Inject

class TraderEoriFormProvider @Inject() extends Mappings {

  def apply(traderName: String): Form[String] =
    Form(
      "value" -> text("traderEori.error.required", Seq(traderName))
        .verifying(
          StopOnFirstFail[String](
            regexp(eoriLengthRegex.r, "traderEori.error.length", Seq(traderName)),
            regexp(eoriRegex.r, "traderEori.error.invalid", Seq(traderName)),
            regexp(eoriUkXiRegex.r, "traderEori.error.format", Seq(traderName))
          )
        )
    )
}
