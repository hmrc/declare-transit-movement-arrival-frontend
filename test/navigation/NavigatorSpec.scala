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
import controllers.events.seals.{routes => sealRoutes}
import controllers.events.transhipments.{routes => transhipmentRoutes}
import controllers.events.{routes => eventRoutes}
import controllers.routes
import generators.{Generators, MessagesModelGenerators}
import models.TranshipmentType.{DifferentContainer, DifferentContainerAndVehicle, DifferentVehicle}
import models._
import models.messages.{Container, EnRouteEvent, Seal}
import models.reference.Country
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._
import pages.events._
import pages.events.seals._
import pages.events.transhipments._
import queries.EventsQuery

class NavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators with MessagesModelGenerators {

  val navigator: Navigator = app.injector.instanceOf[Navigator]

  val country: Country = Country("Valid", "GB", "United Kingdom")

  "Navigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {

        case object UnknownPage extends Page

        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(UnknownPage, NormalMode, answers)
              .mustBe(routes.MovementReferenceNumberController.onPageLoad())
        }
      }

      "must go from movement reference number to 'Good location' page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(MovementReferenceNumberPage, NormalMode, answers)
              .mustBe(routes.GoodsLocationController.onPageLoad(answers.id, NormalMode))
        }

      }

      "must go from 'goods location' to  'customs approved location' when user chooses 'Border Force Office'" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers = answers.set(GoodsLocationPage, GoodsLocation.BorderForceOffice).success.value

            navigator
              .nextPage(GoodsLocationPage, NormalMode, updatedAnswers)
              .mustBe(routes.CustomsSubPlaceController.onPageLoad(updatedAnswers.id, NormalMode))
        }
      }

      "must go from 'customs approved location' to  'presentation office'" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(CustomsSubPlacePage, NormalMode, answers)
              .mustBe(routes.PresentationOfficeController.onPageLoad(answers.id, NormalMode))
        }
      }

      "must go from 'goods location' to  'use different service' when user chooses 'Authorised consignee’s location'" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            val updatedAnswers = answers.set(GoodsLocationPage, GoodsLocation.AuthorisedConsigneesLocation).success.value

            navigator
              .nextPage(GoodsLocationPage, NormalMode, updatedAnswers)
              .mustBe(routes.UseDifferentServiceController.onPageLoad(updatedAnswers.id))
        }
      }

      "must go from 'presentation office' to  'traders name'" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(PresentationOfficePage, NormalMode, answers)
              .mustBe(routes.TraderNameController.onPageLoad(answers.id, NormalMode))
        }
      }

      "must go from 'traders name' to 'traders eori'" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(TraderNamePage, NormalMode, answers)
              .mustBe(routes.TraderEoriController.onPageLoad(answers.id, NormalMode))
        }
      }

      "must go from 'traders address' to 'IsTraderAddressPlaceOfNotificationController'" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(TraderAddressPage, NormalMode, answers)
              .mustBe(routes.IsTraderAddressPlaceOfNotificationController.onPageLoad(answers.id, NormalMode))
        }
      }

      "must go from 'traders eori' to 'traders address'" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(TraderEoriPage, NormalMode, answers)
              .mustBe(routes.TraderAddressController.onPageLoad(answers.id, NormalMode))
        }
      }

      "must go from 'IsTraderAddressPlaceOfNotificationPage'" - {
        "to 'IncidentOnRoutePage' when answer is 'Yes'" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedUserAnswers = answers.set(IsTraderAddressPlaceOfNotificationPage, true).success.value

              navigator
                .nextPage(IsTraderAddressPlaceOfNotificationPage, NormalMode, updatedUserAnswers)
                .mustBe(routes.IncidentOnRouteController.onPageLoad(updatedUserAnswers.id, NormalMode))
          }
        }

        "to 'IncidentOnRoutePage' when answer is 'No'" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedUserAnswers = answers.set(IsTraderAddressPlaceOfNotificationPage, false).success.value

              navigator
                .nextPage(IsTraderAddressPlaceOfNotificationPage, NormalMode, updatedUserAnswers)
                .mustBe(routes.PlaceOfNotificationController.onPageLoad(updatedUserAnswers.id, NormalMode))
          }

        }

      }

      "go from 'Place of Notification' to 'IncidentOnRoute'" in {
        forAll(arbitrary[UserAnswers], stringsWithMaxLength(35)) {
          (answers, placeOfNotification) =>
            val updatedUserAnswers = answers.set(PlaceOfNotificationPage, placeOfNotification).success.value

            navigator
              .nextPage(PlaceOfNotificationPage, NormalMode, updatedUserAnswers)
              .mustBe(routes.IncidentOnRouteController.onPageLoad(updatedUserAnswers.id, NormalMode))
        }
      }

      "must go from 'incident on route'" - {

        "to 'check your answers' when the user answers no" in {

          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers = answers.set(IncidentOnRoutePage, false).success.value

              navigator
                .nextPage(IncidentOnRoutePage, NormalMode, updatedAnswers)
                .mustBe(routes.CheckYourAnswersController.onPageLoad(answers.id))
          }
        }

        "must go to AddEvent if existing events" in {

          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers = {
                answers
                  .set(IncidentOnRoutePage, true)
                  .success
                  .value
                  .set(EventCountryPage(eventIndex), country)
                  .success
                  .value
                  .set(EventPlacePage(eventIndex), "TestPlace")
                  .success
                  .value
                  .set(EventReportedPage(eventIndex), true)
                  .success
                  .value
                  .set(IsTranshipmentPage(eventIndex), false)
                  .success
                  .value
              }

              navigator
                .nextPage(IncidentOnRoutePage, NormalMode, updatedAnswers)
                .mustBe(eventRoutes.AddEventController.onPageLoad(answers.id, NormalMode))
          }
        }

        "must go to EventCountry if no events" in {

          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers = {
                answers
                  .set(IncidentOnRoutePage, true)
                  .success
                  .value
                  .remove(EventsQuery)
                  .success
                  .value
              }

              navigator
                .nextPage(IncidentOnRoutePage, NormalMode, updatedAnswers)
                .mustBe(eventRoutes.EventCountryController.onPageLoad(answers.id, eventIndex, NormalMode))
          }
        }

        "to Session Expired when we cannot tell if an event happened on route" in {

          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers = answers.remove(IncidentOnRoutePage).success.value

              navigator
                .nextPage(IncidentOnRoutePage, NormalMode, updatedAnswers)
                .mustBe(routes.SessionExpiredController.onPageLoad())
          }
        }
      }

      "must go from Event Country to Event Place" in {

        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(EventCountryPage(eventIndex), NormalMode, answers)
              .mustBe(eventRoutes.EventPlaceController.onPageLoad(answers.id, eventIndex, NormalMode))
        }
      }

      "must go from Event Place to Event Reported" in {

        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(EventPlacePage(eventIndex), NormalMode, answers)
              .mustBe(eventRoutes.EventReportedController.onPageLoad(answers.id, eventIndex, NormalMode))
        }
      }

      "must go from Event Reported to Is Transhipment" in {

        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(EventReportedPage(eventIndex), NormalMode, answers)
              .mustBe(eventRoutes.IsTranshipmentController.onPageLoad(answers.id, eventIndex, NormalMode))
        }
      }

      "must go from Is Transhipment" - {

        "to Incident Information when the event has not been reported and transhipment as 'No'" in {

          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers = answers
                .set(EventReportedPage(eventIndex), false)
                .success
                .value
                .set(IsTranshipmentPage(eventIndex), false)
                .success
                .value

              navigator
                .nextPage(IsTranshipmentPage(eventIndex), NormalMode, updatedAnswers)
                .mustBe(eventRoutes.IncidentInformationController.onPageLoad(updatedAnswers.id, eventIndex, NormalMode))
          }
        }

        "to have seals changed Page when the event has been reported and Transhipment as 'No'" in {

          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers = answers
                .set(EventReportedPage(eventIndex), true)
                .success
                .value
                .set(IsTranshipmentPage(eventIndex), false)
                .success
                .value

              navigator
                .nextPage(IsTranshipmentPage(eventIndex), NormalMode, updatedAnswers)
                .mustBe(sealRoutes.HaveSealsChangedController.onPageLoad(updatedAnswers.id, eventIndex, NormalMode))
          }
        }

        "to transhipment type page when Transhipment is 'Yes'" in {

          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers = answers
                .set(IsTranshipmentPage(eventIndex), true)
                .success
                .value

              navigator
                .nextPage(IsTranshipmentPage(eventIndex), NormalMode, updatedAnswers)
                .mustBe(transhipmentRoutes.TranshipmentTypeController.onPageLoad(updatedAnswers.id, eventIndex, NormalMode))
          }
        }

        "to Session Expired when we cannot tell if the event has been reported or if Transhipment is selected" in {

          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers = answers
                .remove(EventReportedPage(eventIndex))
                .success
                .value
                .remove(IsTranshipmentPage(eventIndex))
                .success
                .value

              navigator
                .nextPage(IsTranshipmentPage(eventIndex), NormalMode, updatedAnswers)
                .mustBe(routes.SessionExpiredController.onPageLoad())
          }
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
                .nextPage(ConfirmRemoveContainerPage(eventIndex), NormalMode, updatedAnswers)
                .mustBe(transhipmentRoutes.AddContainerController.onPageLoad(updatedAnswers.id, eventIndex, NormalMode))
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
                .nextPage(ConfirmRemoveContainerPage(eventIndex), NormalMode, updatedAnswers)
                .mustBe(eventRoutes.IsTranshipmentController.onPageLoad(updatedAnswers.id, eventIndex, NormalMode))
          }
        }
      }

      "must go from Transhipment type" - {

        "to Transport Identity when option is 'a different vehicle' " in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers = answers
                .set(TranshipmentTypePage(eventIndex), DifferentVehicle)
                .success
                .value

              navigator
                .nextPage(TranshipmentTypePage(eventIndex), NormalMode, updatedAnswers)
                .mustBe(transhipmentRoutes.TransportIdentityController.onPageLoad(updatedAnswers.id, eventIndex, NormalMode))
          }
        }

        "to ContainerNumber when option is 'a different container' and there are no containers" in {
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
                .nextPage(TranshipmentTypePage(eventIndex), NormalMode, updatedAnswers)
                .mustBe(transhipmentRoutes.ContainerNumberController.onPageLoad(updatedAnswers.id, eventIndex, containerIndex, NormalMode))
          }
        }

        "to Add Container when option is 'a different container' and there is one container" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers = answers
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
                .nextPage(TranshipmentTypePage(eventIndex), NormalMode, updatedAnswers)
                .mustBe(transhipmentRoutes.AddContainerController.onPageLoad(updatedAnswers.id, eventIndex, NormalMode))
          }
        }

        "to ContainerNumber when option is 'both' and there is a no container " in {
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
                .set(TranshipmentTypePage(eventIndex), DifferentContainerAndVehicle)
                .success
                .value

              navigator
                .nextPage(TranshipmentTypePage(eventIndex), NormalMode, updatedAnswers)
                .mustBe(transhipmentRoutes.ContainerNumberController.onPageLoad(updatedAnswers.id, eventIndex, containerIndex, NormalMode))
          }
        }

        "to Add Container when option is 'both' and there is a single container " in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers = answers
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
                .set(TranshipmentTypePage(eventIndex), DifferentContainerAndVehicle)
                .success
                .value
                .set(ContainerNumberPage(eventIndex, containerIndex), Container("number1"))
                .success
                .value

              navigator
                .nextPage(TranshipmentTypePage(eventIndex), NormalMode, updatedAnswers)
                .mustBe(transhipmentRoutes.AddContainerController.onPageLoad(updatedAnswers.id, eventIndex, NormalMode))
          }
        }
      }

      "must go from transport identity to Transport Nationality page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(TransportIdentityPage(eventIndex), NormalMode, answers)
              .mustBe(transhipmentRoutes.TransportNationalityController.onPageLoad(answers.id, eventIndex, NormalMode))
        }
      }

      "must go from transport nationality to have seals changed page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(TransportNationalityPage(eventIndex), NormalMode, answers)
              .mustBe(sealRoutes.HaveSealsChangedController.onPageLoad(answers.id, eventIndex, NormalMode))
        }
      }

      "must go from Incident Information to have seals changed page" in {

        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(IncidentInformationPage(eventIndex), NormalMode, answers)
              .mustBe(sealRoutes.HaveSealsChangedController.onPageLoad(answers.id, eventIndex, NormalMode))
        }
      }

      "must go from container number page to 'Add another container'" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(ContainerNumberPage(eventIndex, containerIndex), NormalMode, answers)
              .mustBe(transhipmentRoutes.AddContainerController.onPageLoad(answers.id, eventIndex, NormalMode))
        }
      }

      "must go from 'Add another container'" - {

        "to 'transport identity' when the option is 'No' and transhipment type is both" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers = answers
                .set(TranshipmentTypePage(eventIndex), DifferentContainerAndVehicle)
                .success
                .value
                .set(AddContainerPage(eventIndex), false)
                .success
                .value

              navigator
                .nextPage(AddContainerPage(eventIndex), NormalMode, updatedAnswers)
                .mustBe(transhipmentRoutes.TransportIdentityController.onPageLoad(answers.id, eventIndex, NormalMode))
          }
        }

        "to have seals changed page when the option is 'No' and transhipment type is not both" in {

          val transhipmentType: Gen[WithName with TranshipmentType] = Gen.oneOf(Seq(DifferentContainer, DifferentVehicle))
          forAll(arbitrary[UserAnswers], transhipmentType) {
            (answers, transhipment) =>
              val updatedAnswers = answers
                .set(TranshipmentTypePage(eventIndex), transhipment)
                .success
                .value
                .set(AddContainerPage(eventIndex), false)
                .success
                .value

              navigator
                .nextPage(AddContainerPage(eventIndex), NormalMode, updatedAnswers)
                .mustBe(sealRoutes.HaveSealsChangedController.onPageLoad(answers.id, eventIndex, NormalMode))
          }
        }

        "to 'Container number' with index 0 when the option is 'Yes' and there are no previous containers" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers = answers
                .remove(EventsQuery)
                .success
                .value
                .set(AddContainerPage(eventIndex), true)
                .success
                .value

              navigator
                .nextPage(AddContainerPage(eventIndex), NormalMode, updatedAnswers)
                .mustBe(transhipmentRoutes.ContainerNumberController.onPageLoad(answers.id, eventIndex, containerIndex, NormalMode))
          }
        }

        "to 'Container number' with index 1 when the option is 'Yes' and there is 1 previous containers" in {
          val nextIndex = Index(containerIndex.position + 1)

          forAll(arbitrary[UserAnswers], arbitrary[Container]) {
            case (answers, container) =>
              val updatedAnswers = answers
                .remove(EventsQuery)
                .success
                .value
                .set(ContainerNumberPage(eventIndex, containerIndex), container)
                .success
                .value
                .set(AddContainerPage(eventIndex), true)
                .success
                .value

              navigator
                .nextPage(AddContainerPage(eventIndex), NormalMode, updatedAnswers)
                .mustBe(transhipmentRoutes.ContainerNumberController.onPageLoad(answers.id, eventIndex, nextIndex, NormalMode))
          }
        }
      }

      "must go from Confirm Remove Event Page" - {

        "to Add Event Page when user selects 'No'" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers = {
                answers
                  .set(IncidentOnRoutePage, true)
                  .success
                  .value
                  .set(EventCountryPage(eventIndex), country)
                  .success
                  .value
                  .set(EventPlacePage(eventIndex), "TestPlace")
                  .success
                  .value
                  .set(EventReportedPage(eventIndex), true)
                  .success
                  .value
                  .set(IsTranshipmentPage(eventIndex), false)
                  .success
                  .value
              }
              navigator
                .nextPage(ConfirmRemoveEventPage(eventIndex), NormalMode, updatedAnswers)
                .mustBe(eventRoutes.AddEventController.onPageLoad(answers.id, NormalMode))
          }
        }

        "to Add Event Page when user selects 'Yes' and events still exist" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers = {
                answers
                  .set(IncidentOnRoutePage, true)
                  .success
                  .value
                  .set(EventCountryPage(eventIndex), country)
                  .success
                  .value
                  .set(EventPlacePage(eventIndex), "TestPlace")
                  .success
                  .value
                  .set(EventReportedPage(eventIndex), true)
                  .success
                  .value
                  .set(IsTranshipmentPage(eventIndex), false)
                  .success
                  .value
              }
              navigator
                .nextPage(ConfirmRemoveEventPage(eventIndex), NormalMode, updatedAnswers)
                .mustBe(eventRoutes.AddEventController.onPageLoad(answers.id, NormalMode))
          }
        }

        "to Incident on Route Page when user selects 'Yes' and no event exists" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val withoutEvents = answers.remove(EventsQuery).success.value
              navigator
                .nextPage(ConfirmRemoveEventPage(eventIndex), NormalMode, withoutEvents)
                .mustBe(routes.IncidentOnRouteController.onPageLoad(answers.id, NormalMode))
          }
        }
      }

      "must go from Add Event Page" - {
        "when user selects 'Yes' to" - {

          "Event Country Page with index 0 when there are no events" in {
            forAll(arbitrary[UserAnswers]) {
              answers =>
                val withoutEvents = answers.remove(EventsQuery).success.value

                val updatedAnswers = withoutEvents.set(AddEventPage, true).success.value

                navigator
                  .nextPage(AddEventPage, NormalMode, updatedAnswers)
                  .mustBe(eventRoutes.EventCountryController.onPageLoad(answers.id, eventIndex, NormalMode))
            }
          }

          "Event Country Page with index 1 when there is 1 event" in {
            forAll(arbitrary[EnRouteEvent], stringsWithMaxLength(350)) {
              case (EnRouteEvent(place, countryCode, _, _, _), information) =>
                val updatedAnswers = emptyUserAnswers
                  .set(IncidentOnRoutePage, true)
                  .success
                  .value
                  .set(EventCountryPage(eventIndex), Country("Valid", countryCode, "Some country"))
                  .success
                  .value
                  .set(EventPlacePage(eventIndex), place)
                  .success
                  .value
                  .set(EventReportedPage(eventIndex), false)
                  .success
                  .value
                  .set(IsTranshipmentPage(eventIndex), false)
                  .success
                  .value
                  .set(IncidentInformationPage(eventIndex), information)
                  .success
                  .value
                  .set(AddEventPage, true)
                  .success
                  .value

                navigator
                  .nextPage(AddEventPage, NormalMode, updatedAnswers)
                  .mustBe(eventRoutes.EventCountryController.onPageLoad(emptyUserAnswers.id, Index(1), NormalMode))
            }
          }
        }

        "to check your answers page when user selects option 'No' on add event page" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers = answers.set(AddEventPage, false).success.value

              navigator
                .nextPage(AddEventPage, NormalMode, updatedAnswers)
                .mustBe(routes.CheckYourAnswersController.onPageLoad(answers.id))
          }
        }
      }

      "must go from Have seals changed page" - {

        "to check event answers page when user selects No" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers = answers.set(HaveSealsChangedPage(eventIndex), false).success.value

              navigator
                .nextPage(HaveSealsChangedPage(eventIndex), NormalMode, updatedAnswers)
                .mustBe(eventRoutes.CheckEventAnswersController.onPageLoad(answers.id, eventIndex))
          }
        }

        "to seal identity page when user selects Yes" in {
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
                .nextPage(HaveSealsChangedPage(eventIndex), NormalMode, updatedAnswers)
                .mustBe(sealRoutes.SealIdentityController.onPageLoad(answers.id, eventIndex, sealIndex, NormalMode))
          }
        }

        "to 'add seal page'" in {
          forAll(arbitrary[UserAnswers], arbitrary[Seal]) {
            (answers, seal) =>
              val updatedAnswers = answers
                .set(SealIdentityPage(eventIndex, sealIndex), seal)
                .success
                .value
                .set(HaveSealsChangedPage(eventIndex), true)
                .success
                .value

              navigator
                .nextPage(HaveSealsChangedPage(eventIndex), NormalMode, updatedAnswers)
                .mustBe(sealRoutes.AddSealController.onPageLoad(answers.id, eventIndex, NormalMode))
          }
        }
      }

      "must go from seals identity page" - {

        "to check event answers page" in {
          forAll(arbitrary[UserAnswers], arbitrary[Seal]) {
            (answers, seal) =>
              val updatedAnswers = answers.set(SealIdentityPage(eventIndex, sealIndex), seal).success.value

              navigator
                .nextPage(SealIdentityPage(eventIndex, sealIndex), NormalMode, updatedAnswers)
                .mustBe(sealRoutes.AddSealController.onPageLoad(answers.id, eventIndex, NormalMode))
          }
        }
      }

      "must go from add seal page" - {

        "to check event details page when answer is no" in {
          forAll(arbitrary[UserAnswers]) {
            answers =>
              val updatedAnswers = answers.set(AddSealPage(eventIndex), false).success.value

              navigator
                .nextPage(AddSealPage(eventIndex), NormalMode, updatedAnswers)
                .mustBe(eventRoutes.CheckEventAnswersController.onPageLoad(answers.id, eventIndex))

          }
        }

        "to seal identity page when answer is Yes" in {
          val nextIndex = Index(sealIndex.position + 1)
          val updatedAnswers = emptyUserAnswers
            .set(AddSealPage(eventIndex), true)
            .success
            .value
            .set(SealIdentityPage(eventIndex, sealIndex), Seal("seal1"))
            .success
            .value

          navigator
            .nextPage(AddSealPage(eventIndex), NormalMode, updatedAnswers)
            .mustBe(sealRoutes.SealIdentityController.onPageLoad(updatedAnswers.id, eventIndex, nextIndex, NormalMode))
        }
      }

      "go from remove seals page" - {
        "add seals page when 'Yes' is selected and there are still seals" in {
          forAll(arbitrary[UserAnswers], arbitrary[Seal]) {
            case (userAnswers, seal) =>
              val updatedAnswers = userAnswers
                .set(SealIdentityPage(eventIndex, sealIndex), seal)
                .success
                .value

              navigator
                .nextPage(ConfirmRemoveSealPage(eventIndex), NormalMode, updatedAnswers)
                .mustBe(sealRoutes.AddSealController.onPageLoad(updatedAnswers.id, eventIndex, NormalMode))
          }

        }

        "have seals changed page when 'Yes' is selected and all seals are removed" in {
          forAll(arbitrary[UserAnswers]) {
            case (userAnswers) =>
              val updatedAnswers = userAnswers
                .remove(EventsQuery)
                .success
                .value

              navigator
                .nextPage(ConfirmRemoveSealPage(eventIndex), NormalMode, updatedAnswers)
                .mustBe(sealRoutes.HaveSealsChangedController.onPageLoad(updatedAnswers.id, eventIndex, NormalMode))
          }

        }
      }
    }

    "nextRejectionPage" - {

      "must go from UpdateRejectionMovementReferenceNumber page to CheckYourAnswersRejection page" in {
        navigator
          .nextRejectionPage(UpdateRejectedMovementReferenceNumberPage, mrn, ArrivalId(1))
          .mustBe(routes.CheckYourAnswersRejectionsController.onPageLoad(mrn, ArrivalId(1)))
      }
    }

  }

}
