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
import controllers.routes
import generators.Generators
import pages._
import models._
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class NavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  val navigator = new Navigator

  "Navigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {

        case object UnknownPage extends Page

        forAll(arbitrary[UserAnswers]) {
          answers =>

            navigator.nextPage(UnknownPage, NormalMode, answers)
              .mustBe(routes.IndexController.onPageLoad())
        }
      }

      "must go from movement reference number to 'Good location' page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>

            navigator.nextPage(MovementReferenceNumberPage, NormalMode, answers)
              .mustBe(routes.GoodsLocationController.onPageLoad(answers.id, NormalMode))
        }

      }

      "must go from 'goods location' to  'presentation office' when user chooses 'Border Force Office'" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>

            val updatedAnswers = answers.set(GoodsLocationPage, GoodsLocation.BorderForceOffice).success.value
            navigator.nextPage(GoodsLocationPage, NormalMode, updatedAnswers)
              .mustBe(routes.PresentationOfficeController.onPageLoad(updatedAnswers.id, NormalMode))
        }
      }

      "must go from 'presentation office' to  'customs approved location'" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>

            navigator.nextPage(PresentationOfficePage, NormalMode, answers)
              .mustBe(routes.CustomsSubPlaceController.onPageLoad(answers.id, NormalMode))
        }
      }

      "must go from 'goods location' to  'authorised location' when user chooses 'Authorised consignee’s location'" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>

            val updatedAnswers = answers.set(GoodsLocationPage, GoodsLocation.AuthorisedConsigneesLocation).success.value
            navigator.nextPage(GoodsLocationPage, NormalMode, updatedAnswers)
              .mustBe(routes.AuthorisedLocationController.onPageLoad(updatedAnswers.id, NormalMode))
        }
      }

      "must go from 'customs approved location' to  'traders name'" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>

            navigator.nextPage(CustomsSubPlacePage, NormalMode, answers)
              .mustBe(routes.TraderNameController.onPageLoad(answers.id, NormalMode))
        }
      }

      "must go from 'traders name' to 'traders address'" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>

            navigator.nextPage(TraderNamePage, NormalMode, answers)
              .mustBe(routes.TraderAddressController.onPageLoad(answers.id, NormalMode))
        }
      }

      "must go from 'traders address' to 'traders eori'" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>

            navigator.nextPage(TraderAddressPage, NormalMode, answers)
              .mustBe(routes.TraderEoriController.onPageLoad(answers.id, NormalMode))
        }
      }

      "must go from 'traders eori' to 'incident on route page'" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>

            navigator.nextPage(TraderEoriPage, NormalMode, answers)
              .mustBe(routes.IncidentOnRouteController.onPageLoad(answers.id, NormalMode))
        }
      }

      "must go from 'incident on route' to 'check your answers page'" in {

        forAll(arbitrary[UserAnswers]) {
          answers =>

            navigator.nextPage(IncidentOnRoutePage, NormalMode, answers)
              .mustBe(routes.CheckYourAnswersController.onPageLoad(answers.id))
        }
      }

    }

    "in Check mode" - {

      "must go from a page that doesn't exist in the edit route map  to Check Your Answers" in {

        case object UnknownPage extends Page

        forAll(arbitrary[UserAnswers]) {
          answers =>

            navigator.nextPage(UnknownPage, CheckMode, answers)
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
                    .set(GoodsLocationPage, GoodsLocation.BorderForceOffice).success.value
                    .set(CustomsSubPlacePage, subPlace).success.value

                navigator.nextPage(GoodsLocationPage, CheckMode, updatedAnswers)
                  .mustBe(routes.CheckYourAnswersController.onPageLoad(answers.id))
            }
          }

          "when the user answers Authorised Consignee and they have already answered Authoerised Location" in {

            forAll(arbitrary[UserAnswers], arbitrary[String]) {
              (answers, location) =>

                val updatedAnswers =
                  answers
                    .set(GoodsLocationPage, GoodsLocation.AuthorisedConsigneesLocation).success.value
                    .set(AuthorisedLocationPage, location).success.value

                navigator.nextPage(GoodsLocationPage, CheckMode, updatedAnswers)
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
                    .set(GoodsLocationPage, GoodsLocation.BorderForceOffice).success.value
                    .remove(CustomsSubPlacePage).success.value

                navigator.nextPage(GoodsLocationPage, CheckMode, updatedAnswers)
                  .mustBe(routes.CustomsSubPlaceController.onPageLoad(answers.id, CheckMode))
            }
          }
        }

        "to Authorised Location" - {

          "when the user answers Authorised Consignee and had not answered Authorised Location" in {

            forAll(arbitrary[UserAnswers]) {
              answers =>

                val updatedAnswers =
                  answers
                    .set(GoodsLocationPage, GoodsLocation.AuthorisedConsigneesLocation).success.value
                    .remove(AuthorisedLocationPage).success.value

                navigator.nextPage(GoodsLocationPage, CheckMode, updatedAnswers)
                  .mustBe(routes.AuthorisedLocationController.onPageLoad(answers.id, CheckMode))
            }
          }
        }
      }
    }
  }
}
