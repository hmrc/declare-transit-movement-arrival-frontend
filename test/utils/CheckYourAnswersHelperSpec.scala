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
import controllers.routes
import models.reference.CustomsOffice
import models.{Address, CheckMode, Mode}
import pages._
import uk.gov.hmrc.viewmodels.Html
import uk.gov.hmrc.viewmodels.SummaryList.{Action, Key, Row, Value}
import uk.gov.hmrc.viewmodels.Text.{Literal, Message}

class CheckYourAnswersHelperSpec extends SpecBase {

  val mode: Mode = CheckMode

  "CheckYourAnswersHelper" - {

    val name              = "NAME"
    val eori              = "EORI"
    val location          = "LOCATION"
    val customsOfficeId   = "CUSTOMS OFFICE ID"
    val customsOfficeName = "CUSTOMS OFFICE NAME"
    val address           = Address("STREET", "CITY", "POSTCODE")

    ".eoriNumber" - {

      "must return None" - {

        "when ConsigneeNamePage undefined" in {

          val checkYourAnswersHelper = new CheckYourAnswersHelper(emptyUserAnswers, mode)
          checkYourAnswersHelper.eoriNumber mustBe None
        }

        "when ConsigneeEoriNumberPage undefined" in {

          val answers = emptyUserAnswers
            .set(ConsigneeNamePage, name)
            .success
            .value

          val checkYourAnswersHelper = new CheckYourAnswersHelper(answers, mode)
          checkYourAnswersHelper.eoriNumber mustBe None
        }
      }

      "must return Some(Row)" - {
        "when ConsigneeEoriNumberPage defined" in {

          val answers = emptyUserAnswers
            .set(ConsigneeNamePage, name)
            .success
            .value
            .set(ConsigneeEoriNumberPage, eori)
            .success
            .value

          val checkYourAnswersHelper = new CheckYourAnswersHelper(answers, mode)
          checkYourAnswersHelper.eoriNumber mustBe Some(
            Row(
              key = Key(
                content = Message("eoriNumber.checkYourAnswersLabel").withArgs(name),
                classes = Seq("govuk-!-width-one-half")
              ),
              value = Value(Literal(eori)),
              actions = List(
                Action(
                  content = Message("site.edit"),
                  href = routes.ConsigneeEoriNumberController.onPageLoad(mrn, mode).url,
                  visuallyHiddenText = Some(Message("eoriNumber.change.hidden").withArgs(name)),
                  attributes = Map("id" -> "change-eori-number")
                )
              )
            )
          )
        }
      }
    }

    ".consigneeName" - {

      "must return None" - {
        "when ConsigneeNamePage undefined" in {

          val checkYourAnswersHelper = new CheckYourAnswersHelper(emptyUserAnswers, mode)
          checkYourAnswersHelper.consigneeName mustBe None
        }
      }

      "must return Some(Row)" - {
        "when ConsigneeNamePage defined" in {

          val answers = emptyUserAnswers
            .set(ConsigneeNamePage, name)
            .success
            .value

          val checkYourAnswersHelper = new CheckYourAnswersHelper(answers, mode)
          checkYourAnswersHelper.consigneeName mustBe Some(
            Row(
              key = Key(
                content = Message("consigneeName.checkYourAnswersLabel"),
                classes = Seq("govuk-!-width-one-half")
              ),
              value = Value(Literal(name)),
              actions = List(
                Action(
                  content = Message("site.edit"),
                  href = routes.ConsigneeNameController.onPageLoad(mrn, mode).url,
                  visuallyHiddenText = Some(Message("consigneeName.change.hidden")),
                  attributes = Map("id" -> "change-consignee-name")
                )
              )
            )
          )
        }
      }
    }

    ".placeOfNotification" - {

      "must return None" - {
        "when PlaceOfNotificationPage undefined" in {

          val checkYourAnswersHelper = new CheckYourAnswersHelper(emptyUserAnswers, mode)
          checkYourAnswersHelper.placeOfNotification mustBe None
        }
      }

      "must return Some(Row)" - {
        "when PlaceOfNotificationPage defined" in {

          val answers = emptyUserAnswers
            .set(PlaceOfNotificationPage, location)
            .success
            .value

          val checkYourAnswersHelper = new CheckYourAnswersHelper(answers, mode)
          checkYourAnswersHelper.placeOfNotification mustBe Some(
            Row(
              key = Key(
                content = Message("placeOfNotification.checkYourAnswersLabel"),
                classes = Seq("govuk-!-width-one-half")
              ),
              value = Value(Literal(location)),
              actions = List(
                Action(
                  content = Message("site.edit"),
                  href = routes.PlaceOfNotificationController.onPageLoad(mrn, mode).url,
                  visuallyHiddenText = Some(Message("placeOfNotification.change.hidden")),
                  attributes = Map("id" -> "change-place-of-notification")
                )
              )
            )
          )
        }
      }
    }

    ".isTraderAddressPlaceOfNotification" - {

      "must return None" - {
        "when IsTraderAddressPlaceOfNotificationPage undefined" in {

          val checkYourAnswersHelper = new CheckYourAnswersHelper(emptyUserAnswers, mode)
          checkYourAnswersHelper.isTraderAddressPlaceOfNotification mustBe None
        }
      }

      "must return Some(Row)" - {
        "when IsTraderAddressPlaceOfNotificationPage defined" in {

          val answers = emptyUserAnswers
            .set(IsTraderAddressPlaceOfNotificationPage, true)
            .success
            .value

          val checkYourAnswersHelper = new CheckYourAnswersHelper(answers, mode)
          checkYourAnswersHelper.isTraderAddressPlaceOfNotification mustBe Some(
            Row(
              key = Key(
                content = Message("isTraderAddressPlaceOfNotification.checkYourAnswersLabel"),
                classes = Seq("govuk-!-width-one-half")
              ),
              value = Value(Message("site.yes")),
              actions = List(
                Action(
                  content = Message("site.edit"),
                  href = routes.IsTraderAddressPlaceOfNotificationController.onPageLoad(mrn, mode).url,
                  visuallyHiddenText = Some(Message("isTraderAddressPlaceOfNotification.change.hidden")),
                  attributes = Map("id" -> "change-trader-address-place-of-notification")
                )
              )
            )
          )
        }
      }
    }

    ".incidentOnRoute" - {

      "must return None" - {
        "when IncidentOnRoutePage undefined" in {

          val checkYourAnswersHelper = new CheckYourAnswersHelper(emptyUserAnswers, mode)
          checkYourAnswersHelper.incidentOnRoute mustBe None
        }
      }

      "must return Some(Row)" - {
        "when IncidentOnRoutePage defined" in {

          val answers = emptyUserAnswers
            .set(IncidentOnRoutePage, true)
            .success
            .value

          val checkYourAnswersHelper = new CheckYourAnswersHelper(answers, mode)
          checkYourAnswersHelper.incidentOnRoute mustBe Some(
            Row(
              key = Key(
                content = Message("incidentOnRoute.checkYourAnswersLabel"),
                classes = Seq("govuk-!-width-one-half")
              ),
              value = Value(Message("site.yes")),
              actions = List(
                Action(
                  content = Message("site.edit"),
                  href = routes.IncidentOnRouteController.onPageLoad(mrn, mode).url,
                  visuallyHiddenText = Some(Message("incidentOnRoute.change.hidden")),
                  attributes = Map("id" -> "change-incident-on-route")
                )
              )
            )
          )
        }
      }
    }

    ".traderName" - {

      "must return None" - {
        "when TraderNamePage undefined" in {

          val checkYourAnswersHelper = new CheckYourAnswersHelper(emptyUserAnswers, mode)
          checkYourAnswersHelper.traderName mustBe None
        }
      }

      "must return Some(Row)" - {
        "when TraderNamePage defined" in {

          val answers = emptyUserAnswers
            .set(TraderNamePage, name)
            .success
            .value

          val checkYourAnswersHelper = new CheckYourAnswersHelper(answers, mode)
          checkYourAnswersHelper.traderName mustBe Some(
            Row(
              key = Key(
                content = Message("traderName.checkYourAnswersLabel"),
                classes = Seq("govuk-!-width-one-half")
              ),
              value = Value(Literal(name)),
              actions = List(
                Action(
                  content = Message("site.edit"),
                  href = routes.TraderNameController.onPageLoad(mrn, mode).url,
                  visuallyHiddenText = Some(Message("traderName.change.hidden")),
                  attributes = Map("id" -> "change-trader-name")
                )
              )
            )
          )
        }
      }
    }

    ".traderEori" - {

      "must return None" - {
        "when TraderEoriPage undefined" in {

          val checkYourAnswersHelper = new CheckYourAnswersHelper(emptyUserAnswers, mode)
          checkYourAnswersHelper.traderEori mustBe None
        }
      }

      "must return Some(Row)" - {
        "when TraderEoriPage defined" in {

          val answers = emptyUserAnswers
            .set(TraderEoriPage, eori)
            .success
            .value

          val checkYourAnswersHelper = new CheckYourAnswersHelper(answers, mode)
          checkYourAnswersHelper.traderEori mustBe Some(
            Row(
              key = Key(
                content = Message("traderEori.checkYourAnswersLabel"),
                classes = Seq("govuk-!-width-one-half")
              ),
              value = Value(Literal(eori)),
              actions = List(
                Action(
                  content = Message("site.edit"),
                  href = routes.TraderEoriController.onPageLoad(mrn, mode).url,
                  visuallyHiddenText = Some(Message("traderEori.change.hidden")),
                  attributes = Map("id" -> "change-trader-eori")
                )
              )
            )
          )
        }
      }
    }

    ".traderAddress" - {

      "must return None" - {
        "when TraderAddressPage undefined" in {

          val checkYourAnswersHelper = new CheckYourAnswersHelper(emptyUserAnswers, mode)
          checkYourAnswersHelper.traderAddress mustBe None
        }
      }

      "must return Some(Row)" - {
        "when TraderAddressPage defined" in {

          val answers = emptyUserAnswers
            .set(TraderAddressPage, address)
            .success
            .value

          val checkYourAnswersHelper = new CheckYourAnswersHelper(answers, mode)
          checkYourAnswersHelper.traderAddress mustBe Some(
            Row(
              key = Key(
                content = Message("traderAddress.checkYourAnswersLabel"),
                classes = Seq("govuk-!-width-one-half")
              ),
              value = Value(Html(s"${address.buildingAndStreet}<br>${address.city}<br>${address.postcode}")),
              actions = List(
                Action(
                  content = Message("site.edit"),
                  href = routes.TraderAddressController.onPageLoad(mrn, mode).url,
                  visuallyHiddenText = Some(Message("traderAddress.change.hidden")),
                  attributes = Map("id" -> "change-trader-address")
                )
              )
            )
          )
        }
      }
    }

    ".consigneeAddress" - {

      "must return None" - {

        "when ConsigneeNamePage undefined" in {

          val checkYourAnswersHelper = new CheckYourAnswersHelper(emptyUserAnswers, mode)
          checkYourAnswersHelper.consigneeAddress mustBe None
        }

        "when ConsigneeAddressPage undefined" in {

          val answers = emptyUserAnswers
            .set(ConsigneeNamePage, name)
            .success
            .value

          val checkYourAnswersHelper = new CheckYourAnswersHelper(answers, mode)
          checkYourAnswersHelper.consigneeAddress mustBe None
        }
      }

      "must return Some(Row)" - {
        "when ConsigneeAddressPage defined" in {

          val answers = emptyUserAnswers
            .set(ConsigneeNamePage, name)
            .success
            .value
            .set(ConsigneeAddressPage, address)
            .success
            .value

          val checkYourAnswersHelper = new CheckYourAnswersHelper(answers, mode)
          checkYourAnswersHelper.consigneeAddress mustBe Some(
            Row(
              key = Key(
                content = Message("consigneeAddress.checkYourAnswersLabel").withArgs(name),
                classes = Seq("govuk-!-width-one-half")
              ),
              value = Value(Html(s"${address.buildingAndStreet}<br>${address.city}<br>${address.postcode}")),
              actions = List(
                Action(
                  content = Message("site.edit"),
                  href = routes.ConsigneeAddressController.onPageLoad(mrn, mode).url,
                  visuallyHiddenText = Some(Message("consigneeAddress.change.hidden").withArgs(name)),
                  attributes = Map("id" -> "change-consignee-address")
                )
              )
            )
          )
        }
      }
    }

    ".authorisedLocation" - {

      "must return None" - {
        "when AuthorisedLocationPage undefined" in {

          val checkYourAnswersHelper = new CheckYourAnswersHelper(emptyUserAnswers, mode)
          checkYourAnswersHelper.authorisedLocation mustBe None
        }
      }

      "must return Some(Row)" - {
        "when AuthorisedLocationPage defined" in {

          val answers = emptyUserAnswers
            .set(AuthorisedLocationPage, location)
            .success
            .value

          val checkYourAnswersHelper = new CheckYourAnswersHelper(answers, mode)
          checkYourAnswersHelper.authorisedLocation mustBe Some(
            Row(
              key = Key(
                content = Message("authorisedLocation.checkYourAnswersLabel"),
                classes = Seq("govuk-!-width-one-half")
              ),
              value = Value(Literal(location)),
              actions = List(
                Action(
                  content = Message("site.edit"),
                  href = routes.AuthorisedLocationController.onPageLoad(mrn, mode).url,
                  visuallyHiddenText = Some(Message("authorisedLocation.change.hidden")),
                  attributes = Map("id" -> "change-authorised-location")
                )
              )
            )
          )
        }
      }
    }

    ".customsSubPlace" - {

      "must return None" - {
        "when CustomsSubPlacePage undefined" in {

          val checkYourAnswersHelper = new CheckYourAnswersHelper(emptyUserAnswers, mode)
          checkYourAnswersHelper.customsSubPlace mustBe None
        }
      }

      "must return Some(Row)" - {
        "when CustomsSubPlacePage defined" in {

          val answers = emptyUserAnswers
            .set(CustomsSubPlacePage, location)
            .success
            .value

          val checkYourAnswersHelper = new CheckYourAnswersHelper(answers, mode)
          checkYourAnswersHelper.customsSubPlace mustBe Some(
            Row(
              key = Key(
                content = Message("customsSubPlace.checkYourAnswersLabel"),
                classes = Seq("govuk-!-width-one-half")
              ),
              value = Value(Literal(location)),
              actions = List(
                Action(
                  content = Message("site.edit"),
                  href = routes.CustomsSubPlaceController.onPageLoad(mrn, mode).url,
                  visuallyHiddenText = Some(Message("customsSubPlace.change.hidden")),
                  attributes = Map("id" -> "change-customs-sub-place")
                )
              )
            )
          )
        }
      }
    }

    ".simplifiedCustomsOffice" - {

      "must return None" - {

        "when SimplifiedCustomsOfficePage undefined" in {

          val checkYourAnswersHelper = new CheckYourAnswersHelper(emptyUserAnswers, mode)
          checkYourAnswersHelper.simplifiedCustomsOffice mustBe None
        }

        "when SimplifiedCustomsOfficePage defined but CustomsSubPlacePage and ConsigneeNamePage empty" in {

          val answers = emptyUserAnswers
            .set(SimplifiedCustomsOfficePage, CustomsOffice("id", None, None))
            .success
            .value

          val checkYourAnswersHelper = new CheckYourAnswersHelper(answers, mode)
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

            val checkYourAnswersHelper = new CheckYourAnswersHelper(answers, mode)
            checkYourAnswersHelper.simplifiedCustomsOffice mustBe Some(
              Row(
                key = Key(
                  content = Message("customsOffice.simplified.checkYourAnswersLabel", location),
                  classes = Seq("govuk-!-width-one-half")
                ),
                value = Value(Literal(customsOfficeId)),
                actions = List(
                  Action(
                    content = Message("site.edit"),
                    href = routes.SimplifiedCustomsOfficeController.onPageLoad(mrn, mode).url,
                    visuallyHiddenText = Some(Message("customsOffice.simplified.change.hidden", location)),
                    attributes = Map("id" -> "change-presentation-office")
                  )
                )
              )
            )
          }

          "when SimplifiedCustomsOfficePage and ConsigneeNamePage defined" in {

            val answers = emptyUserAnswers
              .set(SimplifiedCustomsOfficePage, CustomsOffice(customsOfficeId, None, None))
              .success
              .value
              .set(ConsigneeNamePage, location)
              .success
              .value

            val checkYourAnswersHelper = new CheckYourAnswersHelper(answers, mode)
            checkYourAnswersHelper.simplifiedCustomsOffice mustBe Some(
              Row(
                key = Key(
                  content = Message("customsOffice.simplified.checkYourAnswersLabel", location),
                  classes = Seq("govuk-!-width-one-half")
                ),
                value = Value(Literal(customsOfficeId)),
                actions = List(
                  Action(
                    content = Message("site.edit"),
                    href = routes.SimplifiedCustomsOfficeController.onPageLoad(mrn, mode).url,
                    visuallyHiddenText = Some(Message("customsOffice.simplified.change.hidden", location)),
                    attributes = Map("id" -> "change-presentation-office")
                  )
                )
              )
            )
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

            val checkYourAnswersHelper = new CheckYourAnswersHelper(answers, mode)
            checkYourAnswersHelper.simplifiedCustomsOffice mustBe Some(
              Row(
                key = Key(
                  content = Message("customsOffice.simplified.checkYourAnswersLabel", location),
                  classes = Seq("govuk-!-width-one-half")
                ),
                value = Value(Literal(s"$customsOfficeName ($customsOfficeId)")),
                actions = List(
                  Action(
                    content = Message("site.edit"),
                    href = routes.SimplifiedCustomsOfficeController.onPageLoad(mrn, mode).url,
                    visuallyHiddenText = Some(Message("customsOffice.simplified.change.hidden", location)),
                    attributes = Map("id" -> "change-presentation-office")
                  )
                )
              )
            )
          }

          "when SimplifiedCustomsOfficePage and ConsigneeNamePage defined" in {

            val answers = emptyUserAnswers
              .set(SimplifiedCustomsOfficePage, CustomsOffice(customsOfficeId, Some(customsOfficeName), None))
              .success
              .value
              .set(ConsigneeNamePage, location)
              .success
              .value

            val checkYourAnswersHelper = new CheckYourAnswersHelper(answers, mode)
            checkYourAnswersHelper.simplifiedCustomsOffice mustBe Some(
              Row(
                key = Key(
                  content = Message("customsOffice.simplified.checkYourAnswersLabel", location),
                  classes = Seq("govuk-!-width-one-half")
                ),
                value = Value(Literal(s"$customsOfficeName ($customsOfficeId)")),
                actions = List(
                  Action(
                    content = Message("site.edit"),
                    href = routes.SimplifiedCustomsOfficeController.onPageLoad(mrn, mode).url,
                    visuallyHiddenText = Some(Message("customsOffice.simplified.change.hidden", location)),
                    attributes = Map("id" -> "change-presentation-office")
                  )
                )
              )
            )
          }
        }
      }
    }

    ".customsOffice" - {

      "must return None" - {

        "when CustomsOfficePage undefined" in {

          val checkYourAnswersHelper = new CheckYourAnswersHelper(emptyUserAnswers, mode)
          checkYourAnswersHelper.customsOffice mustBe None
        }

        "when CustomsOfficePage defined but CustomsSubPlacePage and ConsigneeNamePage empty" in {

          val answers = emptyUserAnswers
            .set(CustomsOfficePage, CustomsOffice("id", None, None))
            .success
            .value

          val checkYourAnswersHelper = new CheckYourAnswersHelper(answers, mode)
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

            val checkYourAnswersHelper = new CheckYourAnswersHelper(answers, mode)
            checkYourAnswersHelper.customsOffice mustBe Some(
              Row(
                key = Key(
                  content = Message("customsOffice.checkYourAnswersLabel", location),
                  classes = Seq("govuk-!-width-one-half")
                ),
                value = Value(Literal(customsOfficeId)),
                actions = List(
                  Action(
                    content = Message("site.edit"),
                    href = routes.CustomsOfficeController.onPageLoad(mrn, mode).url,
                    visuallyHiddenText = Some(Message("customsOffice.change.hidden", location)),
                    attributes = Map("id" -> "change-presentation-office")
                  )
                )
              )
            )
          }

          "when CustomsOfficePage and ConsigneeNamePage defined" in {

            val answers = emptyUserAnswers
              .set(CustomsOfficePage, CustomsOffice(customsOfficeId, None, None))
              .success
              .value
              .set(ConsigneeNamePage, location)
              .success
              .value

            val checkYourAnswersHelper = new CheckYourAnswersHelper(answers, mode)
            checkYourAnswersHelper.customsOffice mustBe Some(
              Row(
                key = Key(
                  content = Message("customsOffice.checkYourAnswersLabel", location),
                  classes = Seq("govuk-!-width-one-half")
                ),
                value = Value(Literal(customsOfficeId)),
                actions = List(
                  Action(
                    content = Message("site.edit"),
                    href = routes.CustomsOfficeController.onPageLoad(mrn, mode).url,
                    visuallyHiddenText = Some(Message("customsOffice.change.hidden", location)),
                    attributes = Map("id" -> "change-presentation-office")
                  )
                )
              )
            )
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

            val checkYourAnswersHelper = new CheckYourAnswersHelper(answers, mode)
            checkYourAnswersHelper.customsOffice mustBe Some(
              Row(
                key = Key(
                  content = Message("customsOffice.checkYourAnswersLabel", location),
                  classes = Seq("govuk-!-width-one-half")
                ),
                value = Value(Literal(s"$customsOfficeName ($customsOfficeId)")),
                actions = List(
                  Action(
                    content = Message("site.edit"),
                    href = routes.CustomsOfficeController.onPageLoad(mrn, mode).url,
                    visuallyHiddenText = Some(Message("customsOffice.change.hidden", location)),
                    attributes = Map("id" -> "change-presentation-office")
                  )
                )
              )
            )
          }

          "when CustomsOfficePage and ConsigneeNamePage defined" in {

            val answers = emptyUserAnswers
              .set(CustomsOfficePage, CustomsOffice(customsOfficeId, Some(customsOfficeName), None))
              .success
              .value
              .set(ConsigneeNamePage, location)
              .success
              .value

            val checkYourAnswersHelper = new CheckYourAnswersHelper(answers, mode)
            checkYourAnswersHelper.customsOffice mustBe Some(
              Row(
                key = Key(
                  content = Message("customsOffice.checkYourAnswersLabel", location),
                  classes = Seq("govuk-!-width-one-half")
                ),
                value = Value(Literal(s"$customsOfficeName ($customsOfficeId)")),
                actions = List(
                  Action(
                    content = Message("site.edit"),
                    href = routes.CustomsOfficeController.onPageLoad(mrn, mode).url,
                    visuallyHiddenText = Some(Message("customsOffice.change.hidden", location)),
                    attributes = Map("id" -> "change-presentation-office")
                  )
                )
              )
            )
          }
        }
      }
    }
  }

}
