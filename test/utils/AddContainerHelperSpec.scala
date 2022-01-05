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

import base.SpecBase
import controllers.events.transhipments.routes.{ConfirmRemoveContainerController, ContainerNumberController}
import models.domain.ContainerDomain
import models.{CheckMode, Mode}
import pages.events.transhipments.ContainerNumberPage
import uk.gov.hmrc.viewmodels.SummaryList.{Action, Key, Row, Value}
import uk.gov.hmrc.viewmodels.Text.{Literal, Message}

class AddContainerHelperSpec extends SpecBase {

  val mode: Mode = CheckMode

  "AddContainerHelper" - {

    val container = ContainerDomain("NUMBER")

    ".containerRow" - {

      "must return None" - {
        "when ContainerNumberPage undefined" in {

          val helper = new AddContainerHelper(emptyUserAnswers, mode)
          helper.containerRow(eventIndex, containerIndex) mustBe None
        }
      }

      "must return Some(Row)" - {
        "when ContainerNumberPage defined" in {

          val answers = emptyUserAnswers
            .set(ContainerNumberPage(eventIndex, containerIndex), container)
            .success
            .value

          val helper = new AddContainerHelper(answers, mode)
          helper.containerRow(eventIndex, containerIndex) mustBe Some(
            Row(
              key = Key(
                content = Literal(container.containerNumber),
                classes = Nil
              ),
              value = Value(Literal("")),
              actions = List(
                Action(
                  content = Message("site.edit"),
                  href = ContainerNumberController.onPageLoad(mrn, eventIndex, containerIndex, mode).url,
                  visuallyHiddenText = Some(Literal(container.containerNumber)),
                  attributes = Map("id" -> s"change-container-${containerIndex.display}")
                ),
                Action(
                  content = Message("site.delete"),
                  href = ConfirmRemoveContainerController.onPageLoad(mrn, eventIndex, containerIndex, mode).url,
                  visuallyHiddenText = Some(Literal(container.containerNumber)),
                  attributes = Map("id" -> s"remove-container-${containerIndex.display}")
                )
              )
            )
          )
        }
      }
    }
  }

}
