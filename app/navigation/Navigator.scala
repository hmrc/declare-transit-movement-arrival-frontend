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

import javax.inject.{Inject, Singleton}

import play.api.mvc.Call
import controllers.routes
import pages._
import models._

@Singleton
class Navigator @Inject()() {

  private val normalRoutes: Page => UserAnswers => Call = {
    case MovementReferenceNumberPage => ua => routes.GoodsLocationController.onPageLoad(ua.id, NormalMode)
    case GoodsLocationPage => ua => goodsLocationPageRoutes(ua)
    case PresentationOfficePage => ua => routes.CustomsSubPlaceController.onPageLoad(ua.id, NormalMode)
    case CustomsSubPlacePage => ua => routes.TraderNameController.onPageLoad(ua.id, NormalMode)
    case TraderNamePage => ua => routes.TraderAddressController.onPageLoad(ua.id, NormalMode)
    case TraderAddressPage => ua => routes.TraderEoriController.onPageLoad(ua.id, NormalMode)
    case TraderEoriPage => ua => routes.IncidentOnRouteController.onPageLoad(ua.id, NormalMode)
    case IncidentOnRoutePage => ua => routes.CheckYourAnswersController.onPageLoad(ua.id)
    case _ => _ => routes.IndexController.onPageLoad()
  }

  private val checkRouteMap: Page => UserAnswers => Call = {
    case _ => ua => routes.CheckYourAnswersController.onPageLoad(ua.id)
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)
    case CheckMode =>
      checkRouteMap(page)(userAnswers)
  }

  private def goodsLocationPageRoutes(ua: UserAnswers): Call = {
    if (ua.get(GoodsLocationPage).contains(GoodsLocation.Borderforceoffice)) {
      routes.PresentationOfficeController.onPageLoad(ua.id, NormalMode)
    } else {
      routes.AuthorisedLocationController.onPageLoad(ua.id, NormalMode)
    }
  }
}
