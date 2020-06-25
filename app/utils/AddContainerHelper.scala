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

package utils

import controllers.events.transhipments.routes.ContainerNumberController
import controllers.events.transhipments.routes.ConfirmRemoveContainerController
import models.domain.ContainerDomain
import models.{Index, Mode, UserAnswers}
import pages.events.transhipments.ContainerNumberPage
import uk.gov.hmrc.viewmodels.SummaryList.{Action, Key, Row, Value}
import uk.gov.hmrc.viewmodels._

class AddContainerHelper(userAnswers: UserAnswers) {

  def containerRow(eventIndex: Index, containerIndex: Index, mode: Mode): Option[Row] =
    userAnswers.get(ContainerNumberPage(eventIndex, containerIndex)).map {
      case ContainerDomain(answer) =>
        Row(
          key   = Key(msg"addContainer.containerList.label".withArgs(containerIndex.display)),
          value = Value(lit"$answer"),
          actions = List(
            Action(
              content            = msg"site.edit",
              href               = ContainerNumberController.onPageLoad(userAnswers.id, eventIndex, containerIndex, mode).url,
              visuallyHiddenText = Some(msg"addContainer.containerList.change.hidden".withArgs(answer)),
              attributes         = Map("id" -> s"""change-container-${containerIndex.display}""")
            ),
            Action(
              content            = msg"site.delete",
              href               = ConfirmRemoveContainerController.onPageLoad(userAnswers.id, eventIndex, containerIndex, mode).url,
              visuallyHiddenText = Some(msg"addContainer.containerList.delete.hidden".withArgs(answer)),
              attributes         = Map("id" -> s"""remove-container-${containerIndex.display}""")
            )
          )
        )
    }
}

object AddContainerHelper {
  def apply(userAnswers: UserAnswers): AddContainerHelper = new AddContainerHelper(userAnswers)
}
