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

package viewModels

import models.{CountryList, Index, Mode, UserAnswers}
import pages.events.IsTranshipmentPage
import play.api.i18n.Messages
import play.api.libs.json.{Json, OWrites}
import viewModels.sections.{EventInfoSection, EventTypeSection, SealSection, Section}

case class CheckEventAnswersViewModel(sections: Seq[Section])

object CheckEventAnswersViewModel {

  def apply(userAnswers: UserAnswers, eventIndex: Index, mode: Mode, codeList: CountryList): CheckEventAnswersViewModel = {

    val isTranshipment: Boolean = userAnswers.get(IsTranshipmentPage(eventIndex)).getOrElse(false)

    val eventInfoSection: Section = EventInfoSection(userAnswers, mode, eventIndex, isTranshipment, codeList)

    val eventTypeSection: Seq[Section] = EventTypeSection(userAnswers, mode, eventIndex, isTranshipment, codeList)

    val sealSection: Section = SealSection(userAnswers, mode, eventIndex)

    CheckEventAnswersViewModel(
      Seq(eventInfoSection) ++
        eventTypeSection :+
        sealSection
    )
  }

  implicit def writes(implicit messages: Messages): OWrites[CheckEventAnswersViewModel] = Json.writes[CheckEventAnswersViewModel]
}
