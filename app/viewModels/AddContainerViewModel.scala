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

import derivable.DeriveNumberOfContainers
import models.{Index, Mode, UserAnswers}
import play.api.i18n.Messages
import play.api.libs.json.{Json, OWrites}
import uk.gov.hmrc.viewmodels.Text.Message
import uk.gov.hmrc.viewmodels._
import utils.AddContainerHelper
import viewModels.sections.Section

case class AddContainerViewModel private (
  pageTitle: Message,
  containers: Option[Section]
)

object AddContainerViewModel {

  def apply(eventIndex: Index, userAnswers: UserAnswers, mode: Mode): AddContainerViewModel = {
    val containerCount = userAnswers.get(DeriveNumberOfContainers(eventIndex)).getOrElse(0)

    val pageTitle: Message = if (containerCount == 1) {
      msg"addContainer.title.singular".withArgs(containerCount)
    } else {
      msg"addContainer.title.plural".withArgs(containerCount)
    }

    val addContainerHelper = AddContainerHelper(userAnswers, mode)
    val containers: Option[Section] = userAnswers
      .get(DeriveNumberOfContainers(eventIndex))
      .map {
        containerCount =>
          val listOfContainerIndex = List.range(0, containerCount).map(Index(_))

          val rows = listOfContainerIndex.flatMap {
            index =>
              addContainerHelper.containerRow(eventIndex, index)
          }

          Section(rows)
      }

    new AddContainerViewModel(pageTitle, containers)
  }

  implicit def writes(implicit message: Messages): OWrites[AddContainerViewModel] =
    Json.writes[AddContainerViewModel]
}
