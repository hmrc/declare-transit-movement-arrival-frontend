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

package viewModels.sections

import models.{CountryList, Index, Mode, UserAnswers}
import utils.CheckEventAnswersHelper

object EventInfoSection {

  def apply(userAnswers: UserAnswers, mode: Mode, eventIndex: Index, isTranshipment: Boolean, codeList: CountryList): Section = {

    val helper = new CheckEventAnswersHelper(userAnswers, mode)

    Section(
      Seq(
        helper.eventCountry(eventIndex)(codeList),
        helper.eventPlace(eventIndex),
        helper.eventReported(eventIndex),
        if (isTranshipment) None else { helper.isTranshipment(eventIndex) },
        helper.incidentInformation(eventIndex)
      ).flatten
    )
  }

}
