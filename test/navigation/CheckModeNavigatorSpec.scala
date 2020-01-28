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
import controllers.events.seals.{routes => sealRoutes}
import controllers.events.transhipments.{routes => transhipmentRoutes}
import controllers.routes
import generators.{Generators, MessagesModelGenerators}
import models.TranshipmentType.{DifferentContainer, DifferentContainerAndVehicle, DifferentVehicle}
import models.messages.Container
import models.reference.Country
import models.{CheckMode, GoodsLocation, Index, NormalMode, TranshipmentType, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.events._
import pages._
import pages.events.seals.{AddSealPage, HaveSealsChangedPage, SealIdentityPage}
import pages.events.transhipments.{
  AddContainerPage,
  ConfirmRemoveContainerPage,
  ContainerNumberPage,
  TranshipmentTypePage,
  TransportIdentityPage,
  TransportNationalityPage
}
import queries.{ContainersQuery, EventsQuery}

class CheckModeNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators with MessagesModelGenerators {

  private val navigator: Navigator = app.injector.instanceOf[Navigator]

  private val country: Country = Country("Valid", "GB", "United Kingdom")

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

      "to Check Event Answers when true and they have answered TranshipmentType and is Vehicle type" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val ua = answers
              .set(IsTranshipmentPage(eventIndex), true)
              .success
              .value
              .set(TranshipmentTypePage(eventIndex), DifferentVehicle)
              .success
              .value

            navigator
              .nextPage(IsTranshipmentPage(eventIndex), CheckMode, ua)
              .mustBe(eventRoutes.CheckEventAnswersController.onPageLoad(ua.id, eventIndex))
        }
      }

      "to Check Event Answers when true and they have answered TranshipmentType and is Container or Both type and has a container" in {
        forAll(arbitrary[UserAnswers], Gen.oneOf(DifferentContainer, DifferentContainerAndVehicle), arbitrary[Container]) {
          (answers, transhipmentType, container) =>
            val ua = answers
              .set(IsTranshipmentPage(eventIndex), true)
              .success
              .value
              .set(TranshipmentTypePage(eventIndex), transhipmentType)
              .success
              .value
              .set(ContainerNumberPage(eventIndex, containerIndex), container)
              .success
              .value

            navigator
              .nextPage(IsTranshipmentPage(eventIndex), CheckMode, ua)
              .mustBe(eventRoutes.CheckEventAnswersController.onPageLoad(ua.id, eventIndex))
        }
      }

      "to TranshipmentType when true and they have answered TranshipmentType and is Container or Both type there are no containers" in {
        forAll(arbitrary[UserAnswers], Gen.oneOf(DifferentContainer, DifferentContainerAndVehicle)) {
          (answers, transhipmentType) =>
            val ua = answers
              .set(IsTranshipmentPage(eventIndex), true)
              .success
              .value
              .set(TranshipmentTypePage(eventIndex), transhipmentType)
              .success
              .value
              .remove(ContainersQuery(eventIndex))
              .success
              .value

            navigator
              .nextPage(IsTranshipmentPage(eventIndex), CheckMode, ua)
              .mustBe(transhipmentRoutes.TranshipmentTypeController.onPageLoad(ua.id, eventIndex, CheckMode))
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

      "to CheckEventAnswers when 'Both' is selected and ContainerNumber and vehicle identity and nationality questions have been answered" in {
        forAll(arbitrary[UserAnswers], arbitrary[Container], arbitrary[String], arbitrary[Country]) {
          (answers, container, transportIdentity, transportNationality) =>
            val updatedUserAnswers = answers
              .set(TranshipmentTypePage(eventIndex), DifferentContainerAndVehicle)
              .success
              .value
              .set(ContainerNumberPage(eventIndex, containerIndex), container)
              .success
              .value
              .set(TransportIdentityPage(eventIndex), transportIdentity)
              .success
              .value
              .set(TransportNationalityPage(eventIndex), transportNationality)
              .success
              .value
            navigator
              .nextPage(TranshipmentTypePage(eventIndex), CheckMode, updatedUserAnswers)
              .mustBe(eventRoutes.CheckEventAnswersController.onPageLoad(updatedUserAnswers.id, eventIndex))
        }
      }

      "to addContainerPage when 'Both' is selected and ContainerNumber has been answered but Transport Identity and Nationality has not been answered" in {
        forAll(arbitrary[UserAnswers], arbitrary[Container]) {
          (answers, container) =>
            val updatedUserAnswers = answers
              .set(TranshipmentTypePage(eventIndex), DifferentContainerAndVehicle)
              .success
              .value
              .set(ContainerNumberPage(eventIndex, containerIndex), container)
              .success
              .value
              .remove(TransportIdentityPage(eventIndex))
              .success
              .value
              .remove(TransportNationalityPage(eventIndex))
              .success
              .value

            navigator
              .nextPage(TranshipmentTypePage(eventIndex), CheckMode, updatedUserAnswers)
              .mustBe(transhipmentRoutes.AddContainerController.onPageLoad(updatedUserAnswers.id, eventIndex, CheckMode))
        }
      }

      "to addContainerPage when 'Both' is selected and ContainerNumber has been answered but Transport Nationality has not been answered" in {
        forAll(arbitrary[UserAnswers], arbitrary[Container]) {
          (answers, container) =>
            val updatedUserAnswers = answers
              .set(TranshipmentTypePage(eventIndex), DifferentContainerAndVehicle)
              .success
              .value
              .set(ContainerNumberPage(eventIndex, containerIndex), container)
              .success
              .value
              .remove(TransportNationalityPage(eventIndex))
              .success
              .value

            navigator
              .nextPage(TranshipmentTypePage(eventIndex), CheckMode, updatedUserAnswers)
              .mustBe(transhipmentRoutes.AddContainerController.onPageLoad(updatedUserAnswers.id, eventIndex, CheckMode))
        }
      }

      "to addContainerPage when 'Both' is selected and ContainerNumber has been answered but Transport Identity has not been answered" in {
        forAll(arbitrary[UserAnswers], arbitrary[Container]) {
          (answers, container) =>
            val updatedUserAnswers = answers
              .set(TranshipmentTypePage(eventIndex), DifferentContainerAndVehicle)
              .success
              .value
              .set(ContainerNumberPage(eventIndex, containerIndex), container)
              .success
              .value
              .remove(TransportIdentityPage(eventIndex))
              .success
              .value

            navigator
              .nextPage(TranshipmentTypePage(eventIndex), CheckMode, updatedUserAnswers)
              .mustBe(transhipmentRoutes.AddContainerController.onPageLoad(updatedUserAnswers.id, eventIndex, CheckMode))
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

      "to TransportNationalityPage when TransportNationality has not been answered" in {
        forAll(arbitrary[UserAnswers], arbitrary[String]) {
          (answers, transportIdentity) =>
            val updatedUserAnswers = answers
              .set(TransportIdentityPage(eventIndex), transportIdentity)
              .success
              .value
              .remove(TransportNationalityPage(eventIndex))
              .success
              .value

            navigator
              .nextPage(TransportIdentityPage(eventIndex), CheckMode, updatedUserAnswers)
              .mustBe(transhipmentRoutes.TransportNationalityController.onPageLoad(answers.id, eventIndex, CheckMode))
        }
      }

      "to CheckEventAnswersPage when TransportNationality has been answered" in {
        forAll(arbitrary[UserAnswers], arbitrary[String], arbitrary[Country]) {
          (answers, transportIdentity, transportNationality) =>
            val updatedUserAnswers = answers
              .set(TransportIdentityPage(eventIndex), transportIdentity)
              .success
              .value
              .set(TransportNationalityPage(eventIndex), transportNationality)
              .success
              .value

            navigator
              .nextPage(TransportIdentityPage(eventIndex), CheckMode, updatedUserAnswers)
              .mustBe(eventRoutes.CheckEventAnswersController.onPageLoad(answers.id, eventIndex))
        }
      }
    }

    "must go from TransportNationality" - {

      "to CheckEventAnswers" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(TransportNationalityPage(eventIndex), CheckMode, answers)
              .mustBe(eventRoutes.CheckEventAnswersController.onPageLoad(answers.id, eventIndex))
        }
      }
    }

    "must go from AddContainerPage" - {
      "to CheckEventAnswers when false and the TranshipmentTypePage is 'A different container'" in {

        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers = answers
              .set(TranshipmentTypePage(eventIndex), DifferentContainer)
              .success
              .value
              .set(AddContainerPage(eventIndex), false)
              .success
              .value

            navigator
              .nextPage(AddContainerPage(eventIndex), CheckMode, updatedAnswers)
              .mustBe(eventRoutes.CheckEventAnswersController.onPageLoad(updatedAnswers.id, eventIndex))
        }
      }

      "to TransportIdentityPage when false, TranshipmentTypePage is 'Both' and TransportIdentity has not been answered" in {

        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers = answers
              .set(TranshipmentTypePage(eventIndex), DifferentContainerAndVehicle)
              .success
              .value
              .set(AddContainerPage(eventIndex), false)
              .success
              .value
              .remove(TransportIdentityPage(eventIndex))
              .success
              .value

            navigator
              .nextPage(AddContainerPage(eventIndex), CheckMode, updatedAnswers)
              .mustBe(transhipmentRoutes.TransportIdentityController.onPageLoad(updatedAnswers.id, eventIndex, CheckMode))
        }
      }

      "to CheckEventAnswers when false, TranshipmentTypePage is 'Both' and TransportIdentity has been answered" in {

        forAll(arbitrary[UserAnswers], arbitrary[String]) {
          (answers, transportIdentity) =>
            val updatedAnswers = answers
              .set(TranshipmentTypePage(eventIndex), DifferentContainerAndVehicle)
              .success
              .value
              .set(AddContainerPage(eventIndex), false)
              .success
              .value
              .set(TransportIdentityPage(eventIndex), transportIdentity)
              .success
              .value

            navigator
              .nextPage(AddContainerPage(eventIndex), CheckMode, updatedAnswers)
              .mustBe(eventRoutes.CheckEventAnswersController.onPageLoad(updatedAnswers.id, eventIndex))
        }
      }

      "to ContainerNumber page in when true with the index increased" in {
        val nextIndex = Index(containerIndex.position + 1)
        forAll(arbitrary[UserAnswers], arbitrary[Container]) {
          (answers, container) =>
            val updatedAnswers = answers
              .set(ContainerNumberPage(eventIndex, containerIndex), container)
              .success
              .value
              .set(AddContainerPage(eventIndex), true)
              .success
              .value

            navigator
              .nextPage(AddContainerPage(eventIndex), CheckMode, updatedAnswers)
              .mustBe(transhipmentRoutes.ContainerNumberController.onPageLoad(updatedAnswers.id, eventIndex, nextIndex, CheckMode))
        }
      }

    }

    "seals page" - {

      "must go from seals identity page to add seals page" in {
        forAll(arbitrary[UserAnswers], arbitrary[String]) {
          (answers, sealsIdentity) =>
            val updatedAnswers = answers.set(SealIdentityPage(eventIndex, sealIndex), sealsIdentity).success.value

            navigator
              .nextPage(SealIdentityPage(eventIndex, sealIndex), CheckMode, updatedAnswers)
              .mustBe(sealRoutes.AddSealController.onPageLoad(answers.id, eventIndex, CheckMode))
        }
      }

      "must go from have seals changed page to check event answers page when the answer is 'No'" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers = answers.set(HaveSealsChangedPage(eventIndex), false).success.value

            navigator
              .nextPage(HaveSealsChangedPage(eventIndex), CheckMode, updatedAnswers)
              .mustBe(eventRoutes.CheckEventAnswersController.onPageLoad(answers.id, eventIndex))
        }
      }

      "must go from have seals changed page to seal identity page page when the answer is 'Yes'" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers = answers
              .set(HaveSealsChangedPage(eventIndex), true)
              .success
              .value
              .remove(SealIdentityPage(eventIndex, sealIndex))
              .success
              .value

            navigator
              .nextPage(HaveSealsChangedPage(eventIndex), CheckMode, updatedAnswers)
              .mustBe(sealRoutes.SealIdentityController.onPageLoad(answers.id, eventIndex, sealIndex, CheckMode))
        }
      }

      "go from addSealPage to sealIdentity when Yes is selected" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers = answers
              .remove(SealIdentityPage(eventIndex, sealIndex))
              .success
              .value
              .set(AddSealPage(eventIndex), true)
              .success
              .value

            navigator
              .nextPage(AddSealPage(eventIndex), CheckMode, updatedAnswers)
              .mustBe(sealRoutes.SealIdentityController.onPageLoad(answers.id, eventIndex, sealIndex, CheckMode))
        }
      }

      "go from addSealPage to checkEventAnswers when No is selected" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers = answers
              .set(AddSealPage(eventIndex), false)
              .success
              .value

            navigator
              .nextPage(AddSealPage(eventIndex), CheckMode, updatedAnswers)
              .mustBe(eventRoutes.CheckEventAnswersController.onPageLoad(answers.id, eventIndex))
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

      "to 'Place of notification' when answer is 'No' and there is no existing 'Place of notification'" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedUserAnswers = answers
              .set(IsTraderAddressPlaceOfNotificationPage, false)
              .success
              .value
              .remove(PlaceOfNotificationPage)
              .success
              .value

            navigator
              .nextPage(IsTraderAddressPlaceOfNotificationPage, CheckMode, updatedUserAnswers)
              .mustBe(routes.PlaceOfNotificationController.onPageLoad(updatedUserAnswers.id, CheckMode))
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
      import models.messages.NormalNotification.Constants.notificationPlaceLength

      forAll(arbitrary[UserAnswers], stringsWithMaxLength(notificationPlaceLength)) {
        case (answers, placeOfNotification) =>
          val updatedUserAnswers = answers.set(PlaceOfNotificationPage, placeOfNotification).success.value

          navigator
            .nextPage(PlaceOfNotificationPage, CheckMode, updatedUserAnswers)
            .mustBe(routes.CheckYourAnswersController.onPageLoad(updatedUserAnswers.id))
      }
    }

    "must go from Confirm remove container page" - {

      "to Add container page when containers exist" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers = answers
              .remove(EventsQuery)
              .success
              .value
              .set(EventCountryPage(eventIndex), country)
              .success
              .value
              .set(EventPlacePage(eventIndex), "place name")
              .success
              .value
              .set(EventReportedPage(eventIndex), true)
              .success
              .value
              .set(IsTranshipmentPage(eventIndex), true)
              .success
              .value
              .set(TranshipmentTypePage(eventIndex), DifferentContainer)
              .success
              .value
              .set(ContainerNumberPage(eventIndex, containerIndex), Container("1"))
              .success
              .value
            navigator
              .nextPage(ConfirmRemoveContainerPage(eventIndex), CheckMode, updatedAnswers)
              .mustBe(transhipmentRoutes.AddContainerController.onPageLoad(updatedAnswers.id, eventIndex, CheckMode))
        }
      }

      "to isTranshipment page when no containers exist" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers = answers
              .remove(EventsQuery)
              .success
              .value
              .set(EventCountryPage(eventIndex), country)
              .success
              .value
              .set(EventPlacePage(eventIndex), "place name")
              .success
              .value
              .set(EventReportedPage(eventIndex), true)
              .success
              .value
              .set(IsTranshipmentPage(eventIndex), true)
              .success
              .value
              .set(TranshipmentTypePage(eventIndex), DifferentContainer)
              .success
              .value
            navigator
              .nextPage(ConfirmRemoveContainerPage(eventIndex), CheckMode, updatedAnswers)
              .mustBe(eventRoutes.IsTranshipmentController.onPageLoad(updatedAnswers.id, eventIndex, CheckMode))
        }
      }

      "must go from incident on route page" - {

        "to event country page when user selects yes" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers = answers
                .remove(IncidentOnRoutePage)
                .success
                .value
                .set(IncidentOnRoutePage, true)
                .success
                .value
              navigator
                .nextPage(IncidentOnRoutePage, CheckMode, updatedAnswers)
                .mustBe(eventRoutes.EventCountryController.onPageLoad(answers.id, eventIndex, NormalMode))

          }

        }
      }

    }
  }
}
