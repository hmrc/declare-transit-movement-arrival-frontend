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
import controllers.events.transhipments.{routes => transhipmentRoutes}
import controllers.events.{routes => eventRoutes}
import controllers.events.seals.{routes => sealRoutes}
import controllers.routes
import derivable.{DeriveNumberOfContainers, DeriveNumberOfEvents, DeriveNumberOfSeals}
import models.GoodsLocation._
import models.TranshipmentType.{DifferentContainer, DifferentContainerAndVehicle, DifferentVehicle}
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import pages._
import pages.events._
import pages.events.seals.{AddSealPage, HaveSealsChangedPage, SealIdentityPage}
import pages.events.transhipments._
import play.api.mvc.Call

@Singleton
class Navigator @Inject()() {

  // format: off
  private val normalRoutes: PartialFunction[Page, UserAnswers => Option[Call]] = {
    case MovementReferenceNumberPage => ua => Some(routes.GoodsLocationController.onPageLoad(ua.id, NormalMode))
    case GoodsLocationPage => goodsLocationPageRoutes
    case PresentationOfficePage => ua => Some(routes.TraderNameController.onPageLoad(ua.id, NormalMode))
    case CustomsSubPlacePage => ua => Some(routes.PresentationOfficeController.onPageLoad(ua.id, NormalMode))
    case TraderNamePage => ua => Some(routes.TraderEoriController.onPageLoad(ua.id, NormalMode))
    case TraderAddressPage => ua => Some(routes.IsTraderAddressPlaceOfNotificationController.onPageLoad(ua.id, NormalMode))
    case TraderEoriPage => ua => Some(routes.TraderAddressController.onPageLoad(ua.id, NormalMode))
    case IsTraderAddressPlaceOfNotificationPage => isTraderAddressPlaceOfNotificationRoute
    case PlaceOfNotificationPage => ua => Some(routes.IncidentOnRouteController.onPageLoad(ua.id, NormalMode))
    case IncidentOnRoutePage => incidentOnRoute
    case EventCountryPage(index) => ua => Some(eventRoutes.EventPlaceController.onPageLoad(ua.id, index, NormalMode))
    case EventPlacePage(index) => ua => Some(eventRoutes.EventReportedController.onPageLoad(ua.id, index, NormalMode))
    case EventReportedPage(index) => ua => Some(eventRoutes.IsTranshipmentController.onPageLoad(ua.id, index, NormalMode))
    case IsTranshipmentPage(index) => isTranshipmentRoute(index)
    case IncidentInformationPage(index) => ua => Some(sealRoutes.HaveSealsChangedController.onPageLoad(ua.id, index, NormalMode))
    case AddEventPage => addEventRoute
    case TranshipmentTypePage(index) => transhipmentType(index)
    case TransportIdentityPage(index) => ua => Some(transhipmentRoutes.TransportNationalityController.onPageLoad(ua.id, index, NormalMode))
    case TransportNationalityPage(index) => ua => Some(sealRoutes.HaveSealsChangedController.onPageLoad(ua.id, index, NormalMode))
    case ContainerNumberPage(index, _) => ua => Some(transhipmentRoutes.AddContainerController.onPageLoad(ua.id, index, NormalMode))
    case AddContainerPage(index) => addContainer(index)
    case HaveSealsChangedPage(index) => haveSealsChanged(index, NormalMode)
    case SealIdentityPage(index, _) => ua => Some(sealRoutes.AddSealController.onPageLoad(ua.id, index, NormalMode))
    case AddSealPage(index) => addSeal(index)
  }

  private val checkRouteMap: PartialFunction[Page, UserAnswers => Option[Call]] = {
    case GoodsLocationPage => goodsLocationCheckRoute
    case EventCountryPage(index) => ua => Some(eventRoutes.CheckEventAnswersController.onPageLoad(ua.id, index))
    case EventPlacePage(index) => ua => Some(eventRoutes.CheckEventAnswersController.onPageLoad(ua.id, index))
    case IsTraderAddressPlaceOfNotificationPage => isTraderAddressPlaceOfNotificationCheckRoute
    case IsTranshipmentPage(index) => isTranshipmentCheckRoute(index)
    case IncidentInformationPage(index) => ua => Some(eventRoutes.CheckEventAnswersController.onPageLoad(ua.id, index))
    case TranshipmentTypePage(index) => transhipmentTypeCheckRoute(index)
    case ContainerNumberPage(index, _) => ua => Some(transhipmentRoutes.AddContainerController.onPageLoad(ua.id, index, CheckMode))
    case TransportIdentityPage(index) => ua => Some(eventRoutes.CheckEventAnswersController.onPageLoad(ua.id, index))
    case TransportNationalityPage(index) => ua => Some(eventRoutes.CheckEventAnswersController.onPageLoad(ua.id, index))
    case SealIdentityPage(index, _) => ua => Some(sealRoutes.AddSealController.onPageLoad(ua.id, index, CheckMode))
    case AddContainerPage(index) => addContainerCheckRoute(index)
    case EventReportedPage(index) => eventReportedCheckRoute(index)
    case HaveSealsChangedPage(index) => haveSealsChanged(index, CheckMode)
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes.lift(page) match {
        case None => routes.IndexController.onPageLoad()
        case Some(call) =>
          call(userAnswers) match {
            case Some(onwardRoute) => onwardRoute
            case None              => routes.SessionExpiredController.onPageLoad()
          }
      }
    case CheckMode =>
      checkRouteMap.lift(page) match {
        case None => routes.CheckYourAnswersController.onPageLoad(userAnswers.id)
        case Some(call) =>
          call(userAnswers) match {
            case Some(onwardRoute) => onwardRoute
            case None              => routes.SessionExpiredController.onPageLoad()
          }
      }
  }
  // format: on
  private def addContainer(index: Int)(ua: UserAnswers): Option[Call] = ua.get(AddContainerPage(index)) map {
    case true =>
      // TODO: Need to consolidate with same logic for initialsation of index in transhipmentType
      val nextContainerIndex = ua.get(DeriveNumberOfContainers(index)).getOrElse(0)
      transhipmentRoutes.ContainerNumberController.onPageLoad(ua.id, index, nextContainerIndex, NormalMode)
    case false =>
      ua.get(TranshipmentTypePage(index)) match {
        case Some(DifferentContainerAndVehicle) => transhipmentRoutes.TransportIdentityController.onPageLoad(ua.id, index, NormalMode)
        case _                                  => sealRoutes.HaveSealsChangedController.onPageLoad(ua.id, index, NormalMode)
      }
  }

  private def addContainerCheckRoute(index: Int)(ua: UserAnswers): Option[Call] = ua.get(AddContainerPage(index)) map {
    case true =>
      val nextContainerIndex = ua.get(DeriveNumberOfContainers(index)).getOrElse(0)
      transhipmentRoutes.ContainerNumberController.onPageLoad(ua.id, index, nextContainerIndex, CheckMode)
    case false =>
      (
        ua.get(TranshipmentTypePage(index)),
        ua.get(TransportIdentityPage(index))
      ) match {
        case (Some(DifferentContainerAndVehicle), None) => transhipmentRoutes.TransportIdentityController.onPageLoad(ua.id, index, CheckMode)
        case _                                          => eventRoutes.CheckEventAnswersController.onPageLoad(ua.id, index)
      }
  }

  private def transhipmentType(index: Int)(ua: UserAnswers): Option[Call] = ua.get(transhipments.TranshipmentTypePage(index)) map {
    case DifferentContainer | DifferentContainerAndVehicle =>
      ua.get(DeriveNumberOfContainers(index)) match {
        case Some(_) => transhipmentRoutes.AddContainerController.onPageLoad(ua.id, index, NormalMode)
        // TODO: Need to consolidate with same logic for initialsation of index in addContainer
        case None => transhipmentRoutes.ContainerNumberController.onPageLoad(ua.id, index, 0, NormalMode)
      }
    case DifferentVehicle => transhipmentRoutes.TransportIdentityController.onPageLoad(ua.id, index, NormalMode)
  }

  private def transhipmentTypeCheckRoute(index: Int)(ua: UserAnswers): Option[Call] =
    (
      ua.get(TranshipmentTypePage(index)),
      ua.get(ContainerNumberPage(index, 0)),
      ua.get(TransportIdentityPage(index)),
      ua.get(TransportNationalityPage(index))
    ) match {
      case (Some(DifferentContainer) | Some(DifferentContainerAndVehicle), None, _, _) =>
        Some(transhipmentRoutes.ContainerNumberController.onPageLoad(ua.id, index, 0, CheckMode))

      case (Some(DifferentVehicle), _, None, _) =>
        Some(transhipmentRoutes.TransportIdentityController.onPageLoad(ua.id, index, CheckMode))

      case (Some(DifferentContainerAndVehicle), Some(_), Some(_), Some(_)) =>
        Some(eventRoutes.CheckEventAnswersController.onPageLoad(ua.id, index))

      case (Some(DifferentContainerAndVehicle), Some(_), _, _) =>
        Some(transhipmentRoutes.AddContainerController.onPageLoad(ua.id, index, CheckMode))

      case _ =>
        Some(eventRoutes.CheckEventAnswersController.onPageLoad(ua.id, index))
    }

  private def goodsLocationPageRoutes(ua: UserAnswers): Option[Call] =
    ua.get(GoodsLocationPage) map {
      case BorderForceOffice            => routes.CustomsSubPlaceController.onPageLoad(ua.id, NormalMode)
      case AuthorisedConsigneesLocation => routes.UseDifferentServiceController.onPageLoad(ua.id)
    }

  private def incidentOnRoute(ua: UserAnswers): Option[Call] =
    (ua.get(IncidentOnRoutePage), ua.get(DeriveNumberOfEvents)) match {
      case (Some(true), None)    => Some(eventRoutes.EventCountryController.onPageLoad(ua.id, 0, NormalMode))
      case (Some(true), Some(_)) => Some(eventRoutes.AddEventController.onPageLoad(ua.id, NormalMode))
      case (Some(false), _)      => Some(routes.CheckYourAnswersController.onPageLoad(ua.id))
      case _                     => None
    }

  private def isTranshipmentRoute(index: Int)(ua: UserAnswers): Option[Call] =
    (ua.get(EventReportedPage(index)), ua.get(IsTranshipmentPage(index))) match {
      case (_, Some(true))            => Some(transhipmentRoutes.TranshipmentTypeController.onPageLoad(ua.id, index, NormalMode))
      case (Some(false), Some(false)) => Some(eventRoutes.IncidentInformationController.onPageLoad(ua.id, index, NormalMode))
      case (Some(true), Some(false))  => Some(sealRoutes.HaveSealsChangedController.onPageLoad(ua.id, index, NormalMode))
      case _                          => None
    }

  private def isTranshipmentCheckRoute(index: Int)(ua: UserAnswers): Option[Call] =
    (
      ua.get(EventReportedPage(index)),
      ua.get(IsTranshipmentPage(index)),
      ua.get(IncidentInformationPage(index)),
      ua.get(TranshipmentTypePage(index))
    ) match {
      case (Some(false), Some(false), None, _) => Some(eventRoutes.IncidentInformationController.onPageLoad(ua.id, index, CheckMode))
      case (_, Some(true), _, None)            => Some(transhipmentRoutes.TranshipmentTypeController.onPageLoad(ua.id, index, CheckMode))
      case _                                   => Some(eventRoutes.CheckEventAnswersController.onPageLoad(ua.id, index))
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
      case (Some(true), Some(index)) => Some(eventRoutes.EventCountryController.onPageLoad(ua.id, index, NormalMode))
      case (Some(true), None)        => Some(eventRoutes.EventCountryController.onPageLoad(ua.id, 0, NormalMode))
      case (Some(false), _)          => Some(routes.CheckYourAnswersController.onPageLoad(ua.id))
      case _                         => None
    }

  private def eventReportedCheckRoute(eventIndex: Int)(userAnswers: UserAnswers): Option[Call] =
    (userAnswers.get(EventReportedPage(eventIndex)), userAnswers.get(IsTranshipmentPage(eventIndex))) match {
      case (Some(false), Some(false)) => Some(eventRoutes.IncidentInformationController.onPageLoad(userAnswers.id, eventIndex, CheckMode))
      case (Some(_), _)               => Some(eventRoutes.CheckEventAnswersController.onPageLoad(userAnswers.id, eventIndex))
      case _                          => None
    }

  private def haveSealsChanged(index: Int, mode: Mode)(userAnswers: UserAnswers): Option[Call] =
    userAnswers.get(HaveSealsChangedPage(index)).map {
      case true  => sealRoutes.SealIdentityController.onPageLoad(userAnswers.id, index, 0, mode)
      case false => eventRoutes.CheckEventAnswersController.onPageLoad(userAnswers.id, index)
    }

  private def addSeal(index: Int)(userAnswers: UserAnswers): Option[Call] =
    userAnswers.get(AddSealPage(index)).map {
      case true  => sealRoutes.SealIdentityController.onPageLoad(userAnswers.id, index, userAnswers.get(DeriveNumberOfSeals(index)).getOrElse(0), NormalMode)
      case false => eventRoutes.CheckEventAnswersController.onPageLoad(userAnswers.id, index)
    }
}
