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

package viewModels.sections

import models.UserAnswers
import utils.CheckYourAnswersHelper

object EventInfoSection {

  def apply(userAnswers: UserAnswers, eventIndex: Int, isTranshipment: Boolean): Section = {

    val helper: CheckYourAnswersHelper = new CheckYourAnswersHelper(userAnswers)

    Section(
      Seq(
        helper.eventCountry(eventIndex),
        helper.eventPlace(eventIndex),
        helper.eventReported(eventIndex),
        if (isTranshipment) None else { helper.isTranshipment(eventIndex) },
        helper.incidentInformation(eventIndex)
      ).flatten)
  }

}
