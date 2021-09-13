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
import controllers.routes
import models.CheckMode
import models.reference.CustomsOffice
import pages.{ConsigneeNamePage, CustomsOfficePage, CustomsSubPlacePage, SimplifiedCustomsOfficePage}
import uk.gov.hmrc.viewmodels.SummaryList.{Action, Key, Row, Value}
import uk.gov.hmrc.viewmodels.Text.{Literal, Message}

class CheckYourAnswersHelperSpec extends SpecBase {

  // format off

  "CheckYourAnswersHelper" - {

    val location          = "LOCATION"
    val customsOfficeId   = "CUSTOMS OFFICE ID"
    val customsOfficeName = "CUSTOMS OFFICE NAME"

    ".simplifiedCustomsOffice" - {

      "must return None" - {

        "when SimplifiedCustomsOfficePage undefined" in {

          val checkYourAnswersHelper = new CheckYourAnswersHelper(emptyUserAnswers)
          checkYourAnswersHelper.simplifiedCustomsOffice mustBe None
        }

        "when SimplifiedCustomsOfficePage defined but CustomsSubPlacePage and ConsigneeNamePage empty" in {

          val answers = emptyUserAnswers
            .set(SimplifiedCustomsOfficePage, CustomsOffice("id", None, None))
            .success
            .value

          val checkYourAnswersHelper = new CheckYourAnswersHelper(answers)
          checkYourAnswersHelper.simplifiedCustomsOffice mustBe None
        }
      }

      "must return Some(Row)" - {

        "when customs office name undefined" - {

          "when SimplifiedCustomsOfficePage and CustomsSubPlacePage defined" in {

            val answers = emptyUserAnswers
              .set(SimplifiedCustomsOfficePage, CustomsOffice(customsOfficeId, None, None))
              .success
              .value
              .set(CustomsSubPlacePage, location)
              .success
              .value

            val checkYourAnswersHelper = new CheckYourAnswersHelper(answers)
            checkYourAnswersHelper.simplifiedCustomsOffice mustBe Some(Row(
              key = Key(
                content = Message("customsOffice.simplified.checkYourAnswersLabel", location),
                classes = Seq("govuk-!-width-one-half")
              ),
              value = Value(Literal(customsOfficeId)),
              actions = List(
                Action(
                  content            = Message("site.edit"),
                  href               = routes.SimplifiedCustomsOfficeController.onPageLoad(mrn, CheckMode).url,
                  visuallyHiddenText = Some(Message("customsOffice.simplified.change.hidden", location)),
                  attributes         = Map("id" -> s"""change-presentation-office""")
                )
              )
            ))
          }

          "when SimplifiedCustomsOfficePage and ConsigneeNamePage defined" in {

            val answers = emptyUserAnswers
              .set(SimplifiedCustomsOfficePage, CustomsOffice(customsOfficeId, None, None))
              .success
              .value
              .set(ConsigneeNamePage, location)
              .success
              .value

            val checkYourAnswersHelper = new CheckYourAnswersHelper(answers)
            checkYourAnswersHelper.simplifiedCustomsOffice mustBe Some(Row(
              key = Key(
                content = Message("customsOffice.simplified.checkYourAnswersLabel", location),
                classes = Seq("govuk-!-width-one-half")
              ),
              value = Value(Literal(customsOfficeId)),
              actions = List(
                Action(
                  content            = Message("site.edit"),
                  href               = routes.SimplifiedCustomsOfficeController.onPageLoad(mrn, CheckMode).url,
                  visuallyHiddenText = Some(Message("customsOffice.simplified.change.hidden", location)),
                  attributes         = Map("id" -> s"""change-presentation-office""")
                )
              )
            ))
          }
        }

        "when customs office name defined" - {

          "when SimplifiedCustomsOfficePage and CustomsSubPlacePage defined" in {

            val answers = emptyUserAnswers
              .set(SimplifiedCustomsOfficePage, CustomsOffice(customsOfficeId, Some(customsOfficeName), None))
              .success
              .value
              .set(CustomsSubPlacePage, location)
              .success
              .value

            val checkYourAnswersHelper = new CheckYourAnswersHelper(answers)
            checkYourAnswersHelper.simplifiedCustomsOffice mustBe Some(Row(
              key = Key(
                content = Message("customsOffice.simplified.checkYourAnswersLabel", location),
                classes = Seq("govuk-!-width-one-half")
              ),
              value = Value(Literal(s"$customsOfficeName ($customsOfficeId)")),
              actions = List(
                Action(
                  content            = Message("site.edit"),
                  href               = routes.SimplifiedCustomsOfficeController.onPageLoad(mrn, CheckMode).url,
                  visuallyHiddenText = Some(Message("customsOffice.simplified.change.hidden", location)),
                  attributes         = Map("id" -> s"""change-presentation-office""")
                )
              )
            ))
          }

          "when SimplifiedCustomsOfficePage and ConsigneeNamePage defined" in {

            val answers = emptyUserAnswers
              .set(SimplifiedCustomsOfficePage, CustomsOffice(customsOfficeId, Some(customsOfficeName), None))
              .success
              .value
              .set(ConsigneeNamePage, location)
              .success
              .value

            val checkYourAnswersHelper = new CheckYourAnswersHelper(answers)
            checkYourAnswersHelper.simplifiedCustomsOffice mustBe Some(Row(
              key = Key(
                content = Message("customsOffice.simplified.checkYourAnswersLabel", location),
                classes = Seq("govuk-!-width-one-half")
              ),
              value = Value(Literal(s"$customsOfficeName ($customsOfficeId)")),
              actions = List(
                Action(
                  content            = Message("site.edit"),
                  href               = routes.SimplifiedCustomsOfficeController.onPageLoad(mrn, CheckMode).url,
                  visuallyHiddenText = Some(Message("customsOffice.simplified.change.hidden", location)),
                  attributes         = Map("id" -> s"""change-presentation-office""")
                )
              )
            ))
          }
        }
      }
    }

    ".customsOffice" - {

      "must return None" - {

        "when CustomsOfficePage undefined" in {

          val checkYourAnswersHelper = new CheckYourAnswersHelper(emptyUserAnswers)
          checkYourAnswersHelper.customsOffice mustBe None
        }

        "when CustomsOfficePage defined but CustomsSubPlacePage and ConsigneeNamePage empty" in {

          val answers = emptyUserAnswers
            .set(CustomsOfficePage, CustomsOffice("id", None, None))
            .success
            .value

          val checkYourAnswersHelper = new CheckYourAnswersHelper(answers)
          checkYourAnswersHelper.customsOffice mustBe None
        }
      }

      "must return Some(Row)" - {

        "when customs office name undefined" - {

          "when CustomsOfficePage and CustomsSubPlacePage defined" in {

            val answers = emptyUserAnswers
              .set(CustomsOfficePage, CustomsOffice(customsOfficeId, None, None))
              .success
              .value
              .set(CustomsSubPlacePage, location)
              .success
              .value

            val checkYourAnswersHelper = new CheckYourAnswersHelper(answers)
            checkYourAnswersHelper.customsOffice mustBe Some(Row(
              key = Key(
                content = Message("customsOffice.checkYourAnswersLabel", location),
                classes = Seq("govuk-!-width-one-half")
              ),
              value = Value(Literal(customsOfficeId)),
              actions = List(
                Action(
                  content            = Message("site.edit"),
                  href               = routes.CustomsOfficeController.onPageLoad(mrn, CheckMode).url,
                  visuallyHiddenText = Some(Message("customsOffice.change.hidden", location)),
                  attributes         = Map("id" -> s"""change-presentation-office""")
                )
              )
            ))
          }

          "when CustomsOfficePage and ConsigneeNamePage defined" in {

            val answers = emptyUserAnswers
              .set(CustomsOfficePage, CustomsOffice(customsOfficeId, None, None))
              .success
              .value
              .set(ConsigneeNamePage, location)
              .success
              .value

            val checkYourAnswersHelper = new CheckYourAnswersHelper(answers)
            checkYourAnswersHelper.customsOffice mustBe Some(Row(
              key = Key(
                content = Message("customsOffice.checkYourAnswersLabel", location),
                classes = Seq("govuk-!-width-one-half")
              ),
              value = Value(Literal(customsOfficeId)),
              actions = List(
                Action(
                  content            = Message("site.edit"),
                  href               = routes.CustomsOfficeController.onPageLoad(mrn, CheckMode).url,
                  visuallyHiddenText = Some(Message("customsOffice.change.hidden", location)),
                  attributes         = Map("id" -> s"""change-presentation-office""")
                )
              )
            ))
          }
        }

        "when customs office name defined" - {

          "when CustomsOfficePage and CustomsSubPlacePage defined" in {

            val answers = emptyUserAnswers
              .set(CustomsOfficePage, CustomsOffice(customsOfficeId, Some(customsOfficeName), None))
              .success
              .value
              .set(CustomsSubPlacePage, location)
              .success
              .value

            val checkYourAnswersHelper = new CheckYourAnswersHelper(answers)
            checkYourAnswersHelper.customsOffice mustBe Some(Row(
              key = Key(
                content = Message("customsOffice.checkYourAnswersLabel", location),
                classes = Seq("govuk-!-width-one-half")
              ),
              value = Value(Literal(s"$customsOfficeName ($customsOfficeId)")),
              actions = List(
                Action(
                  content            = Message("site.edit"),
                  href               = routes.CustomsOfficeController.onPageLoad(mrn, CheckMode).url,
                  visuallyHiddenText = Some(Message("customsOffice.change.hidden", location)),
                  attributes         = Map("id" -> s"""change-presentation-office""")
                )
              )
            ))
          }

          "when CustomsOfficePage and ConsigneeNamePage defined" in {

            val answers = emptyUserAnswers
              .set(CustomsOfficePage, CustomsOffice(customsOfficeId, Some(customsOfficeName), None))
              .success
              .value
              .set(ConsigneeNamePage, location)
              .success
              .value

            val checkYourAnswersHelper = new CheckYourAnswersHelper(answers)
            checkYourAnswersHelper.customsOffice mustBe Some(Row(
              key = Key(
                content = Message("customsOffice.checkYourAnswersLabel", location),
                classes = Seq("govuk-!-width-one-half")
              ),
              value = Value(Literal(s"$customsOfficeName ($customsOfficeId)")),
              actions = List(
                Action(
                  content            = Message("site.edit"),
                  href               = routes.CustomsOfficeController.onPageLoad(mrn, CheckMode).url,
                  visuallyHiddenText = Some(Message("customsOffice.change.hidden", location)),
                  attributes         = Map("id" -> s"""change-presentation-office""")
                )
              )
            ))
          }
        }
      }
    }
  }

  // format on

}
