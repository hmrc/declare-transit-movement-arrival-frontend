/*
 * Copyright 2019 HM Revenue & Customs
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

import controllers.events.{routes => eventRoutes}
import models.{MovementReferenceNumber, UserAnswers}
import pages.events._
import uk.gov.hmrc.viewmodels.SummaryList.{Action, Key, Row, Value}
import uk.gov.hmrc.viewmodels._

class AddEventsHelper(userAnswers: UserAnswers) {

  def listOfEvent(index: Int): Option[Row] =
    placeOfEvent(index).map {
      answer =>
        Row(
          key   = Key(msg"addEvent.event.label".withArgs(index + 1), classes = Seq("govuk-!-width-one-half")), // TODO: Move harded coded interpretation of index to an Index Model
          value = Value(lit"$answer"),
          actions = List(
            Action(
              content            = msg"site.edit",
              href               = eventRoutes.CheckEventAnswersController.onPageLoad(mrn, index).url,
              visuallyHiddenText = Some(msg"addEvent.checkYourAnswersLabel.change".withArgs(index, answer)) // TODO: Prefix in message file for is hard coded, should be the same as: site.edit.hidden
            ),
            Action(
              content            = msg"site.delete",
              href               = "#",
              visuallyHiddenText = Some(msg"addEvent.checkYourAnswersLabel.delete".withArgs(index, answer)) // TODO: Prefix in message file for is hard coded, should be the same as: site.delete.hidden
            )
          )
        )
    }

  def cyaListOfEvent(index: Int): Option[Row] =
    placeOfEvent(index).map {
      answer =>
        Row(
          key   = Key(msg"addEvent.event.label".withArgs(index + 1), classes = Seq("govuk-!-width-one-half")), // TODO: Move harded coded interpretation of index to an Index Model
          value = Value(lit"$answer"),
          actions = List(
            Action(
              content            = msg"site.edit",
              href               = eventRoutes.CheckEventAnswersController.onPageLoad(mrn, index).url,
              visuallyHiddenText = Some(msg"addEvent.checkYourAnswersLabel.change".withArgs(index, answer)) // TODO: Prefix in message file for is hard coded, should be the same as: site.edit.hidden
            )
          )
        )
    }

  private def placeOfEvent(index: Int): Option[String] =
    userAnswers.get(EventPlacePage(index)) match {
      case Some(answer) => Some(answer)
      case _            => userAnswers.get(EventCountryPage(index))
    }

  private def mrn: MovementReferenceNumber = userAnswers.id
}
