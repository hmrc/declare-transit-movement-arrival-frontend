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

import derivable.DeriveNumberOfSeals
import models.{Index, Mode, UserAnswers}
import uk.gov.hmrc.viewmodels.NunjucksSupport
import utils.CheckEventAnswersHelper

object SealSection extends NunjucksSupport {

  def apply(userAnswers: UserAnswers, mode: Mode, eventIndex: Index): Section = {

    val helper: CheckEventAnswersHelper = new CheckEventAnswersHelper(userAnswers, mode)

    val numberOfSeals    = userAnswers.get(DeriveNumberOfSeals(eventIndex)).getOrElse(0)
    val listOfSealsIndex = List.range(0, numberOfSeals).map(Index(_))
    val seals = listOfSealsIndex.flatMap {
      index =>
        helper.sealIdentity(eventIndex, index)
    }

    Section(msg"addSeal.sealList.heading", (helper.haveSealsChanged(eventIndex) ++ seals).toSeq)
  }
}
