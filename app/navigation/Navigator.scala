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

import javax.inject.Inject
import javax.inject.Singleton
import play.api.mvc.Call
import controllers.routes
import pages._
import models._
import GoodsLocation._

@Singleton
class Navigator @Inject()() {

  // format: off
  private val normalRoutes: PartialFunction[Page, UserAnswers => Option[Call]]  = {
    case MovementReferenceNumberPage => ua => Some(routes.GoodsLocationController.onPageLoad(ua.id, NormalMode))
    case GoodsLocationPage => goodsLocationPageRoutes
    case PresentationOfficePage => ua => Some(routes.TraderNameController.onPageLoad(ua.id, NormalMode))
    case CustomsSubPlacePage => ua => Some(routes.PresentationOfficeController.onPageLoad(ua.id, NormalMode))
    case TraderNamePage => ua => Some(routes.TraderEoriController.onPageLoad(ua.id, NormalMode))
    case TraderAddressPage => ua => Some(routes.IsTraderAddressPlaceOfNotificationController.onPageLoad(ua.id, NormalMode))
    case TraderEoriPage => ua => Some(routes.TraderAddressController.onPageLoad(ua.id, NormalMode))
    case IsTraderAddressPlaceOfNotificationPage => isTraderAddressPlaceOfNotificationRoute(NormalMode)
    case PlaceOfNotificationPage => ua => Some(routes.IncidentOnRouteController.onPageLoad(ua.id, NormalMode))
    case IncidentOnRoutePage => incidentOnRouteRoute
    case EventCountryPage => ua => Some(routes.EventPlaceController.onPageLoad(ua.id, NormalMode))
    case EventPlacePage => ua => Some(routes.EventReportedController.onPageLoad(ua.id, NormalMode))
    case EventReportedPage => ua => Some(routes.IsTranshipmentController.onPageLoad(ua.id, NormalMode))
    case IsTranshipmentPage => isTranshipmentRoute
    case IncidentInformationPage => ua => Some(routes.CheckEventAnswersController.onPageLoad(ua.id))
  }

  private val checkRouteMap: PartialFunction[Page, UserAnswers => Option[Call]] = {
    case GoodsLocationPage         => goodsLocationCheckRoute
    case page if eventsPages(page) => ua => Some(routes.CheckEventAnswersController.onPageLoad(ua.id))
    case _                         => ua => Some(routes.CheckYourAnswersController.onPageLoad(ua.id)) // move to match on checkRouteMap
  }
  // format: on

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes.lift(page) match {
        case Some(call) =>
          call(userAnswers) match {
            case Some(onwardRoute) => onwardRoute
            case None              => routes.SessionExpiredController.onPageLoad()
          }
        case None => routes.IndexController.onPageLoad()
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

  private def eventsPages(page: Page): Boolean =
    page match {
      case EventCountryPage | EventPlacePage | EventReportedPage | IsTranshipmentPage | IncidentInformationPage => true
      case _                                                                                                    => false
    }

  private def goodsLocationPageRoutes(ua: UserAnswers): Option[Call] =
    ua.get(GoodsLocationPage) map {
      case BorderForceOffice            => routes.CustomsSubPlaceController.onPageLoad(ua.id, NormalMode)
      case AuthorisedConsigneesLocation => routes.UseDifferentServiceController.onPageLoad(ua.id)
    }

  private def incidentOnRouteRoute(ua: UserAnswers): Option[Call] =
    ua.get(IncidentOnRoutePage) map {
      case true  => routes.EventCountryController.onPageLoad(ua.id, NormalMode)
      case false => routes.CheckYourAnswersController.onPageLoad(ua.id)
    }

  private def isTranshipmentRoute(ua: UserAnswers): Option[Call] =
    ua.get(EventReportedPage) map {
      case true  => routes.CheckEventAnswersController.onPageLoad(ua.id)
      case false => routes.IncidentInformationController.onPageLoad(ua.id, NormalMode)
    }

  private def goodsLocationCheckRoute(ua: UserAnswers): Option[Call] =
    // TODO: Get the requirements for this sorted out. AuthorisedLocationPage is not actually being used here
    (ua.get(GoodsLocationPage), ua.get(AuthorisedLocationPage), ua.get(CustomsSubPlacePage)) match {
      case (Some(BorderForceOffice), _, None)         => Some(routes.CustomsSubPlaceController.onPageLoad(ua.id, CheckMode))
      case (Some(AuthorisedConsigneesLocation), _, _) => Some(routes.UseDifferentServiceController.onPageLoad(ua.id))
      case _                                          => Some(routes.CheckYourAnswersController.onPageLoad(ua.id)) // TODO: This branch is ill defined and needs to be fixed
    }

  private def isTraderAddressPlaceOfNotificationRoute(mode: Mode)(ua: UserAnswers): Option[Call] =
    (ua.get(IsTraderAddressPlaceOfNotificationPage), ua.get(PlaceOfNotificationPage), mode) match {
      case (Some(true), _, NormalMode) => Some(routes.IncidentOnRouteController.onPageLoad(ua.id, mode))
      case (Some(true), _, CheckMode)  => Some(routes.CheckYourAnswersController.onPageLoad(ua.id))

      case (Some(false), _, NormalMode) => Some(routes.PlaceOfNotificationController.onPageLoad(ua.id, mode))

      case (Some(false), Some(_), CheckMode) => Some(routes.CheckYourAnswersController.onPageLoad(ua.id))
      case _                                 => None
    }

}
