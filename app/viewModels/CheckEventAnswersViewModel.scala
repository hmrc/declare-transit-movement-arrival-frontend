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

package viewModels

import models.{Index, Mode, UserAnswers}
import pages.events.IsTranshipmentPage
import play.api.i18n.Messages
import play.api.libs.json.{Json, OWrites}
import utils.CheckYourAnswersHelper
import viewModels.sections.{EventInfoSection, EventTypeSection, SealSection, Section}

case class CheckEventAnswersViewModel(sections: Seq[Section])

object CheckEventAnswersViewModel {

  def apply(userAnswers: UserAnswers, eventIndex: Index, mode: Mode): CheckEventAnswersViewModel = {
    val helper = new CheckYourAnswersHelper(userAnswers)

    val isTranshipment: Boolean = userAnswers.get(IsTranshipmentPage(eventIndex)).getOrElse(false)

    val eventInfoSection: Section = EventInfoSection(userAnswers, eventIndex, isTranshipment)

    val eventTypeSection: Seq[Section] = EventTypeSection(userAnswers, eventIndex, isTranshipment)

    val sealSection: Section = SealSection(userAnswers, eventIndex)

    CheckEventAnswersViewModel(
      Seq(eventInfoSection) ++
        eventTypeSection :+
        sealSection
    )
  }

  implicit def writes(implicit messages: Messages): OWrites[CheckEventAnswersViewModel] = Json.writes[CheckEventAnswersViewModel]
}
