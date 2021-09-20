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
import controllers.events.routes._
import controllers.events.transhipments.routes._
import models.domain.ContainerDomain
import models.{CheckMode, TranshipmentType}
import pages.events._
import pages.events.transhipments._
import uk.gov.hmrc.viewmodels.SummaryList.{Action, Key, Row, Value}
import uk.gov.hmrc.viewmodels.Text.{Literal, Message}

class CheckEventAnswersHelperSpec extends SpecBase {

  // format: off

  "CheckEventAnswersHelper" - {

    ".isTranshipment" - {

      "must return None" - {
        "when IsTranshipmentPage undefined" in {

          val helper = new CheckEventAnswersHelper(emptyUserAnswers)
          helper.isTranshipment(eventIndex) mustBe None
        }
      }

      "must return Some(Row)" - {
        "when IsTranshipmentPage defined" in {

          val answers = emptyUserAnswers
            .set(IsTranshipmentPage(eventIndex), true).success.value

          val helper = new CheckEventAnswersHelper(answers)
          helper.isTranshipment(eventIndex) mustBe Some(Row(
            key = Key(
              content = Message("isTranshipment.checkYourAnswersLabel"),
              classes = Seq("govuk-!-width-one-half")
            ),
            value = Value(Message("site.yes")),
            actions = List(
              Action(
                content            = Message("site.edit"),
                href               = IsTranshipmentController.onPageLoad(mrn, eventIndex, CheckMode).url,
                visuallyHiddenText = Some(Message("isTranshipment.change.hidden")),
                attributes         = Map("id" -> s"change-is-transhipment-${eventIndex.display}")
              )
            )
          ))
        }
      }
    }

    ".transhipmentType" - {

      val transhipmentType = TranshipmentType.DifferentContainer

      "must return None" - {
        "when TranshipmentTypePage undefined" in {

          val helper = new CheckEventAnswersHelper(emptyUserAnswers)
          helper.transhipmentType(eventIndex) mustBe None
        }
      }

      "must return Some(Row)" - {
        "when TranshipmentTypePage defined" in {

          val answers = emptyUserAnswers
            .set(TranshipmentTypePage(eventIndex), transhipmentType).success.value

          val helper = new CheckEventAnswersHelper(answers)
          helper.transhipmentType(eventIndex) mustBe Some(Row(
            key = Key(
              content = Message("transhipmentType.checkYourAnswersLabel"),
              classes = Seq("govuk-!-width-one-half")
            ),
            value = Value(Message(s"transhipmentType.checkYourAnswers.$transhipmentType")),
            actions = List(
              Action(
                content            = Message("site.edit"),
                href               = TranshipmentTypeController.onPageLoad(mrn, eventIndex, CheckMode).url,
                visuallyHiddenText = Some(Message("transhipmentType.change.hidden")),
                attributes         = Map("id" -> s"transhipment-type-${eventIndex.display}")
              )
            )
          ))
        }
      }
    }

    ".containerNumber" - {

      val containerDomain = ContainerDomain("NUMBER")

      "must return None" - {
        "when ContainerNumberPage undefined" in {

          val helper = new CheckEventAnswersHelper(emptyUserAnswers)
          helper.containerNumber(eventIndex, containerIndex) mustBe None
        }
      }

      "must return Some(Row)" - {
        "when ContainerNumberPage defined" in {

          val answers = emptyUserAnswers
            .set(ContainerNumberPage(eventIndex, containerIndex), containerDomain).success.value

          val helper = new CheckEventAnswersHelper(answers)
          helper.containerNumber(eventIndex, containerIndex) mustBe Some(Row(
            key = Key(
              content = Message("addContainer.containerList.label", containerIndex.display),
              classes = Seq("govuk-!-width-one-half")
            ),
            value = Value(Literal(containerDomain.containerNumber)),
            actions = List(
              Action(
                content            = Message("site.edit"),
                href               = ContainerNumberController.onPageLoad(mrn, eventIndex, containerIndex, CheckMode).url,
                visuallyHiddenText = Some(Message("containerNumber.change.hidden", containerIndex.display)),
                attributes         = Map("id" -> s"change-container-${containerIndex.display}")
              )
            )
          ))
        }
      }
    }

  }

  // format: on

}
