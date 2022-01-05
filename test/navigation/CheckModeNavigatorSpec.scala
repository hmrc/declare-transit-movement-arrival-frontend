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

package navigation

import base.SpecBase
import controllers.events.seals.{routes => sealRoutes}
import controllers.events.transhipments.{routes => transhipmentRoutes}
import controllers.events.{routes => eventRoutes}
import controllers.routes
import generators.{Generators, MessagesModelGenerators}
import models.GoodsLocation.BorderForceOffice
import models.TranshipmentType.{DifferentContainer, DifferentContainerAndVehicle, DifferentVehicle}
import models.domain.{ContainerDomain, SealDomain}
import models.reference.{CountryCode, CustomsOffice}
import models.{Address, CheckMode, GoodsLocation, Index, NormalMode, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._
import pages.events._
import pages.events.seals.{AddSealPage, HaveSealsChangedPage, SealIdentityPage}
import pages.events.transhipments._
import queries.{ContainersQuery, EventsQuery}

class CheckModeNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators with MessagesModelGenerators {

  // format: off

  private val navigator: Navigator = new Navigator()

  private val country: CountryCode = CountryCode("GB")

  "Navigator in Check mode" - {
    "must go from a page that doesn't exist in the edit route map  to Check Your Answers" in {
      case object UnknownPage extends Page

      forAll(arbitrary[UserAnswers]) {
        answers =>
          navigator
            .nextPage(UnknownPage, CheckMode, answers)
            .mustBe(routes.CheckYourAnswersController.onPageLoad(answers.movementReferenceNumber))
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

              navigator
                .nextPage(GoodsLocationPage, CheckMode, updatedAnswers)
                .mustBe(routes.CheckYourAnswersController.onPageLoad(answers.movementReferenceNumber))
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

              navigator
                .nextPage(GoodsLocationPage, CheckMode, updatedAnswers)
                .mustBe(routes.CustomsSubPlaceController.onPageLoad(answers.movementReferenceNumber, CheckMode))
          }
        }
      }

      "must go from 'CustomsSubPlaceController' to " - {
        "'CustomsOfficeController' when no previous answer for 'CustomsOffice'" in {
          forAll(arbitrary[UserAnswers], nonEmptyString) {
            (answers, customsSubPlace) =>
              val updatedAnswers =
                answers
                  .set(CustomsSubPlacePage, customsSubPlace).success.value

              navigator
                .nextPage(CustomsSubPlacePage, CheckMode, updatedAnswers)
                .mustBe(routes.CustomsOfficeController.onPageLoad(answers.movementReferenceNumber, CheckMode))
          }
        }

        "'CheckYourAnswersPage' when already answer for 'CustomsOffice'" in {
          forAll(arbitrary[UserAnswers], arbitrary[String], arbitrary[CustomsOffice]) {
            (answers, customsSubPlace, customsOffice) =>
              val updatedAnswers =
                answers
                  .set(CustomsSubPlacePage, customsSubPlace).success.value
                  .set(CustomsOfficePage, customsOffice).success.value

              navigator
                .nextPage(CustomsSubPlacePage, CheckMode, updatedAnswers)
                .mustBe(routes.CheckYourAnswersController.onPageLoad(answers.movementReferenceNumber))

          }
        }
      }
      "must go from OfficeOfPresentation to" - {
        "CheckYourAnswersPage when trader at destination name has already been answered " in {
          forAll(arbitrary[UserAnswers], arbitrary[CustomsOffice], arbitrary[String]) {
            (answers, customsOffice, traderName) =>
              val updatedAnswers =
                answers
                  .set(CustomsOfficePage, customsOffice).success.value
                  .set(TraderNamePage, traderName).success.value

              navigator
                .nextPage(CustomsSubPlacePage, CheckMode, updatedAnswers)
                .mustBe(routes.CheckYourAnswersController.onPageLoad(answers.movementReferenceNumber))

          }
        }
        "trader name when trader name has not previously been answered on Normal Route" in {
          forAll(arbitrary[UserAnswers], arbitrary[CustomsOffice]) {
            (answers, customsOffice) =>
              val updatedAnswers =
                answers
                  .set(GoodsLocationPage, BorderForceOffice)
                  .success
                  .value
                  .set(CustomsOfficePage, customsOffice).success.value

                  .remove(TraderNamePage).success.value

              navigator
                .nextPage(CustomsOfficePage, CheckMode, updatedAnswers)
                .mustBe(routes.TraderNameController.onPageLoad(answers.movementReferenceNumber, CheckMode))

          }
        }
        "from trader name page to" - {
          "CheckYourAnswersPage when trader eori has previously been answered " in {
            forAll(arbitrary[UserAnswers], arbitrary[String]) {
              (answers, traderEori) =>
                val updatedAnswers =
                  answers
                    .set(TraderNamePage, traderName).success.value
                    .set(TraderEoriPage, traderEori).success.value

                navigator
                  .nextPage(TraderNamePage, CheckMode, updatedAnswers)
                  .mustBe(routes.CheckYourAnswersController.onPageLoad(answers.movementReferenceNumber))

            }
          }
          "TraderEoriPage when trader eori has not previously been answered " in {
            forAll(arbitrary[UserAnswers]) {
              answers =>
                val updatedAnswers =
                  answers.remove(TraderEoriPage).success.value

                navigator
                  .nextPage(TraderNamePage, CheckMode, updatedAnswers)
                  .mustBe(routes.TraderEoriController.onPageLoad(answers.movementReferenceNumber, CheckMode))

            }
          }
        }
        "from trader eori page to" - {
          "CheckYourAnswersPage when trader address has previously been answered " in {
            forAll(arbitrary[UserAnswers], arbitrary[String], arbitrary[Address]) {
              (answers, traderEori, traderAddress) =>
                val updatedAnswers =
                  answers
                    .set(TraderEoriPage, traderEori).success.value
                    .set(TraderAddressPage, traderAddress).success.value

                navigator
                  .nextPage(TraderEoriPage, CheckMode, updatedAnswers)
                  .mustBe(routes.CheckYourAnswersController.onPageLoad(answers.movementReferenceNumber))

            }
          }
          "TraderAddressPage when trader address has not previously been answered " in {
            forAll(arbitrary[UserAnswers], arbitrary[String]) {
              (answers, traderEori) =>
                val updatedAnswers =
                  answers
                    .set(TraderEoriPage, traderEori).success.value
                    .remove(TraderAddressPage).success.value

                navigator
                  .nextPage(TraderEoriPage, CheckMode, updatedAnswers)
                  .mustBe(routes.TraderAddressController.onPageLoad(answers.movementReferenceNumber, CheckMode))

            }
          }
        }


        "must go from 'TraderAddressController' to " - {
          "'IsTraderAddressPlaceOfNotifcationController' when this is not answered" in {
            forAll(arbitrary[UserAnswers]) {
              answers =>
                val userAnswers = answers
                  .remove(IsTraderAddressPlaceOfNotificationPage).success.value

                navigator
                  .nextPage(TraderAddressPage, CheckMode, userAnswers)
                  .mustBe(routes.IsTraderAddressPlaceOfNotificationController.onPageLoad(userAnswers.movementReferenceNumber, CheckMode))
            }
          }

          "'CheckYourAnswersController' when 'IsTraderAddressPlaceOfNotification' has been answered" in {
            forAll(arbitrary[UserAnswers], arbitrary[Boolean]) {
              (answers, isTraderAddressPlaceOfNotification) =>
                val userAnswers = answers
                  .set(IsTraderAddressPlaceOfNotificationPage, isTraderAddressPlaceOfNotification).success.value

                navigator
                  .nextPage(TraderAddressPage, CheckMode, userAnswers)
                  .mustBe(routes.CheckYourAnswersController.onPageLoad(userAnswers.movementReferenceNumber))
            }

          }
        }

        "must go from 'IsTraderAddressPlaceOfNotificationController' to " - {
          "'PlaceOfNotificationController' when the answer is false and it has not been answered" in {
            forAll(arbitrary[UserAnswers]) {
              answers =>
                val userAnswers = answers
                  .set(IsTraderAddressPlaceOfNotificationPage, false).success.value
                  .remove(PlaceOfNotificationPage).success.value
                navigator
                  .nextPage(IsTraderAddressPlaceOfNotificationPage, CheckMode, userAnswers)
                  .mustBe(routes.PlaceOfNotificationController.onPageLoad(userAnswers.movementReferenceNumber, CheckMode))
            }
          }

          "'CheckYourAnswersController' when the answer is true" in {
            forAll(arbitrary[UserAnswers]) {
              answers =>
                val userAnswers = answers
                  .set(IsTraderAddressPlaceOfNotificationPage, true).success.value

                navigator
                  .nextPage(IsTraderAddressPlaceOfNotificationPage, CheckMode, userAnswers)
                  .mustBe(routes.CheckYourAnswersController.onPageLoad(answers.movementReferenceNumber))
            }
          }

          "'CheckYourAnswersController' when the answer is false when PlaceOfNotification has been answered" in {
            forAll(arbitrary[UserAnswers], nonEmptyString) {
              (answers, placeOfNotification) =>
                val userAnswers = answers
                  .set(IsTraderAddressPlaceOfNotificationPage, false).success.value
                  .set(PlaceOfNotificationPage, placeOfNotification).success.value

                navigator
                  .nextPage(IsTraderAddressPlaceOfNotificationPage, CheckMode, userAnswers)
                  .mustBe(routes.CheckYourAnswersController.onPageLoad(answers.movementReferenceNumber))
            }
          }

        }

        "must go from 'PlaceOfNotification' to " - {
          "'CheckYourAnswersController'" in {
            forAll(arbitrary[UserAnswers]) {
              answers =>
                navigator
                  .nextPage(PlaceOfNotificationPage, CheckMode, answers)
                  .mustBe(routes.CheckYourAnswersController.onPageLoad(answers.movementReferenceNumber))
            }
          }
        }
        "must go from 'AuthorisedLocationController' to " - {
          "'CheckYourAnswersController' when an answer for 'ConsigneeName'" in {
            forAll(arbitrary[UserAnswers], nonEmptyString, nonEmptyString) {
              (answers, authorisedLocation, consigneeName) =>
                val updatedAnswers =
                  answers
                    .set(AuthorisedLocationPage, authorisedLocation).success.value
                    .set(ConsigneeNamePage, consigneeName).success.value

                navigator
                  .nextPage(AuthorisedLocationPage, CheckMode, updatedAnswers)
                  .mustBe(routes.CheckYourAnswersController.onPageLoad(answers.movementReferenceNumber))
            }
          }


        }

        "must go from 'ConsigneeNameController' to " - {
          "'Consignee eori number page no address is present'" in {
            forAll(arbitrary[UserAnswers], nonEmptyString) {
              (answers, consigneeName) =>
                val updatedAnswers =
                  answers
                    .set(ConsigneeNamePage, consigneeName).success.value
                    .remove(ConsigneeAddressPage).success.value

                navigator
                  .nextPage(ConsigneeNamePage, CheckMode, updatedAnswers)
                  .mustBe(routes.ConsigneeEoriNumberController.onPageLoad(answers.movementReferenceNumber, CheckMode))
            }
          }
          "'CYA page' when address is present" in {
            forAll(arbitrary[UserAnswers], nonEmptyString) {
              (answers, consigneeName) =>
                val updatedAnswers =
                  answers
                    .set(ConsigneeNamePage, consigneeName).success.value
                    .set(ConsigneeAddressPage, traderAddress ).success.value

                navigator
                  .nextPage(ConsigneeNamePage, CheckMode, updatedAnswers)
                  .mustBe(routes.CheckYourAnswersController.onPageLoad(answers.movementReferenceNumber))
            }
          }

        }

        "must go from 'ConsigneeEoriNumberController' to " - {
          "'Consignee Address page' when no address is present" in {
            forAll(arbitrary[UserAnswers]) {
              answers =>
                val updatedAnswers = answers
                  .remove(ConsigneeAddressPage).success.value

                navigator
                  .nextPage(ConsigneeEoriNumberPage, CheckMode, updatedAnswers)
                  .mustBe(routes.ConsigneeAddressController.onPageLoad(updatedAnswers.movementReferenceNumber, CheckMode))
            }
          }

          "'CYA page' when  address is present" in {
            forAll(arbitrary[UserAnswers]) {
              answers =>
                val updatedAnswers = answers
                  .set(ConsigneeAddressPage, traderAddress ).success.value
                navigator
                  .nextPage(ConsigneeEoriNumberPage, CheckMode, updatedAnswers)
                  .mustBe(routes.CheckYourAnswersController.onPageLoad(updatedAnswers.movementReferenceNumber))
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
                  .mustBe(routes.UseDifferentServiceController.onPageLoad(answers.movementReferenceNumber))
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
                  .mustBe(eventRoutes.CheckEventAnswersController.onPageLoad(answers.movementReferenceNumber, eventIndex))
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
                .mustBe(eventRoutes.CheckEventAnswersController.onPageLoad(ua.movementReferenceNumber, eventIndex))
          }
        }

        "to check event answers when event reported is false and transhipment is true" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val ua = answers
                .set(EventReportedPage(eventIndex), false).success.value
                .set(IsTranshipmentPage(eventIndex), true).success.value
              navigator
                .nextPage(EventReportedPage(eventIndex), CheckMode, ua)
                .mustBe(eventRoutes.CheckEventAnswersController.onPageLoad(ua.movementReferenceNumber, eventIndex))
          }

        }

        "to incident information when event reported is false and is not a transhipment" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val ua = answers
                .set(EventReportedPage(eventIndex), false).success.value
                .set(IsTranshipmentPage(eventIndex), false).success.value

              navigator
                .nextPage(EventReportedPage(eventIndex), CheckMode, ua)
                .mustBe(eventRoutes.IncidentInformationController.onPageLoad(ua.movementReferenceNumber, eventIndex, CheckMode))
          }
        }

      }

      "must go from IsTranshipmentPage" - {

        "to TranshipmentTypePage when true and they have not answered TranshipmentType" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val ua = answers
                .set(IsTranshipmentPage(eventIndex), true).success.value
                .remove(TranshipmentTypePage(eventIndex)).success.value

              navigator
                .nextPage(IsTranshipmentPage(eventIndex), CheckMode, ua)
                .mustBe(transhipmentRoutes.TranshipmentTypeController.onPageLoad(ua.movementReferenceNumber, eventIndex, CheckMode))
          }
        }

        "to Check Event Answers when true and they have answered TranshipmentType and is Vehicle type" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val ua = answers
                .set(IsTranshipmentPage(eventIndex), true).success.value
                .set(TranshipmentTypePage(eventIndex), DifferentVehicle).success.value

              navigator
                .nextPage(IsTranshipmentPage(eventIndex), CheckMode, ua)
                .mustBe(eventRoutes.CheckEventAnswersController.onPageLoad(ua.movementReferenceNumber, eventIndex))
          }
        }

        "to Check Event Answers when true and they have answered TranshipmentType and is Container or Both type and has a container" in {
          forAll(arbitrary[UserAnswers], Gen.oneOf(DifferentContainer, DifferentContainerAndVehicle), arbitrary[ContainerDomain]) {
            (answers, transhipmentType, container) =>
              val ua = answers
                .set(IsTranshipmentPage(eventIndex), true).success.value
                .set(TranshipmentTypePage(eventIndex), transhipmentType).success.value
                .set(ContainerNumberPage(eventIndex, containerIndex), container).success.value

              navigator
                .nextPage(IsTranshipmentPage(eventIndex), CheckMode, ua)
                .mustBe(eventRoutes.CheckEventAnswersController.onPageLoad(ua.movementReferenceNumber, eventIndex))
          }
        }

        "to TranshipmentType when true and they have answered TranshipmentType and is Container or Both type there are no containers" in {
          forAll(arbitrary[UserAnswers], Gen.oneOf(DifferentContainer, DifferentContainerAndVehicle)) {
            (answers, transhipmentType) =>
              val ua = answers
                .set(IsTranshipmentPage(eventIndex), true).success.value
                .set(TranshipmentTypePage(eventIndex), transhipmentType).success.value
                .remove(ContainersQuery(eventIndex)).success.value

              navigator
                .nextPage(IsTranshipmentPage(eventIndex), CheckMode, ua)
                .mustBe(transhipmentRoutes.TranshipmentTypeController.onPageLoad(ua.movementReferenceNumber, eventIndex, CheckMode))
          }
        }

        "to Check Event Answers when false and ReportedEvent is true" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val ua = answers
                .set(EventReportedPage(eventIndex), true).success.value
                .set(IsTranshipmentPage(eventIndex), false).success.value

              navigator
                .nextPage(IsTranshipmentPage(eventIndex), CheckMode, ua)
                .mustBe(eventRoutes.CheckEventAnswersController.onPageLoad(ua.movementReferenceNumber, eventIndex))
          }
        }

        "to incident Information when false and ReportedEvent is false and they have not answered IncidentInformation" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val ua = answers
                .set(EventReportedPage(eventIndex), false).success.value
                .set(IsTranshipmentPage(eventIndex), false).success.value
                .remove(IncidentInformationPage(eventIndex)).success.value

              navigator
                .nextPage(IsTranshipmentPage(eventIndex), CheckMode, ua)
                .mustBe(eventRoutes.IncidentInformationController.onPageLoad(ua.movementReferenceNumber, eventIndex, CheckMode))
          }
        }

        "to Check Event Answers when false, ReportedEvent is false and IncidentInformation has been answered" in {
          forAll(arbitrary[UserAnswers], arbitrary[String]) {
            (answers, incidentInformationAnswer) =>
              val ua = answers
                .set(EventReportedPage(eventIndex), false).success.value
                .set(IsTranshipmentPage(eventIndex), false).success.value
                .set(IncidentInformationPage(eventIndex), incidentInformationAnswer).success.value

              navigator
                .nextPage(IsTranshipmentPage(eventIndex), CheckMode, ua)
                .mustBe(eventRoutes.CheckEventAnswersController.onPageLoad(ua.movementReferenceNumber, eventIndex))
          }
        }
      }

      "must go from TranshipmentTypePage" - {

        "to ContainerNumberPage when 'A different container' is selected and ContainerNumber has not been answered" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedUserAnswers = answers
                .set(TranshipmentTypePage(eventIndex), DifferentContainer).success.value
                .remove(ContainerNumberPage(eventIndex, containerIndex)).success.value

              navigator
                .nextPage(TranshipmentTypePage(eventIndex), CheckMode, updatedUserAnswers)
                .mustBe(transhipmentRoutes.ContainerNumberController.onPageLoad(updatedUserAnswers.movementReferenceNumber, eventIndex, containerIndex, CheckMode))
          }
        }

        "to CheckEventAnswers when 'A different container' is selected and ContainerNumber has been answered" in {
          forAll(arbitrary[UserAnswers], arbitrary[ContainerDomain]) {
            (answers, container) =>
              val updatedUserAnswers = answers
                .set(TranshipmentTypePage(eventIndex), DifferentContainer).success.value
                .set(ContainerNumberPage(eventIndex, containerIndex), container).success.value

              navigator
                .nextPage(TranshipmentTypePage(eventIndex), CheckMode, updatedUserAnswers)
                .mustBe(eventRoutes.CheckEventAnswersController.onPageLoad(updatedUserAnswers.movementReferenceNumber, eventIndex))
          }
        }

        "to TransportIdentityPage when 'A different vehicle' is selected and TransportIdentity has not been answered" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedUserAnswers = answers
                .set(TranshipmentTypePage(eventIndex), DifferentVehicle).success.value
                .remove(TransportIdentityPage(eventIndex)).success.value

              navigator
                .nextPage(TranshipmentTypePage(eventIndex), CheckMode, updatedUserAnswers)
                .mustBe(transhipmentRoutes.TransportIdentityController.onPageLoad(updatedUserAnswers.movementReferenceNumber, eventIndex, CheckMode))
          }
        }

        "to CheckEventAnswers when 'A different vehicle' is selected and TransportIdentity has been answered" in {
          forAll(arbitrary[UserAnswers], arbitrary[String]) {
            (answers, transportIdentity) =>
              val updatedUserAnswers = answers
                .set(TranshipmentTypePage(eventIndex), DifferentVehicle).success.value
                .set(TransportIdentityPage(eventIndex), transportIdentity).success.value

              navigator
                .nextPage(TranshipmentTypePage(eventIndex), CheckMode, updatedUserAnswers)
                .mustBe(eventRoutes.CheckEventAnswersController.onPageLoad(updatedUserAnswers.movementReferenceNumber, eventIndex))
          }
        }

        "to ContainerNumberPage when 'Both' is selected and ContainerNumber has not been answered" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedUserAnswers = answers
                .set(TranshipmentTypePage(eventIndex), DifferentContainerAndVehicle).success.value
                .remove(ContainerNumberPage(eventIndex, containerIndex)).success.value

              navigator
                .nextPage(TranshipmentTypePage(eventIndex), CheckMode, updatedUserAnswers)
                .mustBe(transhipmentRoutes.ContainerNumberController.onPageLoad(updatedUserAnswers.movementReferenceNumber, eventIndex, containerIndex, CheckMode))
          }
        }

        "to CheckEventAnswers when 'Both' is selected and ContainerNumber and vehicle identity and nationality questions have been answered" in {
          forAll(arbitrary[UserAnswers], arbitrary[ContainerDomain], arbitrary[String], arbitrary[CountryCode]) {
            (answers, container, transportIdentity, transportNationality) =>
              val updatedUserAnswers = answers
                .set(TranshipmentTypePage(eventIndex), DifferentContainerAndVehicle).success.value
                .set(ContainerNumberPage(eventIndex, containerIndex), container).success.value
                .set(TransportIdentityPage(eventIndex), transportIdentity).success.value
                .set(TransportNationalityPage(eventIndex), transportNationality).success.value
              navigator
                .nextPage(TranshipmentTypePage(eventIndex), CheckMode, updatedUserAnswers)
                .mustBe(eventRoutes.CheckEventAnswersController.onPageLoad(updatedUserAnswers.movementReferenceNumber, eventIndex))
          }
        }

        "to addContainerPage when 'Both' is selected and ContainerNumber has been answered but Transport Identity and Nationality has not been answered" in {
          forAll(arbitrary[UserAnswers], arbitrary[ContainerDomain]) {
            (answers, container) =>
              val updatedUserAnswers = answers
                .set(TranshipmentTypePage(eventIndex), DifferentContainerAndVehicle).success.value
                .set(ContainerNumberPage(eventIndex, containerIndex), container).success.value
                .remove(TransportIdentityPage(eventIndex)).success.value
                .remove(TransportNationalityPage(eventIndex)).success.value

              navigator
                .nextPage(TranshipmentTypePage(eventIndex), CheckMode, updatedUserAnswers)
                .mustBe(transhipmentRoutes.AddContainerController.onPageLoad(updatedUserAnswers.movementReferenceNumber, eventIndex, CheckMode))
          }
        }

        "to addContainerPage when 'Both' is selected and ContainerNumber has been answered but Transport Nationality has not been answered" in {
          forAll(arbitrary[UserAnswers], arbitrary[ContainerDomain]) {
            (answers, container) =>
              val updatedUserAnswers = answers
                .set(TranshipmentTypePage(eventIndex), DifferentContainerAndVehicle).success.value
                .set(ContainerNumberPage(eventIndex, containerIndex), container).success.value
                .remove(TransportNationalityPage(eventIndex)).success.value

              navigator
                .nextPage(TranshipmentTypePage(eventIndex), CheckMode, updatedUserAnswers)
                .mustBe(transhipmentRoutes.AddContainerController.onPageLoad(updatedUserAnswers.movementReferenceNumber, eventIndex, CheckMode))
          }
        }

        "to addContainerPage when 'Both' is selected and ContainerNumber has been answered but Transport Identity has not been answered" in {
          forAll(arbitrary[UserAnswers], arbitrary[ContainerDomain]) {
            (answers, container) =>
              val updatedUserAnswers = answers
                .set(TranshipmentTypePage(eventIndex), DifferentContainerAndVehicle).success.value
                .set(ContainerNumberPage(eventIndex, containerIndex), container).success.value
                .remove(TransportIdentityPage(eventIndex)).success.value

              navigator
                .nextPage(TranshipmentTypePage(eventIndex), CheckMode, updatedUserAnswers)
                .mustBe(transhipmentRoutes.AddContainerController.onPageLoad(updatedUserAnswers.movementReferenceNumber, eventIndex, CheckMode))
          }
        }

      }

      "must go from ContainerNumberPage" - {

        "to AddContainer" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              navigator
                .nextPage(ContainerNumberPage(eventIndex, containerIndex), CheckMode, answers)
                .mustBe(transhipmentRoutes.AddContainerController.onPageLoad(answers.movementReferenceNumber, eventIndex, CheckMode))
          }
        }

      }

      "must go from TransportIdentityPage" - {

        "to TransportNationalityPage when TransportNationality has not been answered" in {
          forAll(arbitrary[UserAnswers], arbitrary[String]) {
            (answers, transportIdentity) =>
              val updatedUserAnswers = answers
                .set(TransportIdentityPage(eventIndex), transportIdentity).success.value
                .remove(TransportNationalityPage(eventIndex)).success.value

              navigator
                .nextPage(TransportIdentityPage(eventIndex), CheckMode, updatedUserAnswers)
                .mustBe(transhipmentRoutes.TransportNationalityController.onPageLoad(answers.movementReferenceNumber, eventIndex, CheckMode))
          }
        }

        "to CheckEventAnswersPage when TransportNationality has been answered" in {
          forAll(arbitrary[UserAnswers], arbitrary[String], arbitrary[CountryCode]) {
            (answers, transportIdentity, transportNationality) =>
              val updatedUserAnswers = answers
                .set(TransportIdentityPage(eventIndex), transportIdentity).success.value
                .set(TransportNationalityPage(eventIndex), transportNationality).success.value

              navigator
                .nextPage(TransportIdentityPage(eventIndex), CheckMode, updatedUserAnswers)
                .mustBe(eventRoutes.CheckEventAnswersController.onPageLoad(answers.movementReferenceNumber, eventIndex))
          }
        }
      }

      "must go from TransportNationality" - {

        "to CheckEventAnswers" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              navigator
                .nextPage(TransportNationalityPage(eventIndex), CheckMode, answers)
                .mustBe(eventRoutes.CheckEventAnswersController.onPageLoad(answers.movementReferenceNumber, eventIndex))
          }
        }
      }

      "must go from AddContainerPage" - {
        "to CheckEventAnswers when false and the TranshipmentTypePage is 'A different container'" in {

          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers = answers
                .set(TranshipmentTypePage(eventIndex), DifferentContainer).success.value
                .set(AddContainerPage(eventIndex), false).success.value

              navigator
                .nextPage(AddContainerPage(eventIndex), CheckMode, updatedAnswers)
                .mustBe(eventRoutes.CheckEventAnswersController.onPageLoad(updatedAnswers.movementReferenceNumber, eventIndex))
          }
        }

        "to TransportIdentityPage when false, TranshipmentTypePage is 'Both' and TransportIdentity has not been answered" in {

          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers = answers
                .set(TranshipmentTypePage(eventIndex), DifferentContainerAndVehicle).success.value
                .set(AddContainerPage(eventIndex), false).success.value
                .remove(TransportIdentityPage(eventIndex)).success.value

              navigator
                .nextPage(AddContainerPage(eventIndex), CheckMode, updatedAnswers)
                .mustBe(transhipmentRoutes.TransportIdentityController.onPageLoad(updatedAnswers.movementReferenceNumber, eventIndex, CheckMode))
          }
        }

        "to CheckEventAnswers when false, TranshipmentTypePage is 'Both' and TransportIdentity has been answered" in {

          forAll(arbitrary[UserAnswers], arbitrary[String]) {
            (answers, transportIdentity) =>
              val updatedAnswers = answers
                .set(TranshipmentTypePage(eventIndex), DifferentContainerAndVehicle).success.value
                .set(AddContainerPage(eventIndex), false).success.value
                .set(TransportIdentityPage(eventIndex), transportIdentity).success.value

              navigator
                .nextPage(AddContainerPage(eventIndex), CheckMode, updatedAnswers)
                .mustBe(eventRoutes.CheckEventAnswersController.onPageLoad(updatedAnswers.movementReferenceNumber, eventIndex))
          }
        }

        "to ContainerNumber page in when true with the index increased" in {
          val nextIndex = Index(containerIndex.position + 1)
          forAll(arbitrary[UserAnswers], arbitrary[ContainerDomain]) {
            (answers, container) =>
              val updatedAnswers = answers
                .set(ContainerNumberPage(eventIndex, containerIndex), container).success.value
                .set(AddContainerPage(eventIndex), true).success.value

              navigator
                .nextPage(AddContainerPage(eventIndex), CheckMode, updatedAnswers)
                .mustBe(transhipmentRoutes.ContainerNumberController.onPageLoad(updatedAnswers.movementReferenceNumber, eventIndex, nextIndex, CheckMode))
          }
        }

      }

      "seals page" - {

        "must go from seals identity page to add seals page" in {
          forAll(arbitrary[UserAnswers], arbitrary[SealDomain]) {
            (answers, seal) =>
              val updatedAnswers = answers.set(SealIdentityPage(eventIndex, sealIndex), seal).success.value

              navigator
                .nextPage(SealIdentityPage(eventIndex, sealIndex), CheckMode, updatedAnswers)
                .mustBe(sealRoutes.AddSealController.onPageLoad(answers.movementReferenceNumber, eventIndex, CheckMode))
          }
        }

        "must go from have seals changed page to check event answers page when the answer is 'No'" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers = answers.set(HaveSealsChangedPage(eventIndex), false).success.value

              navigator
                .nextPage(HaveSealsChangedPage(eventIndex), CheckMode, updatedAnswers)
                .mustBe(eventRoutes.CheckEventAnswersController.onPageLoad(answers.movementReferenceNumber, eventIndex))
          }
        }

        "must go from have seals changed page to seal identity page page when the answer is 'Yes'" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers = answers
                .set(HaveSealsChangedPage(eventIndex), true).success.value
                .remove(SealIdentityPage(eventIndex, sealIndex)).success.value

              navigator
                .nextPage(HaveSealsChangedPage(eventIndex), CheckMode, updatedAnswers)
                .mustBe(sealRoutes.SealIdentityController.onPageLoad(answers.movementReferenceNumber, eventIndex, sealIndex, CheckMode))
          }
        }

        "go from addSealPage to sealIdentity when Yes is selected" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers = answers
                .remove(SealIdentityPage(eventIndex, sealIndex)).success.value
                .set(AddSealPage(eventIndex), true).success.value

              navigator
                .nextPage(AddSealPage(eventIndex), CheckMode, updatedAnswers)
                .mustBe(sealRoutes.SealIdentityController.onPageLoad(answers.movementReferenceNumber, eventIndex, sealIndex, CheckMode))
          }
        }

        "go from addSealPage to checkEventAnswers when No is selected" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers = answers
                .set(AddSealPage(eventIndex), false).success.value

              navigator
                .nextPage(AddSealPage(eventIndex), CheckMode, updatedAnswers)
                .mustBe(eventRoutes.CheckEventAnswersController.onPageLoad(answers.movementReferenceNumber, eventIndex))
          }
        }
      }

      "must go from 'IsTraderAddressPlaceOfNotificationPage'" - {
        "to 'Check Your Answers' when answer is 'No' and there is a 'Place of notification'" in {
          forAll(arbitrary[UserAnswers], arbitrary[String]) {
            (answers, placeOfNotification) =>
              val updatedUserAnswers = answers
                .set(IsTraderAddressPlaceOfNotificationPage, false).success.value
                .set(PlaceOfNotificationPage, placeOfNotification).success.value

              navigator
                .nextPage(IsTraderAddressPlaceOfNotificationPage, CheckMode, updatedUserAnswers)
                .mustBe(routes.CheckYourAnswersController.onPageLoad(updatedUserAnswers.movementReferenceNumber))
          }
        }

        "to 'Place of notification' when answer is 'No' and there is no existing 'Place of notification'" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedUserAnswers = answers
                .set(IsTraderAddressPlaceOfNotificationPage, false).success.value
                .remove(PlaceOfNotificationPage).success.value

              navigator
                .nextPage(IsTraderAddressPlaceOfNotificationPage, CheckMode, updatedUserAnswers)
                .mustBe(routes.PlaceOfNotificationController.onPageLoad(updatedUserAnswers.movementReferenceNumber, CheckMode))
          }
        }

        "to 'Check Your Answers' when answer is 'Yes'" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedUserAnswers = answers.set(IsTraderAddressPlaceOfNotificationPage, true).success.value

              navigator
                .nextPage(IsTraderAddressPlaceOfNotificationPage, CheckMode, updatedUserAnswers)
                .mustBe(routes.CheckYourAnswersController.onPageLoad(updatedUserAnswers.movementReferenceNumber))
          }
        }
      }

      "go from 'Place of Notification' to CheckYourAnswer" in {
        import models.domain.NormalNotification.Constants.notificationPlaceLength

        forAll(arbitrary[UserAnswers], stringsWithMaxLength(notificationPlaceLength)) {
          case (answers, placeOfNotification) =>
            val updatedUserAnswers = answers.set(PlaceOfNotificationPage, placeOfNotification).success.value

            navigator
              .nextPage(PlaceOfNotificationPage, CheckMode, updatedUserAnswers)
              .mustBe(routes.CheckYourAnswersController.onPageLoad(updatedUserAnswers.movementReferenceNumber))
        }
      }

      "must go from Confirm remove container page" - {

        "to Add container page when containers exist" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers = answers
                .remove(EventsQuery).success.value
                .set(EventCountryPage(eventIndex), country).success.value
                .set(EventPlacePage(eventIndex), "place name").success.value
                .set(EventReportedPage(eventIndex), true).success.value
                .set(IsTranshipmentPage(eventIndex), true).success.value
                .set(TranshipmentTypePage(eventIndex), DifferentContainer).success.value
                .set(ContainerNumberPage(eventIndex, containerIndex), ContainerDomain("1")).success.value
              navigator
                .nextPage(ConfirmRemoveContainerPage(eventIndex), CheckMode, updatedAnswers)
                .mustBe(transhipmentRoutes.AddContainerController.onPageLoad(updatedAnswers.movementReferenceNumber, eventIndex, CheckMode))
          }
        }

        "to isTranshipment page when no containers exist" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers = answers
                .remove(EventsQuery).success.value
                .set(EventCountryPage(eventIndex), country).success.value
                .set(EventPlacePage(eventIndex), "place name").success.value
                .set(EventReportedPage(eventIndex), true).success.value
                .set(IsTranshipmentPage(eventIndex), true).success.value
                .set(TranshipmentTypePage(eventIndex), DifferentContainer).success.value
              navigator
                .nextPage(ConfirmRemoveContainerPage(eventIndex), CheckMode, updatedAnswers)
                .mustBe(eventRoutes.IsTranshipmentController.onPageLoad(updatedAnswers.movementReferenceNumber, eventIndex, CheckMode))
          }
        }

        "must go from incident on route page" - {

          "to event country page when user selects yes" in {
            forAll(arbitrary[UserAnswers]) {
              answers =>
                val updatedAnswers = answers
                  .remove(IncidentOnRoutePage).success.value
                  .set(IncidentOnRoutePage, true).success.value
                navigator
                  .nextPage(IncidentOnRoutePage, CheckMode, updatedAnswers)
                  .mustBe(eventRoutes.EventCountryController.onPageLoad(answers.movementReferenceNumber, eventIndex, NormalMode))

            }
          }
        }
      }
    }
  }
}