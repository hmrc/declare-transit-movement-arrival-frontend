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

package utils

import controllers.events.transhipments.routes.{ConfirmRemoveContainerController, ContainerNumberController}
import models.domain.ContainerDomain
import models.{Index, Mode, UserAnswers}
import pages.events.transhipments.ContainerNumberPage
import uk.gov.hmrc.viewmodels.SummaryList.Row
import uk.gov.hmrc.viewmodels._

class AddContainerHelper(userAnswers: UserAnswers, mode: Mode) extends SummaryListRowHelper(userAnswers) {

  def containerRow(eventIndex: Index, containerIndex: Index): Option[Row] = getAnswerAndBuildRemovableRow[ContainerDomain](
    page = ContainerNumberPage(eventIndex, containerIndex),
    formatAnswer = container => lit"${container.containerNumber}",
    id = s"container-${containerIndex.display}",
    changeCall = ContainerNumberController.onPageLoad(mrn, eventIndex, containerIndex, mode),
    removeCall = ConfirmRemoveContainerController.onPageLoad(mrn, eventIndex, containerIndex, mode)
  )
}

object AddContainerHelper {
  def apply(userAnswers: UserAnswers, mode: Mode): AddContainerHelper = new AddContainerHelper(userAnswers, mode)
}
