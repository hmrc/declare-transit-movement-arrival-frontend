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

import controllers.events.seals.routes._
import models.{CheckMode, Mode, UserAnswers}
import pages.events.seals._
import uk.gov.hmrc.viewmodels.SummaryList.{Action, Key, Row, Value}
import uk.gov.hmrc.viewmodels._

class AddSealHelper(userAnswers: UserAnswers) {

  def sealRow(eventIndex: Int, sealIndex: Int, mode: Mode): Option[Row] =
    userAnswers.get(SealIdentityPage(eventIndex, sealIndex)).map {
      answer =>
        Row(
          key   = Key(msg"addSeal.sealList.label".withArgs(sealIndex + 1), classes = Seq("govuk-!-width-one-half")), // TODO: Move harded coded interpretation of index to an Index Model
          value = Value(lit"$answer"),
          actions = List(
            Action(
              content            = msg"site.edit",
              href               = SealIdentityController.onPageLoad(userAnswers.id, eventIndex, sealIndex, mode).url,
              visuallyHiddenText = Some(msg"addSeal.sealList.change".withArgs(answer)) // TODO: Prefix in message file for is hard coded, should be the same as: site.edit.hidden
            ),
            Action(
              content            = msg"site.delete",
              href               = ConfirmRemoveSealController.onPageLoad(userAnswers.id, eventIndex, sealIndex, mode).url,
              visuallyHiddenText = Some(msg"addSeal.sealList.delete".withArgs(answer)) // TODO: Prefix in message file for is hard coded, should be the same as: site.delete.hidden
            )
          )
        )
    }
}

object AddSealHelper {
  def apply(userAnswers: UserAnswers): AddSealHelper = new AddSealHelper(userAnswers)
}
