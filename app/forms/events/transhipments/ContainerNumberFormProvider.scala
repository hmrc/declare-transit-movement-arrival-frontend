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

package forms.events.transhipments

import forms.StopOnFirstFail
import forms.mappings.Mappings
import javax.inject.Inject
import models.Index
import models.StringFieldRegex.stringFieldRegex
import models.domain.ContainerDomain
import models.messages.Transhipment
import play.api.data.Form

class ContainerNumberFormProvider @Inject() extends Mappings {

  def apply(index: Index, declaredContainers: Seq[ContainerDomain] = Seq.empty[ContainerDomain]): Form[String] =
    Form(
      "value" -> text("containerNumber.error.required")
        .verifying(
          StopOnFirstFail[String](
            maxLength(Transhipment.Constants.containerLength, "containerNumber.error.length"),
            regexp(stringFieldRegex, "containerNumber.error.invalid", Seq.empty),
            doesNotExistIn(declaredContainers, index, "containerNumber.error.duplicate")
          )
        )
    )
}
