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

import controllers.events.{routes => eventRoutes}
import models.{Index, NormalMode, UserAnswers}
import pages.events._
import uk.gov.hmrc.viewmodels.SummaryList.{Action, Key, Row, Value}
import uk.gov.hmrc.viewmodels._

class AddEventsHelper(userAnswers: UserAnswers) extends SummaryListRowHelper(userAnswers) {

  def listOfEvent(eventIndex: Index): Option[Row] =
    placeOfEvent(eventIndex).map {
      answer =>
        Row(
          key = Key(msg"addEvent.event.label".withArgs(eventIndex.display)),
          value = Value(lit"$answer"),
          actions = List(
            Action(
              content = msg"site.edit",
              href = eventRoutes.CheckEventAnswersController.onPageLoad(mrn, eventIndex).url,
              visuallyHiddenText = Some(msg"addEvent.change.hidden".withArgs(eventIndex.display, answer)),
              attributes = Map("id" -> s"change-event-${eventIndex.display}")
            ),
            Action(
              content = msg"site.delete",
              href = eventRoutes.ConfirmRemoveEventController.onPageLoad(mrn, eventIndex, NormalMode).url,
              visuallyHiddenText = Some(msg"addEvent.remove.hidden".withArgs(eventIndex.display, answer)),
              attributes = Map("id" -> s"remove-event-${eventIndex.display}")
            )
          )
        )
    }

  def cyaListOfEvent(eventIndex: Index): Option[Row] =
    placeOfEvent(eventIndex).map {
      answer =>
        Row(
          // TODO: Move hard coded interpretation of eventIndex to an Index Model
          key = Key(msg"addEvent.event.label".withArgs(eventIndex.display), classes = Seq("govuk-!-width-one-half")),
          value = Value(lit"$answer"),
          actions = List(
            Action(
              content = msg"site.edit",
              href = eventRoutes.CheckEventAnswersController.onPageLoad(mrn, eventIndex).url,
              visuallyHiddenText = Some(msg"addEvent.change.hidden".withArgs(eventIndex.display, answer))
            )
          )
        )
    }

  private def placeOfEvent(eventIndex: Index): Option[String] =
    userAnswers.get(EventPlacePage(eventIndex)) orElse userAnswers.get(EventCountryPage(eventIndex)).map(_.code)
}
