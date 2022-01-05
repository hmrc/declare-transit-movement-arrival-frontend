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
import controllers.events.routes._
import models.reference.CountryCode
import models.{CheckMode, Mode}
import pages.events.{EventCountryPage, EventPlacePage}
import uk.gov.hmrc.viewmodels.SummaryList.{Action, Key, Row, Value}
import uk.gov.hmrc.viewmodels.Text.{Literal, Message}

class AddEventsHelperSpec extends SpecBase {

  val mode: Mode = CheckMode

  "AddEventsHelper" - {

    ".listOfEvent" - {

      "must return None" - {
        "when EventPlacePage and EventCountryPage undefined" in {

          val helper = new AddEventsHelper(emptyUserAnswers, mode)
          helper.listOfEvent(eventIndex) mustBe None
        }
      }

      "must return Some(Row)" - {
        "when EventPlacePage defined" in {

          val place = "PLACE"

          val answers = emptyUserAnswers
            .set(EventPlacePage(eventIndex), place)
            .success
            .value

          val helper = new AddEventsHelper(answers, mode)
          helper.listOfEvent(eventIndex) mustBe Some(
            Row(
              key = Key(
                content = Literal(place),
                classes = Nil
              ),
              value = Value(Literal("")),
              actions = List(
                Action(
                  content = Message("site.edit"),
                  href = CheckEventAnswersController.onPageLoad(mrn, eventIndex).url,
                  visuallyHiddenText = Some(Literal(place)),
                  attributes = Map("id" -> s"change-event-${eventIndex.display}")
                ),
                Action(
                  content = Message("site.delete"),
                  href = ConfirmRemoveEventController.onPageLoad(mrn, eventIndex, mode).url,
                  visuallyHiddenText = Some(Literal(place)),
                  attributes = Map("id" -> s"remove-event-${eventIndex.display}")
                )
              )
            )
          )
        }

        "when EventCountryPage defined" in {

          val countryCode = CountryCode("CODE")

          val answers = emptyUserAnswers
            .set(EventCountryPage(eventIndex), countryCode)
            .success
            .value

          val helper = new AddEventsHelper(answers, mode)
          helper.listOfEvent(eventIndex) mustBe Some(
            Row(
              key = Key(
                content = Literal(countryCode.code),
                classes = Nil
              ),
              value = Value(Literal("")),
              actions = List(
                Action(
                  content = Message("site.edit"),
                  href = CheckEventAnswersController.onPageLoad(mrn, eventIndex).url,
                  visuallyHiddenText = Some(Literal(countryCode.code)),
                  attributes = Map("id" -> s"change-event-${eventIndex.display}")
                ),
                Action(
                  content = Message("site.delete"),
                  href = ConfirmRemoveEventController.onPageLoad(mrn, eventIndex, mode).url,
                  visuallyHiddenText = Some(Literal(countryCode.code)),
                  attributes = Map("id" -> s"remove-event-${eventIndex.display}")
                )
              )
            )
          )
        }
      }
    }

    ".cyaListOfEvent" - {

      "must return None" - {
        "when EventPlacePage and EventCountryPage undefined" in {

          val helper = new AddEventsHelper(emptyUserAnswers, mode)
          helper.cyaListOfEvent(eventIndex) mustBe None
        }
      }

      "must return Some(Row)" - {
        "when EventPlacePage defined" in {

          val place = "PLACE"

          val answers = emptyUserAnswers
            .set(EventPlacePage(eventIndex), place)
            .success
            .value

          val helper = new AddEventsHelper(answers, mode)
          helper.cyaListOfEvent(eventIndex) mustBe Some(
            Row(
              key = Key(
                content = Message("addEvent.event.label", eventIndex.display),
                classes = Seq("govuk-!-width-one-half")
              ),
              value = Value(Literal(place)),
              actions = List(
                Action(
                  content = Message("site.edit"),
                  href = CheckEventAnswersController.onPageLoad(mrn, eventIndex).url,
                  visuallyHiddenText = Some(Message("addEvent.change.hidden", eventIndex.display, place))
                )
              )
            )
          )
        }

        "when EventCountryPage defined" in {

          val countryCode = CountryCode("CODE")

          val answers = emptyUserAnswers
            .set(EventCountryPage(eventIndex), countryCode)
            .success
            .value

          val helper = new AddEventsHelper(answers, mode)
          helper.cyaListOfEvent(eventIndex) mustBe Some(
            Row(
              key = Key(
                content = Message("addEvent.event.label", eventIndex.display),
                classes = Seq("govuk-!-width-one-half")
              ),
              value = Value(Literal(countryCode.code)),
              actions = List(
                Action(
                  content = Message("site.edit"),
                  href = CheckEventAnswersController.onPageLoad(mrn, eventIndex).url,
                  visuallyHiddenText = Some(Message("addEvent.change.hidden", eventIndex.display, countryCode.code))
                )
              )
            )
          )
        }
      }
    }
  }

}
