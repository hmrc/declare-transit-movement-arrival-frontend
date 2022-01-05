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

import controllers.events.{routes => eventRoutes}
import models.{Index, Mode, UserAnswers}
import pages.events._
import uk.gov.hmrc.viewmodels.SummaryList.Row
import uk.gov.hmrc.viewmodels._

class AddEventsHelper(userAnswers: UserAnswers, mode: Mode) extends SummaryListRowHelper(userAnswers) {

  def listOfEvent(eventIndex: Index): Option[Row] =
    placeOfEvent(eventIndex).map {
      answer =>
        buildRemovableRow(
          label = lit"$answer",
          id = s"event-${eventIndex.display}",
          changeCall = eventRoutes.CheckEventAnswersController.onPageLoad(mrn, eventIndex),
          removeCall = eventRoutes.ConfirmRemoveEventController.onPageLoad(mrn, eventIndex, mode)
        )
    }

  // format: off
  def cyaListOfEvent(eventIndex: Index): Option[Row] =
    placeOfEvent(eventIndex) map {
      answer =>
        buildSimpleRow(
          prefix = "addEvent",
          label  = msg"addEvent.event.label".withArgs(eventIndex.display),
          answer = lit"$answer",
          id     = None,
          call   = eventRoutes.CheckEventAnswersController.onPageLoad(mrn, eventIndex),
          args   = eventIndex.display, answer
        )
    }
  // format: on

  private def placeOfEvent(eventIndex: Index): Option[String] =
    userAnswers.get(EventPlacePage(eventIndex)) orElse
      userAnswers.get(EventCountryPage(eventIndex)).map(_.code)
}
