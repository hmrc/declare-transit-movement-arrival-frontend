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

package navigation

import base.SpecBase
import controllers.events.{routes => eventRoutes}
import controllers.routes
import generators.{DomainModelGenerators, Generators}
import models.{CheckMode, GoodsLocation, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.events._
import pages._

class CheckModeNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators with DomainModelGenerators {

  val navigator = app.injector.instanceOf[Navigator]

  "Navigator in Check mode" - {

    "must go from a page that doesn't exist in the edit route map  to Check Your Answers" in {

      case object UnknownPage extends Page

      forAll(arbitrary[UserAnswers]) {
        answers =>
          navigator
            .nextPage(UnknownPage, CheckMode, answers)
            .mustBe(routes.CheckYourAnswersController.onPageLoad(answers.id))
      }
    }

    "must go from Goods Location" - {

      "to Check Your Answers" - {

        "when the user answers Border Force Office and they have already answered Customs Sub Place" in {

          forAll(arbitrary[UserAnswers], arbitrary[String]) {
            (answers, subPlace) =>
              val updatedAnswers =
                answers
                  .set(GoodsLocationPage, GoodsLocation.BorderForceOffice)
                  .success
                  .value
                  .set(CustomsSubPlacePage, subPlace)
                  .success
                  .value

              navigator
                .nextPage(GoodsLocationPage, CheckMode, updatedAnswers)
                .mustBe(routes.CheckYourAnswersController.onPageLoad(answers.id))
          }
        }
      }

      "to Customs Sub Place" - {

        "when the user answers Border Force Office and had not answered Customs Sub Place" in {

          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers =
                answers
                  .set(GoodsLocationPage, GoodsLocation.BorderForceOffice)
                  .success
                  .value
                  .remove(CustomsSubPlacePage)
                  .success
                  .value

              navigator
                .nextPage(GoodsLocationPage, CheckMode, updatedAnswers)
                .mustBe(routes.CustomsSubPlaceController.onPageLoad(answers.id, CheckMode))
          }
        }
      }

      "to Use Different Service" - {

        "when the user answers Authorised Consignee" in {

          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers = answers.set(GoodsLocationPage, GoodsLocation.AuthorisedConsigneesLocation).success.value

              navigator
                .nextPage(GoodsLocationPage, CheckMode, updatedAnswers)
                .mustBe(routes.UseDifferentServiceController.onPageLoad(answers.id))
          }
        }
      }
    }

    Seq(
      EventCountryPage(eventIndex),
      EventPlacePage(eventIndex),
      IncidentInformationPage(eventIndex)
    ) foreach {
      page =>
        s"must go from $page pages to check event answers" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              navigator
                .nextPage(page, CheckMode, answers)
                .mustBe(eventRoutes.CheckEventAnswersController.onPageLoad(answers.id, eventIndex))
          }
        }
    }

    "must go from EventReportedPage pages must go to check event answers" in {
      // TODO: We need to force them down correct route since their answers may now be incomplete/inconsistent
      forAll(arbitrary[UserAnswers]) {
        answers =>
          navigator
            .nextPage(EventReportedPage(eventIndex), CheckMode, answers)
            .mustBe(eventRoutes.CheckEventAnswersController.onPageLoad(answers.id, eventIndex))
      }
    }

    "must go from IsTranshipmentPage pages must go to check event answers" in {
      // TODO: We need to force them down correct route since their answers may now be incomplete/inconsistent
      forAll(arbitrary[UserAnswers]) {
        answers =>
          navigator
            .nextPage(IsTranshipmentPage(eventIndex), CheckMode, answers)
            .mustBe(eventRoutes.CheckEventAnswersController.onPageLoad(answers.id, eventIndex))
      }
    }

    "must go from 'IsTraderAddressPlaceOfNotificationPage'" - {
      "to 'Check Your Answers' when answer is 'No' and there is a 'Place of notification'" in {
        forAll(arbitrary[UserAnswers], arbitrary[String]) {
          (answers, placeOfNotification) =>
            val updatedUserAnswers = answers
              .set(IsTraderAddressPlaceOfNotificationPage, false)
              .success
              .value
              .set(PlaceOfNotificationPage, placeOfNotification)
              .success
              .value

            navigator
              .nextPage(IsTraderAddressPlaceOfNotificationPage, CheckMode, updatedUserAnswers)
              .mustBe(routes.CheckYourAnswersController.onPageLoad(updatedUserAnswers.id))
        }
      }

      "to 'Check Your Answers' when answer is 'Yes'" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedUserAnswers = answers.set(IsTraderAddressPlaceOfNotificationPage, true).success.value

            navigator
              .nextPage(IsTraderAddressPlaceOfNotificationPage, CheckMode, updatedUserAnswers)
              .mustBe(routes.CheckYourAnswersController.onPageLoad(updatedUserAnswers.id))
        }
      }
    }

    "go from 'Place of Notification' to CheckYourAnswer" in {
      import models.domain.messages.NormalNotification.Constants.notificationPlaceLength

      forAll(arbitrary[UserAnswers], stringsWithMaxLength(notificationPlaceLength)) {
        case (answers, placeOfNotification) =>
          val updatedUserAnswers = answers.set(PlaceOfNotificationPage, placeOfNotification).success.value

          navigator
            .nextPage(PlaceOfNotificationPage, CheckMode, updatedUserAnswers)
            .mustBe(routes.CheckYourAnswersController.onPageLoad(updatedUserAnswers.id))
      }
    }

  }

}
