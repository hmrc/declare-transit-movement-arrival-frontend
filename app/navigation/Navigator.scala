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

import com.google.inject.{Inject, Singleton}
import controllers.events.seals.{routes => sealRoutes}
import controllers.events.transhipments.{routes => transhipmentRoutes}
import controllers.events.{routes => eventRoutes}
import controllers.routes
import derivable.{DeriveNumberOfContainers, DeriveNumberOfEvents, DeriveNumberOfSeals}
import models.GoodsLocation._
import models.TranshipmentType.{DifferentContainer, DifferentContainerAndVehicle, DifferentVehicle}
import models.{ArrivalId, CheckMode, Index, Mode, MovementReferenceNumber, NormalMode, UserAnswers}
import pages._
import pages.events._
import pages.events.seals._
import pages.events.transhipments._
import play.api.mvc.Call

@Singleton
class Navigator @Inject()() {

  // format: off
  private val normalRoutes: PartialFunction[Page, UserAnswers => Option[Call]] = {
    case MovementReferenceNumberPage => ua => Some(routes.GoodsLocationController.onPageLoad(ua.id, NormalMode))
    case GoodsLocationPage => ua => Some(routes.CustomsSubPlaceController.onPageLoad(ua.id, NormalMode))
    case AuthorisedLocationPage => ua => Some(routes.ConsigneeNameController.onPageLoad(ua.id, NormalMode))
    case ConsigneeNamePage => ua => Some(routes.ConsigneeEoriConfirmationController.onPageLoad(ua.id, NormalMode))
    case ConsigneeEoriConfirmationPage => consigneeEoriConfirmationRoute(NormalMode)
    case ConsigneeEoriNumberPage => ua => Some(routes.ConsigneeAddressController.onPageLoad(ua.id, NormalMode))
    case ConsigneeAddressPage => ua => Some(routes.PresentationOfficeController.onPageLoad(ua.id, NormalMode))
    case PresentationOfficePage => presentationOfficeRoute(NormalMode)
    case CustomsSubPlacePage => ua => Some(routes.PresentationOfficeController.onPageLoad(ua.id, NormalMode))
    case TraderNamePage => ua => Some(routes.TraderEoriController.onPageLoad(ua.id, NormalMode))
    case TraderAddressPage => ua => Some(routes.IsTraderAddressPlaceOfNotificationController.onPageLoad(ua.id, NormalMode))
    case TraderEoriPage => ua => Some(routes.TraderAddressController.onPageLoad(ua.id, NormalMode))
    case IsTraderAddressPlaceOfNotificationPage => isTraderAddressPlaceOfNotificationRoute
    case PlaceOfNotificationPage => ua => Some(routes.IncidentOnRouteController.onPageLoad(ua.id, NormalMode))
    case IncidentOnRoutePage => incidentOnRoute
    case EventCountryPage(eventIndex) => ua => Some(eventRoutes.EventPlaceController.onPageLoad(ua.id, eventIndex, NormalMode))
    case EventPlacePage(eventIndex) => ua => Some(eventRoutes.EventReportedController.onPageLoad(ua.id, eventIndex, NormalMode))
    case EventReportedPage(eventIndex) => ua => Some(eventRoutes.IsTranshipmentController.onPageLoad(ua.id, eventIndex, NormalMode))
    case IsTranshipmentPage(eventIndex) => isTranshipmentRoute(eventIndex)
    case IncidentInformationPage(eventIndex) => ua => Some(sealRoutes.HaveSealsChangedController.onPageLoad(ua.id, eventIndex, NormalMode))
    case AddEventPage => addEventRoute
    case TranshipmentTypePage(eventIndex) => transhipmentType(eventIndex)
    case TransportIdentityPage(eventIndex) => ua => Some(transhipmentRoutes.TransportNationalityController.onPageLoad(ua.id, eventIndex, NormalMode))
    case TransportNationalityPage(eventIndex) => ua => Some(sealRoutes.HaveSealsChangedController.onPageLoad(ua.id, eventIndex, NormalMode))
    case ContainerNumberPage(eventIndex, _) => ua => Some(transhipmentRoutes.AddContainerController.onPageLoad(ua.id, eventIndex, NormalMode))
    case AddContainerPage(eventIndex) => addContainer(eventIndex)
    case ConfirmRemoveContainerPage(eventIndex) => confirmRemoveContainerRoute(eventIndex, NormalMode)
    case ConfirmRemoveEventPage(eventIndex) => confirmRemoveEventRoute(eventIndex, NormalMode)
    case HaveSealsChangedPage(eventIndex) => haveSealsChanged(eventIndex, NormalMode)
    case SealIdentityPage(eventIndex, _) => ua => Some(sealRoutes.AddSealController.onPageLoad(ua.id, eventIndex, NormalMode))
    case AddSealPage(eventIndex) => addSeal(eventIndex, NormalMode)
    case ConfirmRemoveSealPage(eventIndex) => removeSeal(eventIndex, NormalMode)
  }

  private val checkRouteMap: PartialFunction[Page, UserAnswers => Option[Call]] = {
    case GoodsLocationPage => goodsLocationCheckRoute
    case AuthorisedLocationPage  => authorisedLocationRoute(CheckMode)
    case CustomsSubPlacePage => customsSubPlaceRoute(CheckMode)
    case ConsigneeNamePage => consigneeNameRoute(CheckMode)
    case ConsigneeEoriConfirmationPage => consigneeEoriConfirmationRoute(CheckMode)
    case ConsigneeEoriNumberPage => consigneeEoriNumberRoute(CheckMode)
    case ConsigneeAddressPage => consigneeAddressRoute(CheckMode)
    case EventCountryPage(index) => ua => Some(eventRoutes.CheckEventAnswersController.onPageLoad(ua.id, index))
    case EventPlacePage(index) => ua => Some(eventRoutes.CheckEventAnswersController.onPageLoad(ua.id, index))
    case TraderAddressPage => traderAddressRoute(CheckMode)
    case IsTraderAddressPlaceOfNotificationPage => isTraderAddressPlaceOfNotificationCheckRoute
    case IsTranshipmentPage(index) => isTranshipmentCheckRoute(index)
    case IncidentInformationPage(index) => ua => Some(eventRoutes.CheckEventAnswersController.onPageLoad(ua.id, index))
    case TranshipmentTypePage(index) => transhipmentTypeCheckRoute(index)
    case ContainerNumberPage(index, _) => ua => Some(transhipmentRoutes.AddContainerController.onPageLoad(ua.id, index, CheckMode))
    case TransportIdentityPage(index) => transportIdentity(index)
    case TransportNationalityPage(index) => ua => Some(eventRoutes.CheckEventAnswersController.onPageLoad(ua.id, index))
    case AddContainerPage(index) => addContainerCheckRoute(index)
    case EventReportedPage(index) => eventReportedCheckRoute(index)
    case ConfirmRemoveContainerPage(index) => confirmRemoveContainerRoute(index, CheckMode)
    case ConfirmRemoveEventPage(index) => confirmRemoveEventRoute(index, CheckMode)
    case IncidentOnRoutePage => incidentOnRoute
    case SealIdentityPage(index, _) => ua => Some(sealRoutes.AddSealController.onPageLoad(ua.id, index, CheckMode))
    case HaveSealsChangedPage(index) => haveSealsChanged(index, CheckMode)
    case ConfirmRemoveSealPage(eventIndex) => removeSeal(eventIndex, CheckMode)
    case AddSealPage(eventIndex) => addSeal(eventIndex, CheckMode)
    case PresentationOfficePage =>  presentationOfficeRoute(CheckMode)
    case TraderNamePage =>  traderNameRoute(CheckMode)
    case TraderEoriPage =>  traderEoriRoute(CheckMode)


  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes.lift(page) match {
        case None => routes.MovementReferenceNumberController.onPageLoad()
        case Some(call) =>
          call(userAnswers) match {
            case Some(onwardRoute) => onwardRoute
            case None => routes.SessionExpiredController.onPageLoad()
          }
      }
    case CheckMode =>
      checkRouteMap.lift(page) match {
        case None => routes.CheckYourAnswersController.onPageLoad(userAnswers.id)
        case Some(call) =>
          call(userAnswers) match {
            case Some(onwardRoute) => onwardRoute
            case None => routes.SessionExpiredController.onPageLoad()
          }
      }
  }

  @deprecated("All navigation should rely on the nextPage method", "")
  def nextRejectionPage(page: Page, mrn: MovementReferenceNumber): Call =
    page match {
      case UpdateRejectedMRNPage => routes.CheckYourAnswersController.onPageLoad(mrn)
      case _ => routes.TechnicalDifficultiesController.onPageLoad()
    }
  
  // format: on

  private def traderAddressRoute(mode: Mode)(ua: UserAnswers) =
    (ua.get(IsTraderAddressPlaceOfNotificationPage), mode) match {
      case (Some(_), CheckMode) => Some(routes.CheckYourAnswersController.onPageLoad(ua.id))
      case (None, _)            => Some(routes.IsTraderAddressPlaceOfNotificationController.onPageLoad(ua.id, mode))
    }

//TODO refactor case matching
  private def consigneeEoriConfirmationRoute(mode: Mode)(ua: UserAnswers) =
    (ua.get(ConsigneeEoriConfirmationPage), mode, ua.get(ConsigneeEoriNumberPage), ua.get(ConsigneeAddressPage)) match {
      case (Some(true), NormalMode, _, _)       => Some(routes.ConsigneeAddressController.onPageLoad(ua.id, mode))
      case (Some(true), CheckMode, _, None)     => Some(routes.ConsigneeAddressController.onPageLoad(ua.id, mode))
      case (Some(true), CheckMode, _, _)        => Some(routes.CheckYourAnswersController.onPageLoad(ua.id))
      case (Some(false), CheckMode, Some(_), _) => Some(routes.ConsigneeEoriNumberController.onPageLoad(ua.id, mode))
      case (Some(false), _, _, _)               => Some(routes.ConsigneeEoriNumberController.onPageLoad(ua.id, mode))
      case _                                    => None
    }

  private def consigneeEoriNumberRoute(mode: Mode)(ua: UserAnswers) =
    (mode, ua.get(ConsigneeAddressPage)) match {
      case (CheckMode, Some(_)) => Some(routes.CheckYourAnswersController.onPageLoad(ua.id))
      case _                    => Some(routes.ConsigneeAddressController.onPageLoad(ua.id, mode))
    }

  private def consigneeNameRoute(mode: Mode)(ua: UserAnswers): Option[Call] =
    (ua.get(ConsigneeEoriConfirmationPage), mode) match {
      case (Some(_), CheckMode) => Some(routes.CheckYourAnswersController.onPageLoad(ua.id))
      case _                    => Some(routes.ConsigneeEoriConfirmationController.onPageLoad(ua.id, mode))
    }

  private def consigneeAddressRoute(mode: Mode)(ua: UserAnswers): Option[Call] =
    (ua.get(PresentationOfficePage), mode) match {
      case (Some(_), CheckMode) => Some(routes.CheckYourAnswersController.onPageLoad(ua.id))
      case _                    => Some(routes.PresentationOfficeController.onPageLoad(ua.id, mode))
    }

  private def presentationOfficeRoute(mode: Mode)(ua: UserAnswers): Option[Call] =
    (ua.get(GoodsLocationPage), ua.get(TraderNamePage), mode) match {
      case (Some(BorderForceOffice), Some(_), _)               => Some(routes.CheckYourAnswersController.onPageLoad(ua.id))
      case (Some(BorderForceOffice), None, _)                  => Some(routes.TraderNameController.onPageLoad(ua.id, mode))
      case (Some(AuthorisedConsigneesLocation), _, NormalMode) => Some(routes.IncidentOnRouteController.onPageLoad(ua.id, mode))
      case (Some(AuthorisedConsigneesLocation), _, _)          => Some(routes.CheckYourAnswersController.onPageLoad(ua.id))
      case _                                                   => Some(routes.SessionExpiredController.onPageLoad())
    }
  private def traderNameRoute(mode: Mode)(ua: UserAnswers): Option[Call] =
    (ua.get(TraderEoriPage), mode) match {
      case (Some(_), CheckMode) => Some(routes.CheckYourAnswersController.onPageLoad(ua.id))
      case _                    => Some(routes.TraderEoriController.onPageLoad(ua.id, mode))
    }
  private def traderEoriRoute(mode: Mode)(ua: UserAnswers): Option[Call] =
    (ua.get(TraderAddressPage), mode) match {
      case (Some(_), CheckMode) => Some(routes.CheckYourAnswersController.onPageLoad(ua.id))
      case _                    => Some(routes.TraderAddressController.onPageLoad(ua.id, mode))
    }
  private def customsSubPlaceRoute(mode: Mode)(ua: UserAnswers): Option[Call] =
    (ua.get(PresentationOfficePage), mode) match {
      case (Some(_), CheckMode) => Some(routes.CheckYourAnswersController.onPageLoad(ua.id))
      case _                    => Some(routes.PresentationOfficeController.onPageLoad(ua.id, mode))
    }
  private def authorisedLocationRoute(mode: Mode)(ua: UserAnswers): Option[Call] =
    (ua.get(ConsigneeNamePage), mode) match {
      case (Some(_), CheckMode) => Some(routes.CheckYourAnswersController.onPageLoad(ua.id))
      case _                    => Some(routes.ConsigneeNameController.onPageLoad(ua.id, mode))
    }
  private def transportIdentity(eventIndex: Index)(ua: UserAnswers): Option[Call] =
    ua.get(TransportNationalityPage(eventIndex)) match {
      case (Some(_)) => Some(eventRoutes.CheckEventAnswersController.onPageLoad(ua.id, eventIndex))
      case _         => Some(transhipmentRoutes.TransportNationalityController.onPageLoad(ua.id, eventIndex, CheckMode))
    }

  private def removeSeal(eventIndex: Index, mode: Mode)(ua: UserAnswers) =
    ua.get(DeriveNumberOfSeals(eventIndex)) match {
      case None | Some(0) => Some(sealRoutes.HaveSealsChangedController.onPageLoad(ua.id, eventIndex, mode))
      case _              => Some(sealRoutes.AddSealController.onPageLoad(ua.id, eventIndex, mode))
    }

  def confirmRemoveContainerRoute(eventIndex: Index, mode: Mode)(ua: UserAnswers): Option[Call] = ua.get(DeriveNumberOfContainers(eventIndex)) match {
    case Some(0) | None => Some(eventRoutes.IsTranshipmentController.onPageLoad(ua.id, eventIndex, mode))
    case _              => Some(transhipmentRoutes.AddContainerController.onPageLoad(ua.id, eventIndex, mode))
  }

  private def confirmRemoveEventRoute(eventIndex: Index, mode: Mode)(ua: UserAnswers) = ua.get(DeriveNumberOfEvents) match {
    case Some(0) | None => Some(routes.IncidentOnRouteController.onPageLoad(ua.id, mode))
    case _              => Some(eventRoutes.AddEventController.onPageLoad(ua.id, mode))

  }

  private def addContainer(eventIndex: Index)(ua: UserAnswers): Option[Call] = ua.get(AddContainerPage(eventIndex)) map {
    case true =>
      // TODO: Need to consolidate with same logic for initialsation of eventIndex in transhipmentType
      val nextContainerCount = ua.get(DeriveNumberOfContainers(eventIndex)).getOrElse(0)
      val nextContainerIndex = Index(nextContainerCount)
      transhipmentRoutes.ContainerNumberController.onPageLoad(ua.id, eventIndex, nextContainerIndex, NormalMode)
    case false =>
      ua.get(TranshipmentTypePage(eventIndex)) match {
        case Some(DifferentContainerAndVehicle) => transhipmentRoutes.TransportIdentityController.onPageLoad(ua.id, eventIndex, NormalMode)
        case _                                  => sealRoutes.HaveSealsChangedController.onPageLoad(ua.id, eventIndex, NormalMode)
      }
  }

  private def addContainerCheckRoute(eventIndex: Index)(ua: UserAnswers): Option[Call] = ua.get(AddContainerPage(eventIndex)) map {
    case true =>
      val nextContainerCount = ua.get(DeriveNumberOfContainers(eventIndex)).getOrElse(0)
      val nextContainerIndex = Index(nextContainerCount)
      transhipmentRoutes.ContainerNumberController.onPageLoad(ua.id, eventIndex, nextContainerIndex, CheckMode)
    case false =>
      (
        ua.get(TranshipmentTypePage(eventIndex)),
        ua.get(TransportIdentityPage(eventIndex))
      ) match {
        case (Some(DifferentContainerAndVehicle), None) => transhipmentRoutes.TransportIdentityController.onPageLoad(ua.id, eventIndex, CheckMode)
        case _                                          => eventRoutes.CheckEventAnswersController.onPageLoad(ua.id, eventIndex)
      }
  }

  private def transhipmentType(eventIndex: Index)(ua: UserAnswers): Option[Call] = ua.get(transhipments.TranshipmentTypePage(eventIndex)) map {
    case DifferentContainer | DifferentContainerAndVehicle =>
      ua.get(DeriveNumberOfContainers(eventIndex)) match {
        case Some(0) | None => transhipmentRoutes.ContainerNumberController.onPageLoad(ua.id, eventIndex, Index(0), NormalMode)
        case Some(_)        => transhipmentRoutes.AddContainerController.onPageLoad(ua.id, eventIndex, NormalMode)
      }
    case DifferentVehicle => transhipmentRoutes.TransportIdentityController.onPageLoad(ua.id, eventIndex, NormalMode)
  }

  private def transhipmentTypeCheckRoute(eventIndex: Index)(ua: UserAnswers): Option[Call] =
    (
      ua.get(TranshipmentTypePage(eventIndex)),
      ua.get(ContainerNumberPage(eventIndex, Index(0))), //todo: confirm the logic here and hard coded 0's
      ua.get(TransportIdentityPage(eventIndex)),
      ua.get(TransportNationalityPage(eventIndex))
    ) match {
      case (Some(DifferentContainer) | Some(DifferentContainerAndVehicle), None, _, _) =>
        Some(transhipmentRoutes.ContainerNumberController.onPageLoad(ua.id, eventIndex, Index(0), CheckMode))

      case (Some(DifferentVehicle), _, None, _) =>
        Some(transhipmentRoutes.TransportIdentityController.onPageLoad(ua.id, eventIndex, CheckMode))

      case (Some(DifferentContainerAndVehicle), Some(_), Some(_), Some(_)) =>
        Some(eventRoutes.CheckEventAnswersController.onPageLoad(ua.id, eventIndex))

      case (Some(DifferentContainerAndVehicle), Some(_), _, _) =>
        Some(transhipmentRoutes.AddContainerController.onPageLoad(ua.id, eventIndex, CheckMode))

      case _ =>
        Some(eventRoutes.CheckEventAnswersController.onPageLoad(ua.id, eventIndex))
    }

  private def incidentOnRoute(ua: UserAnswers): Option[Call] =
    (ua.get(IncidentOnRoutePage), ua.get(DeriveNumberOfEvents)) match {
      case (Some(true), None | Some(0)) => Some(eventRoutes.EventCountryController.onPageLoad(ua.id, Index(0), NormalMode))
      case (Some(true), Some(_))        => Some(eventRoutes.AddEventController.onPageLoad(ua.id, NormalMode))
      case (Some(false), _)             => Some(routes.CheckYourAnswersController.onPageLoad(ua.id))
      case _                            => None
    }

  private def isTranshipmentRoute(eventIndex: Index)(ua: UserAnswers): Option[Call] =
    (ua.get(EventReportedPage(eventIndex)), ua.get(IsTranshipmentPage(eventIndex))) match {
      case (_, Some(true))            => Some(transhipmentRoutes.TranshipmentTypeController.onPageLoad(ua.id, eventIndex, NormalMode))
      case (Some(false), Some(false)) => Some(eventRoutes.IncidentInformationController.onPageLoad(ua.id, eventIndex, NormalMode))
      case (Some(true), Some(false))  => Some(sealRoutes.HaveSealsChangedController.onPageLoad(ua.id, eventIndex, NormalMode))
      case _                          => None
    }

  private def isTranshipmentCheckRoute(eventIndex: Index)(ua: UserAnswers): Option[Call] =
    (
      ua.get(EventReportedPage(eventIndex)),
      ua.get(IsTranshipmentPage(eventIndex)),
      ua.get(IncidentInformationPage(eventIndex)),
      ua.get(TranshipmentTypePage(eventIndex)),
      ua.get(DeriveNumberOfContainers(eventIndex))
    ) match {
      case (Some(false), Some(false), None, _, _) => Some(eventRoutes.IncidentInformationController.onPageLoad(ua.id, eventIndex, CheckMode))
      case (_, Some(true), _, None, _)            => Some(transhipmentRoutes.TranshipmentTypeController.onPageLoad(ua.id, eventIndex, CheckMode))
      case (_, Some(true), _, Some(DifferentContainer) | Some(DifferentContainerAndVehicle), Some(0) | None) =>
        Some(transhipmentRoutes.TranshipmentTypeController.onPageLoad(ua.id, eventIndex, CheckMode))
      case _ => Some(eventRoutes.CheckEventAnswersController.onPageLoad(ua.id, eventIndex))
    }

  private def goodsLocationCheckRoute(ua: UserAnswers): Option[Call] =
    // TODO: Get the requirements for this sorted out. AuthorisedLocationPage is not actually being used here
    (ua.get(GoodsLocationPage), ua.get(AuthorisedLocationPage), ua.get(CustomsSubPlacePage)) match {
      case (Some(BorderForceOffice), _, None)         => Some(routes.CustomsSubPlaceController.onPageLoad(ua.id, CheckMode))
      case (Some(AuthorisedConsigneesLocation), _, _) => Some(routes.UseDifferentServiceController.onPageLoad(ua.id))
      case _ =>
        Some(routes.CheckYourAnswersController.onPageLoad(ua.id)) // TODO: This branch is ill defined and needs to be fixed
    }

  private def isTraderAddressPlaceOfNotificationRoute(ua: UserAnswers): Option[Call] =
    ua.get(IsTraderAddressPlaceOfNotificationPage) match {
      case Some(true)  => Some(routes.IncidentOnRouteController.onPageLoad(ua.id, NormalMode))
      case Some(false) => Some(routes.PlaceOfNotificationController.onPageLoad(ua.id, NormalMode))
      case _           => None
    }

  private def isTraderAddressPlaceOfNotificationCheckRoute(ua: UserAnswers): Option[Call] =
    (ua.get(IsTraderAddressPlaceOfNotificationPage), ua.get(PlaceOfNotificationPage)) match {
      case (Some(false), None) => Some(routes.PlaceOfNotificationController.onPageLoad(ua.id, CheckMode))
      case (Some(_), _)        => Some(routes.CheckYourAnswersController.onPageLoad(ua.id))
      case _                   => None
    }

  private def addEventRoute(ua: UserAnswers): Option[Call] =
    (ua.get(AddEventPage), ua.get(DeriveNumberOfEvents)) match {
      case (Some(true), Some(eventIndex)) => Some(eventRoutes.EventCountryController.onPageLoad(ua.id, Index(eventIndex), NormalMode))
      case (Some(true), None)             => Some(eventRoutes.EventCountryController.onPageLoad(ua.id, Index(0), NormalMode))
      case (Some(false), _)               => Some(routes.CheckYourAnswersController.onPageLoad(ua.id))
      case _                              => None
    }

  private def eventReportedCheckRoute(eventIndex: Index)(userAnswers: UserAnswers): Option[Call] =
    (userAnswers.get(EventReportedPage(eventIndex)), userAnswers.get(IsTranshipmentPage(eventIndex))) match {
      case (Some(false), Some(false)) => Some(eventRoutes.IncidentInformationController.onPageLoad(userAnswers.id, eventIndex, CheckMode))
      case _                          => Some(eventRoutes.CheckEventAnswersController.onPageLoad(userAnswers.id, eventIndex))
    }

  private def haveSealsChanged(eventIndex: Index, mode: Mode)(userAnswers: UserAnswers): Option[Call] =
    userAnswers.get(HaveSealsChangedPage(eventIndex)).map {
      case true =>
        if (userAnswers.get(SealIdentityPage(eventIndex, Index(0))).isDefined) { //todo: hardcoded 0?
          sealRoutes.AddSealController.onPageLoad(userAnswers.id, eventIndex, mode)
        } else {
          val sealCount = userAnswers.get(DeriveNumberOfSeals(eventIndex)).getOrElse(0)
          val sealIndex = Index(sealCount)
          sealRoutes.SealIdentityController.onPageLoad(userAnswers.id, eventIndex, sealIndex, mode)
        }
      case false => eventRoutes.CheckEventAnswersController.onPageLoad(userAnswers.id, eventIndex)
    }

  private def addSeal(eventIndex: Index, mode: Mode)(userAnswers: UserAnswers): Option[Call] =
    userAnswers.get(AddSealPage(eventIndex)).map {
      case true =>
        val sealCount = userAnswers.get(DeriveNumberOfSeals(eventIndex)).getOrElse(0)
        val sealIndex = Index(sealCount)
        sealRoutes.SealIdentityController.onPageLoad(userAnswers.id, eventIndex, sealIndex, mode)
      case false =>
        eventRoutes.CheckEventAnswersController.onPageLoad(userAnswers.id, eventIndex)
    }
}
