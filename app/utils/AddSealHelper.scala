/*
 * Copyright 2023 HM Revenue & Customs
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

package utils

import controllers.events.seals.routes._
import models.domain.SealDomain
import models.{Index, Mode, UserAnswers}
import pages.events.seals._
import uk.gov.hmrc.viewmodels.SummaryList.Row
import uk.gov.hmrc.viewmodels._

class AddSealHelper(userAnswers: UserAnswers, mode: Mode) extends SummaryListRowHelper(userAnswers) {

  def sealRow(eventIndex: Index, sealIndex: Index): Option[Row] = getAnswerAndBuildRemovableRow[SealDomain](
    page = SealIdentityPage(eventIndex, sealIndex),
    formatAnswer = seal => lit"${seal.numberOrMark}",
    id = s"seal-${sealIndex.display}",
    changeCall = SealIdentityController.onPageLoad(mrn, eventIndex, sealIndex, mode),
    removeCall = ConfirmRemoveSealController.onPageLoad(mrn, eventIndex, sealIndex, mode)
  )
}

object AddSealHelper {
  def apply(userAnswers: UserAnswers, mode: Mode): AddSealHelper = new AddSealHelper(userAnswers, mode)
}
