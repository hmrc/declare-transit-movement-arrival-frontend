/*
 * Copyright 2021 HM Revenue & Customs
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
import controllers.events.seals.routes._
import models.CheckMode
import models.domain.SealDomain
import pages.events.seals.SealIdentityPage
import uk.gov.hmrc.viewmodels.SummaryList.{Action, Key, Row, Value}
import uk.gov.hmrc.viewmodels.Text.{Literal, Message}

class AddSealHelperSpec extends SpecBase {

  // format: off

  "AddSealHelper" - {

    val seal = SealDomain("NUMBER")

    ".sealRow" - {

      "must return None" - {
        "when SealIdentityPage undefined" in {

          val helper = new AddSealHelper(emptyUserAnswers)
          helper.sealRow(eventIndex, sealIndex, CheckMode) mustBe None
        }
      }

      "must return Some(Row)" - {
        "when SealIdentityPage defined" in {

          val answers = emptyUserAnswers
            .set(SealIdentityPage(eventIndex, sealIndex), seal).success.value

          val helper = new AddSealHelper(answers)
          helper.sealRow(eventIndex, sealIndex, CheckMode) mustBe Some(
            Row(
              key = Key(
                content = Message("addSeal.sealList.label", sealIndex.display),
                classes = Nil
              ),
              value = Value(Literal(seal.numberOrMark)),
              actions = List(
                Action(
                  content            = Message("site.edit"),
                  href               = SealIdentityController.onPageLoad(mrn, eventIndex, sealIndex, CheckMode).url,
                  visuallyHiddenText = Some(Message("addSeal.sealList.change.hidden", seal.numberOrMark)),
                  attributes         = Map("id" -> s"change-seal-${sealIndex.display}")
                ),
                Action(
                  content            = Message("site.delete"),
                  href               = ConfirmRemoveSealController.onPageLoad(mrn, eventIndex, sealIndex, CheckMode).url,
                  visuallyHiddenText = Some(Message("addSeal.sealList.delete.hidden", seal.numberOrMark)),
                  attributes         = Map("id" -> s"remove-seal-${sealIndex.display}")
                )
              )
            ))
        }
      }
    }
  }

  // format: on

}
