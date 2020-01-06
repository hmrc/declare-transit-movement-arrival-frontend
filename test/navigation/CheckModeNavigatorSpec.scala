/*
 * Copyright 2020 HM Revenue & Customs
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
import controllers.events.transhipments.{routes => transhipmentRoutes}
import controllers.routes
import generators.{DomainModelGenerators, Generators}
import models.TranshipmentType.{DifferentContainer, DifferentContainerAndVehicle, DifferentVehicle}
import models.domain.Container
import models.{CheckMode, GoodsLocation, TranshipmentType, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.events._
import pages._
import pages.events.transhipments.{AddContainerPage, ContainerNumberPage, TranshipmentTypePage, TransportIdentityPage}

class CheckModeNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators with DomainModelGenerators {

  val navigator: Navigator = app.injector.instanceOf[Navigator]
//format: off
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

    "must go from EventReportedPage pages" - {
      "to check event answers when event reported is true" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val ua = answers.set(EventReportedPage(eventIndex), true).success.value

            navigator
              .nextPage(EventReportedPage(eventIndex), CheckMode, ua)
              .mustBe(eventRoutes.CheckEventAnswersController.onPageLoad(ua.id, eventIndex))
        }
      }

      "to check event answers when event reported is false and transhipment is true" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val ua = answers
              .set(EventReportedPage(eventIndex), false)
              .success
              .value
              .set(IsTranshipmentPage(eventIndex), true)
              .success
              .value
            navigator
              .nextPage(EventReportedPage(eventIndex), CheckMode, ua)
              .mustBe(eventRoutes.CheckEventAnswersController.onPageLoad(ua.id, eventIndex))
        }

      }

      "to incident information when event reported is false and is not a transhipment" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val ua = answers
              .set(EventReportedPage(eventIndex), false)
              .success
              .value
              .set(IsTranshipmentPage(eventIndex), false)
              .success
              .value

            navigator
              .nextPage(EventReportedPage(eventIndex), CheckMode, ua)
              .mustBe(eventRoutes.IncidentInformationController.onPageLoad(ua.id, eventIndex, CheckMode))
        }
      }

    }

    "must go from IsTranshipmentPage" - {

      "to TranshipmentTypePage when true and they have not answered TranshipmentType" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val ua = answers
              .set(IsTranshipmentPage(eventIndex), true)
              .success
              .value
              .remove(TranshipmentTypePage(eventIndex))
              .success
              .value

            navigator
              .nextPage(IsTranshipmentPage(eventIndex), CheckMode, ua)
              .mustBe(transhipmentRoutes.TranshipmentTypeController.onPageLoad(ua.id, eventIndex, CheckMode))
        }
      }

      "to Check Event Answers when true and they have answered TranshipmentType" in {
        forAll(arbitrary[UserAnswers], arbitrary[TranshipmentType]) {
          (answers, transhipmentType) =>
            val ua = answers
              .set(IsTranshipmentPage(eventIndex), true)
              .success
              .value
              .set(TranshipmentTypePage(eventIndex), transhipmentType)
              .success
              .value

            navigator
              .nextPage(IsTranshipmentPage(eventIndex), CheckMode, ua)
              .mustBe(eventRoutes.CheckEventAnswersController.onPageLoad(ua.id, eventIndex))
        }
      }

      "to Check Event Answers when false and ReportedEvent is true" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val ua = answers
              .set(EventReportedPage(eventIndex), true)
              .success
              .value
              .set(IsTranshipmentPage(eventIndex), false)
              .success
              .value

            navigator
              .nextPage(IsTranshipmentPage(eventIndex), CheckMode, ua)
              .mustBe(eventRoutes.CheckEventAnswersController.onPageLoad(ua.id, eventIndex))
        }
      }

      "to incident Information when false and ReportedEvent is false and they have not answered IncidentInformation" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val ua = answers
              .set(EventReportedPage(eventIndex), false)
              .success
              .value
              .set(IsTranshipmentPage(eventIndex), false)
              .success
              .value
              .remove(IncidentInformationPage(eventIndex))
              .success
              .value

            navigator
              .nextPage(IsTranshipmentPage(eventIndex), CheckMode, ua)
              .mustBe(eventRoutes.IncidentInformationController.onPageLoad(ua.id, eventIndex, CheckMode))
        }
      }

      "to Check Event Answers when false, ReportedEvent is false and IncidentInformation has been answered" in {
        forAll(arbitrary[UserAnswers], arbitrary[String]) {
          (answers, incidentInformationAnswer) =>
            val ua = answers
              .set(EventReportedPage(eventIndex), false)
              .success
              .value
              .set(IsTranshipmentPage(eventIndex), false)
              .success
              .value
              .set(IncidentInformationPage(eventIndex), incidentInformationAnswer)
              .success
              .value

            navigator
              .nextPage(IsTranshipmentPage(eventIndex), CheckMode, ua)
              .mustBe(eventRoutes.CheckEventAnswersController.onPageLoad(ua.id, eventIndex))
        }
      }
    }

    "must go from TranshipmentTypePage" - {

      "to ContainerNumberPage when 'A different container' is selected and ContainerNumber has not been answered" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedUserAnswers = answers
              .set(TranshipmentTypePage(eventIndex), DifferentContainer)
              .success
              .value
              .remove(ContainerNumberPage(eventIndex, containerIndex))
              .success
              .value

            navigator
              .nextPage(TranshipmentTypePage(eventIndex), CheckMode, updatedUserAnswers)
              .mustBe(transhipmentRoutes.ContainerNumberController.onPageLoad(updatedUserAnswers.id, eventIndex, containerIndex, CheckMode))
        }
      }

      "to CheckEventAnswers when 'A different container' is selected and ContainerNumber has been answered" in {
        forAll(arbitrary[UserAnswers], arbitrary[Container]) {
          (answers, container) =>
            val updatedUserAnswers = answers
              .set(TranshipmentTypePage(eventIndex), DifferentContainer)
              .success
              .value
              .set(ContainerNumberPage(eventIndex, containerIndex), container)
              .success
              .value

            navigator
              .nextPage(TranshipmentTypePage(eventIndex), CheckMode, updatedUserAnswers)
              .mustBe(eventRoutes.CheckEventAnswersController.onPageLoad(updatedUserAnswers.id, eventIndex))
        }
      }

      "to TransportIdentityPage when 'A different vehicle' is selected and TransportIdentity has not been answered" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedUserAnswers = answers
              .set(TranshipmentTypePage(eventIndex), DifferentVehicle)
              .success
              .value
              .remove(TransportIdentityPage(eventIndex))
              .success
              .value

            navigator
              .nextPage(TranshipmentTypePage(eventIndex), CheckMode, updatedUserAnswers)
              .mustBe(transhipmentRoutes.TransportIdentityController.onPageLoad(updatedUserAnswers.id, eventIndex, CheckMode))
        }
      }

      "to CheckEventAnswers when 'A different vehicle' is selected and TransportIdentity has been answered" in {
        forAll(arbitrary[UserAnswers], arbitrary[String]) {
          (answers, transportIdentity) =>
            val updatedUserAnswers = answers
              .set(TranshipmentTypePage(eventIndex), DifferentVehicle)
              .success
              .value
              .set(TransportIdentityPage(eventIndex), transportIdentity)
              .success
              .value

            navigator
              .nextPage(TranshipmentTypePage(eventIndex), CheckMode, updatedUserAnswers)
              .mustBe(eventRoutes.CheckEventAnswersController.onPageLoad(updatedUserAnswers.id, eventIndex))
        }
      }

      "to ContainerNumberPage when 'Both' is selected and ContainerNumber has not been answered" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedUserAnswers = answers
              .set(TranshipmentTypePage(eventIndex), DifferentContainerAndVehicle)
              .success
              .value
              .remove(ContainerNumberPage(eventIndex, containerIndex))
              .success
              .value

            navigator
              .nextPage(TranshipmentTypePage(eventIndex), CheckMode, updatedUserAnswers)
              .mustBe(transhipmentRoutes.ContainerNumberController.onPageLoad(updatedUserAnswers.id, eventIndex, containerIndex, CheckMode))
        }
      }

      "to CheckEventAnswers when 'Both' is selected and ContainerNumber has been answered" in {
        forAll(arbitrary[UserAnswers], arbitrary[Container]) {
          (answers, container) =>
            val updatedUserAnswers = answers
              .set(TranshipmentTypePage(eventIndex), DifferentContainerAndVehicle)
              .success
              .value
              .set(ContainerNumberPage(eventIndex, containerIndex), container)
              .success
              .value

            navigator
              .nextPage(TranshipmentTypePage(eventIndex), CheckMode, updatedUserAnswers)
              .mustBe(eventRoutes.CheckEventAnswersController.onPageLoad(updatedUserAnswers.id, eventIndex))
        }
      }

    }

    "must go from ContainerNumberPage" - {

      "to AddContainer" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(ContainerNumberPage(eventIndex, containerIndex), CheckMode, answers)
              .mustBe(transhipmentRoutes.AddContainerController.onPageLoad(answers.id, eventIndex, CheckMode))
        }
      }

    }

    "must go from TransportIdentityPage" - {

      "to TransportNationalityPage" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(TransportIdentityPage(eventIndex), CheckMode, answers)
              .mustBe(transhipmentRoutes.TransportNationalityController.onPageLoad(answers.id, eventIndex, CheckMode))
        }
      }
    }

    "must go from AddContainerPage" - {
      "to CheckEventAnswers when false" in {

        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers = answers.set(AddContainerPage(eventIndex), false).success.value

            navigator
              .nextPage(AddContainerPage(eventIndex), CheckMode, updatedAnswers)
              .mustBe(eventRoutes.CheckEventAnswersController.onPageLoad(updatedAnswers.id, eventIndex))
        }
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
//format: on
