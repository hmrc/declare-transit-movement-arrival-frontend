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
import controllers.events.seals.routes._
import controllers.events.transhipments.routes._
import models.domain.{ContainerDomain, SealDomain}
import models.reference.{Country, CountryCode}
import models.{CheckMode, CountryList, Mode, TranshipmentType}
import pages.events._
import pages.events.seals._
import pages.events.transhipments._
import uk.gov.hmrc.viewmodels.SummaryList.{Action, Key, Row, Value}
import uk.gov.hmrc.viewmodels.Text.{Literal, Message}

class CheckEventAnswersHelperSpec extends SpecBase {

  val mode: Mode = CheckMode

  "CheckEventAnswersHelper" - {

    ".isTranshipment" - {

      "must return None" - {
        "when IsTranshipmentPage undefined" in {

          val helper = new CheckEventAnswersHelper(emptyUserAnswers, mode)
          helper.isTranshipment(eventIndex) mustBe None
        }
      }

      "must return Some(Row)" - {
        "when IsTranshipmentPage defined" in {

          val answers = emptyUserAnswers
            .set(IsTranshipmentPage(eventIndex), true)
            .success
            .value

          val helper = new CheckEventAnswersHelper(answers, mode)
          helper.isTranshipment(eventIndex) mustBe Some(
            Row(
              key = Key(
                content = Message("isTranshipment.checkYourAnswersLabel"),
                classes = Seq("govuk-!-width-one-half")
              ),
              value = Value(Message("site.yes")),
              actions = List(
                Action(
                  content = Message("site.edit"),
                  href = IsTranshipmentController.onPageLoad(mrn, eventIndex, mode).url,
                  visuallyHiddenText = Some(Message("isTranshipment.change.hidden")),
                  attributes = Map("id" -> s"change-is-transhipment-${eventIndex.display}")
                )
              )
            )
          )
        }
      }
    }

    ".transhipmentType" - {

      val transhipmentType = TranshipmentType.DifferentContainer

      "must return None" - {
        "when TranshipmentTypePage undefined" in {

          val helper = new CheckEventAnswersHelper(emptyUserAnswers, mode)
          helper.transhipmentType(eventIndex) mustBe None
        }
      }

      "must return Some(Row)" - {
        "when TranshipmentTypePage defined" in {

          val answers = emptyUserAnswers
            .set(TranshipmentTypePage(eventIndex), transhipmentType)
            .success
            .value

          val helper = new CheckEventAnswersHelper(answers, mode)
          helper.transhipmentType(eventIndex) mustBe Some(
            Row(
              key = Key(
                content = Message("transhipmentType.checkYourAnswersLabel"),
                classes = Seq("govuk-!-width-one-half")
              ),
              value = Value(Message(s"transhipmentType.checkYourAnswers.$transhipmentType")),
              actions = List(
                Action(
                  content = Message("site.edit"),
                  href = TranshipmentTypeController.onPageLoad(mrn, eventIndex, mode).url,
                  visuallyHiddenText = Some(Message("transhipmentType.change.hidden")),
                  attributes = Map("id" -> s"transhipment-type-${eventIndex.display}")
                )
              )
            )
          )
        }
      }
    }

    ".containerNumber" - {

      val containerDomain = ContainerDomain("NUMBER")

      "must return None" - {
        "when ContainerNumberPage undefined" in {

          val helper = new CheckEventAnswersHelper(emptyUserAnswers, mode)
          helper.containerNumber(eventIndex, containerIndex) mustBe None
        }
      }

      "must return Some(Row)" - {
        "when ContainerNumberPage defined" in {

          val answers = emptyUserAnswers
            .set(ContainerNumberPage(eventIndex, containerIndex), containerDomain)
            .success
            .value

          val helper = new CheckEventAnswersHelper(answers, mode)
          helper.containerNumber(eventIndex, containerIndex) mustBe Some(
            Row(
              key = Key(
                content = Message("addContainer.containerList.label", containerIndex.display),
                classes = Seq("govuk-!-width-one-half")
              ),
              value = Value(Literal(containerDomain.containerNumber)),
              actions = List(
                Action(
                  content = Message("site.edit"),
                  href = ContainerNumberController.onPageLoad(mrn, eventIndex, containerIndex, mode).url,
                  visuallyHiddenText = Some(Message("containerNumber.change.hidden", containerDomain.containerNumber)),
                  attributes = Map("id" -> s"change-container-${containerIndex.display}")
                )
              )
            )
          )
        }
      }
    }

    ".eventCountry" - {

      val countryCode = CountryCode("CODE")

      "must return None" - {
        "when EventCountryPage undefined" in {

          val helper = new CheckEventAnswersHelper(emptyUserAnswers, mode)
          helper.eventCountry(eventIndex)(CountryList(Nil)) mustBe None
        }
      }

      "must return Some(Row)" - {
        "when EventCountryPage defined" - {

          "country not found" in {

            val answers = emptyUserAnswers
              .set(EventCountryPage(eventIndex), countryCode)
              .success
              .value

            val helper = new CheckEventAnswersHelper(answers, mode)
            helper.eventCountry(eventIndex)(CountryList(Nil)) mustBe Some(
              Row(
                key = Key(
                  content = Message("eventCountry.checkYourAnswersLabel"),
                  classes = Seq("govuk-!-width-one-half")
                ),
                value = Value(Literal(countryCode.code)),
                actions = List(
                  Action(
                    content = Message("site.edit"),
                    href = EventCountryController.onPageLoad(mrn, eventIndex, mode).url,
                    visuallyHiddenText = Some(Message("eventCountry.change.hidden")),
                    attributes = Map("id" -> s"change-event-country-${eventIndex.display}")
                  )
                )
              )
            )
          }

          "country found" in {

            val country = Country(countryCode, "DESCRIPTION")

            val answers = emptyUserAnswers
              .set(EventCountryPage(eventIndex), countryCode)
              .success
              .value

            val helper = new CheckEventAnswersHelper(answers, mode)
            helper.eventCountry(eventIndex)(CountryList(Seq(country))) mustBe Some(
              Row(
                key = Key(
                  content = Message("eventCountry.checkYourAnswersLabel"),
                  classes = Seq("govuk-!-width-one-half")
                ),
                value = Value(Literal(country.description)),
                actions = List(
                  Action(
                    content = Message("site.edit"),
                    href = EventCountryController.onPageLoad(mrn, eventIndex, mode).url,
                    visuallyHiddenText = Some(Message("eventCountry.change.hidden")),
                    attributes = Map("id" -> s"change-event-country-${eventIndex.display}")
                  )
                )
              )
            )
          }
        }
      }
    }

    ".eventPlace" - {

      val place = "PLACE"

      "must return None" - {
        "when EventPlacePage undefined" in {

          val helper = new CheckEventAnswersHelper(emptyUserAnswers, mode)
          helper.eventPlace(eventIndex) mustBe None
        }
      }

      "must return Some(Row)" - {
        "when EventPlacePage defined" in {

          val answers = emptyUserAnswers
            .set(EventPlacePage(eventIndex), place)
            .success
            .value

          val helper = new CheckEventAnswersHelper(answers, mode)
          helper.eventPlace(eventIndex) mustBe Some(
            Row(
              key = Key(
                content = Message("eventPlace.checkYourAnswersLabel"),
                classes = Seq("govuk-!-width-one-half")
              ),
              value = Value(Literal(place)),
              actions = List(
                Action(
                  content = Message("site.edit"),
                  href = EventPlaceController.onPageLoad(mrn, eventIndex, mode).url,
                  visuallyHiddenText = Some(Message("eventPlace.change.hidden")),
                  attributes = Map("id" -> s"change-event-place-${eventIndex.display}")
                )
              )
            )
          )
        }
      }
    }

    ".eventReported" - {

      "must return None" - {
        "when EventReportedPage undefined" in {

          val helper = new CheckEventAnswersHelper(emptyUserAnswers, mode)
          helper.eventReported(eventIndex) mustBe None
        }
      }

      "must return Some(Row)" - {
        "when EventReportedPage defined" in {

          val answers = emptyUserAnswers
            .set(EventReportedPage(eventIndex), false)
            .success
            .value

          val helper = new CheckEventAnswersHelper(answers, mode)
          helper.eventReported(eventIndex) mustBe Some(
            Row(
              key = Key(
                content = Message("eventReported.checkYourAnswersLabel"),
                classes = Seq("govuk-!-width-one-half")
              ),
              value = Value(Message("site.no")),
              actions = List(
                Action(
                  content = Message("site.edit"),
                  href = EventReportedController.onPageLoad(mrn, eventIndex, mode).url,
                  visuallyHiddenText = Some(Message("eventReported.change.hidden")),
                  attributes = Map("id" -> s"change-event-reported-${eventIndex.display}")
                )
              )
            )
          )
        }
      }
    }

    ".incidentInformation" - {

      val incident = "INCIDENT"

      "must return None" - {
        "when IncidentInformationPage undefined" in {

          val helper = new CheckEventAnswersHelper(emptyUserAnswers, mode)
          helper.incidentInformation(eventIndex) mustBe None
        }
      }

      "must return Some(Row)" - {
        "when IncidentInformationPage defined" in {

          val answers = emptyUserAnswers
            .set(IncidentInformationPage(eventIndex), incident)
            .success
            .value

          val helper = new CheckEventAnswersHelper(answers, mode)
          helper.incidentInformation(eventIndex) mustBe Some(
            Row(
              key = Key(
                content = Message("incidentInformation.checkYourAnswersLabel"),
                classes = Seq("govuk-!-width-one-half")
              ),
              value = Value(Literal(incident)),
              actions = List(
                Action(
                  content = Message("site.edit"),
                  href = IncidentInformationController.onPageLoad(mrn, eventIndex, mode).url,
                  visuallyHiddenText = Some(Message("incidentInformation.change.hidden")),
                  attributes = Map("id" -> s"change-incident-information-${eventIndex.display}")
                )
              )
            )
          )
        }
      }
    }

    ".transportIdentity" - {

      val vehicle = "VEHICLE"

      "must return None" - {
        "when TransportIdentityPage undefined" in {

          val helper = new CheckEventAnswersHelper(emptyUserAnswers, mode)
          helper.transportIdentity(eventIndex) mustBe None
        }
      }

      "must return Some(Row)" - {
        "when TransportIdentityPage defined" in {

          val answers = emptyUserAnswers
            .set(TransportIdentityPage(eventIndex), vehicle)
            .success
            .value

          val helper = new CheckEventAnswersHelper(answers, mode)
          helper.transportIdentity(eventIndex) mustBe Some(
            Row(
              key = Key(
                content = Message("transportIdentity.checkYourAnswersLabel"),
                classes = Seq("govuk-!-width-one-half")
              ),
              value = Value(Literal(vehicle)),
              actions = List(
                Action(
                  content = Message("site.edit"),
                  href = TransportIdentityController.onPageLoad(mrn, eventIndex, mode).url,
                  visuallyHiddenText = Some(Message("transportIdentity.change.hidden")),
                  attributes = Map("id" -> s"transport-identity-${eventIndex.display}")
                )
              )
            )
          )
        }
      }
    }

    ".transportNationality" - {

      val countryCode = CountryCode("CODE")

      "must return None" - {
        "when TransportNationalityPage undefined" in {

          val helper = new CheckEventAnswersHelper(emptyUserAnswers, mode)
          helper.transportNationality(eventIndex)(CountryList(Nil)) mustBe None
        }
      }

      "must return Some(Row)" - {
        "when TransportNationalityPage defined" - {

          "country not found" in {

            val answers = emptyUserAnswers
              .set(TransportNationalityPage(eventIndex), countryCode)
              .success
              .value

            val helper = new CheckEventAnswersHelper(answers, mode)
            helper.transportNationality(eventIndex)(CountryList(Nil)) mustBe Some(
              Row(
                key = Key(
                  content = Message("transportNationality.checkYourAnswersLabel"),
                  classes = Seq("govuk-!-width-one-half")
                ),
                value = Value(Literal(countryCode.code)),
                actions = List(
                  Action(
                    content = Message("site.edit"),
                    href = TransportNationalityController.onPageLoad(mrn, eventIndex, mode).url,
                    visuallyHiddenText = Some(Message("transportNationality.change.hidden")),
                    attributes = Map("id" -> s"transport-nationality-${eventIndex.display}")
                  )
                )
              )
            )
          }

          "country found" in {

            val country = Country(countryCode, "DESCRIPTION")

            val answers = emptyUserAnswers
              .set(TransportNationalityPage(eventIndex), countryCode)
              .success
              .value

            val helper = new CheckEventAnswersHelper(answers, mode)
            helper.transportNationality(eventIndex)(CountryList(Seq(country))) mustBe Some(
              Row(
                key = Key(
                  content = Message("transportNationality.checkYourAnswersLabel"),
                  classes = Seq("govuk-!-width-one-half")
                ),
                value = Value(Literal(country.description)),
                actions = List(
                  Action(
                    content = Message("site.edit"),
                    href = TransportNationalityController.onPageLoad(mrn, eventIndex, mode).url,
                    visuallyHiddenText = Some(Message("transportNationality.change.hidden")),
                    attributes = Map("id" -> s"transport-nationality-${eventIndex.display}")
                  )
                )
              )
            )
          }
        }
      }
    }

    ".haveSealsChanged" - {

      "must return None" - {
        "when HaveSealsChangedPage undefined" in {

          val helper = new CheckEventAnswersHelper(emptyUserAnswers, mode)
          helper.haveSealsChanged(eventIndex) mustBe None
        }
      }

      "must return Some(Row)" - {
        "when HaveSealsChangedPage defined" in {

          val answers = emptyUserAnswers
            .set(HaveSealsChangedPage(eventIndex), true)
            .success
            .value

          val helper = new CheckEventAnswersHelper(answers, mode)
          helper.haveSealsChanged(eventIndex) mustBe Some(
            Row(
              key = Key(
                content = Message("haveSealsChanged.checkYourAnswersLabel"),
                classes = Seq("govuk-!-width-one-half")
              ),
              value = Value(Message("site.yes")),
              actions = List(
                Action(
                  content = Message("site.edit"),
                  href = HaveSealsChangedController.onPageLoad(mrn, eventIndex, mode).url,
                  visuallyHiddenText = Some(Message("haveSealsChanged.change.hidden")),
                  attributes = Map("id" -> s"seals-changed-${eventIndex.display}")
                )
              )
            )
          )
        }
      }
    }

    ".sealIdentity" - {

      val seal = SealDomain("NUMBER")

      "must return None" - {
        "when SealIdentityPage undefined" in {

          val helper = new CheckEventAnswersHelper(emptyUserAnswers, mode)
          helper.sealIdentity(eventIndex, sealIndex) mustBe None
        }
      }

      "must return Some(Row)" - {
        "when SealIdentityPage defined" in {

          val answers = emptyUserAnswers
            .set(SealIdentityPage(eventIndex, sealIndex), seal)
            .success
            .value

          val helper = new CheckEventAnswersHelper(answers, mode)
          helper.sealIdentity(eventIndex, sealIndex) mustBe Some(
            Row(
              key = Key(
                content = Message("addSeal.sealList.label", sealIndex.display),
                classes = Seq("govuk-!-width-one-half")
              ),
              value = Value(Literal(seal.numberOrMark)),
              actions = List(
                Action(
                  content = Message("site.edit"),
                  href = SealIdentityController.onPageLoad(mrn, eventIndex, sealIndex, mode).url,
                  visuallyHiddenText = Some(Message("sealIdentity.change.hidden", seal.numberOrMark)),
                  attributes = Map("id" -> s"change-seal-${sealIndex.display}")
                )
              )
            )
          )
        }
      }
    }

  }

}
