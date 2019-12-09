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

import com.google.inject.{Inject, Singleton}
import computable.NumberOfEvents
import controllers.events.{routes => eventRoutes}
import controllers.routes
import models.GoodsLocation._
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import pages.events._
import pages._
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
    case IsTraderAddressPlaceOfNotificationPage => isTraderAddressPlaceOfNotificationRoute(NormalMode)
    case PlaceOfNotificationPage => ua => Some(routes.IncidentOnRouteController.onPageLoad(ua.id, NormalMode))
    case IncidentOnRoutePage => incidentOnRouteRoute
    case EventCountryPage(index) => ua => Some(eventRoutes.EventPlaceController.onPageLoad(ua.id, index, NormalMode))
    case EventPlacePage(index) => ua => Some(eventRoutes.EventReportedController.onPageLoad(ua.id, index, NormalMode))
    case EventReportedPage(index) => ua => Some(eventRoutes.IsTranshipmentController.onPageLoad(ua.id, index, NormalMode))
    case IsTranshipmentPage(index) => isTranshipmentRoute(index)
    case IncidentInformationPage(index) => ua => Some(eventRoutes.CheckEventAnswersController.onPageLoad(ua.id, index))
    case AddEventPage => addEventRoute
  }

  private val checkRouteMap: PartialFunction[Page, UserAnswers => Option[Call]] = {
    case GoodsLocationPage         => goodsLocationCheckRoute
    case EventCountryPage(index)   => ua => Some(eventRoutes.CheckEventAnswersController.onPageLoad(ua.id, index))
    case EventPlacePage(index) => ua => Some(eventRoutes.CheckEventAnswersController.onPageLoad(ua.id, index))
    case EventReportedPage(index) => ua => Some(eventRoutes.CheckEventAnswersController.onPageLoad(ua.id, index))
    case IsTranshipmentPage(index) => ua => Some(eventRoutes.CheckEventAnswersController.onPageLoad(ua.id, index))
    case IncidentInformationPage(index) => ua => Some(eventRoutes.CheckEventAnswersController.onPageLoad(ua.id, index))
    case _                         => ua => Some(routes.CheckYourAnswersController.onPageLoad(ua.id)) // TODO move to match on checkRouteMap
  }
  // format: on

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

  private def goodsLocationPageRoutes(ua: UserAnswers): Option[Call] =
    ua.get(GoodsLocationPage) map {
      case BorderForceOffice            => routes.CustomsSubPlaceController.onPageLoad(ua.id, NormalMode)
      case AuthorisedConsigneesLocation => routes.UseDifferentServiceController.onPageLoad(ua.id)
    }

  private def incidentOnRouteRoute(ua: UserAnswers): Option[Call] =
    ua.get(IncidentOnRoutePage) map {
      case true  => eventRoutes.EventCountryController.onPageLoad(ua.id, 0, NormalMode) // TODO Remove hard coded 0 here and determine this from a query
      case false => routes.CheckYourAnswersController.onPageLoad(ua.id)
    }

  private def isTranshipmentRoute(ua: UserAnswers): Option[Call] =
    ua.get(IsTranshipmentPage) map {
      case true                                               => routes.CheckEventAnswersController.onPageLoad(ua.id) //TODO new Transhipment journey first page
      case false if ua.get(EventReportedPage).contains(false) => routes.IncidentInformationController.onPageLoad(ua.id, NormalMode)
      case _                                                  => routes.CheckEventAnswersController.onPageLoad(ua.id)
    }

  private def goodsLocationCheckRoute(ua: UserAnswers): Option[Call] =
    // TODO: Get the requirements for this sorted out. AuthorisedLocationPage is not actually being used here
    (ua.get(GoodsLocationPage), ua.get(AuthorisedLocationPage), ua.get(CustomsSubPlacePage)) match {
      case (Some(BorderForceOffice), _, None)         => Some(routes.CustomsSubPlaceController.onPageLoad(ua.id, CheckMode))
      case (Some(AuthorisedConsigneesLocation), _, _) => Some(routes.UseDifferentServiceController.onPageLoad(ua.id))
      case _ =>
        Some(routes.CheckYourAnswersController.onPageLoad(ua.id)) // TODO: This branch is ill defined and needs to be fixed
    }

  private def isTraderAddressPlaceOfNotificationRoute(mode: Mode)(ua: UserAnswers): Option[Call] =
    (ua.get(IsTraderAddressPlaceOfNotificationPage), ua.get(PlaceOfNotificationPage), mode) match {
      case (Some(true), _, NormalMode)       => Some(routes.IncidentOnRouteController.onPageLoad(ua.id, mode))
      case (Some(true), _, CheckMode)        => Some(routes.CheckYourAnswersController.onPageLoad(ua.id))
      case (Some(false), _, NormalMode)      => Some(routes.PlaceOfNotificationController.onPageLoad(ua.id, mode))
      case (Some(false), Some(_), CheckMode) => Some(routes.CheckYourAnswersController.onPageLoad(ua.id))
      case _                                 => None
    }

  private def addEventRoute(ua: UserAnswers): Option[Call] =
    (ua.get(AddEventPage), ua.get(NumberOfEvents)) match {
      case (Some(true), Some(index)) => Some(eventRoutes.EventCountryController.onPageLoad(ua.id, index, NormalMode))
      case (Some(true), None)        => Some(eventRoutes.EventCountryController.onPageLoad(ua.id, 0, NormalMode))
      case (Some(false), _)          => Some(routes.CheckYourAnswersController.onPageLoad(ua.id))
      case _                         => None
    }
}
