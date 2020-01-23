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
import models.messages.Container
import models.{Index, Mode, UserAnswers}
import pages.events.transhipments.ContainerNumberPage
import uk.gov.hmrc.viewmodels.SummaryList.{Action, Key, Row, Value}
import uk.gov.hmrc.viewmodels._

class AddContainerHelper(userAnswers: UserAnswers) {

  def containerRow(eventIndex: Int, containerIndex: Index, mode: Mode): Option[Row] =
    userAnswers.get(ContainerNumberPage(eventIndex, containerIndex.position)).map {
      case Container(answer) =>
        //val containerIndex = Index(containerIndex)

        Row(
          key   = Key(msg"addContainer.containerList.label".withArgs(containerIndex.display), classes = Seq("govuk-!-width-one-half")), // TODO: Move harded coded interpretation of index to an Index Model
          value = Value(lit"$answer"),
          actions = List(
            Action(
              content            = msg"site.edit",
              href               = ContainerNumberController.onPageLoad(userAnswers.id, eventIndex, containerIndex.position, mode).url,
              visuallyHiddenText = Some(msg"addContainer.containerList.change".withArgs(answer)) // TODO: Prefix in message file for is hard coded, should be the same as: site.edit.hidden
            ),
            Action(
              content            = msg"site.delete",
              href               = ConfirmRemoveContainerController.onPageLoad(userAnswers.id, eventIndex, containerIndex, mode).url,
              visuallyHiddenText = Some(msg"addContainer.containerList.delete".withArgs(answer)) // TODO: Prefix in message file for is hard coded, should be the same as: site.delete.hidden
            )
          )
        )
    }
}

object AddContainerHelper {
  def apply(userAnswers: UserAnswers): AddContainerHelper = new AddContainerHelper(userAnswers)
}
