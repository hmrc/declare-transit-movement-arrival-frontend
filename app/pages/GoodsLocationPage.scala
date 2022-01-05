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

package pages

import models.{GoodsLocation, UserAnswers}
import play.api.libs.json.JsPath

import scala.util.Try

case object GoodsLocationPage extends QuestionPage[GoodsLocation] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "goodsLocation"

  override def cleanup(value: Option[GoodsLocation], userAnswers: UserAnswers): Try[UserAnswers] =
    value match {
      case Some(GoodsLocation.AuthorisedConsigneesLocation) =>
        userAnswers
          .remove(CustomsSubPlacePage)
          .flatMap(_.remove(CustomsOfficePage))
          .flatMap(_.remove(TraderNamePage))
          .flatMap(_.remove(TraderEoriPage))
          .flatMap(_.remove(TraderAddressPage))
          .flatMap(_.remove(IsTraderAddressPlaceOfNotificationPage))
          .flatMap(_.remove(PlaceOfNotificationPage))

      case Some(GoodsLocation.BorderForceOffice) =>
        userAnswers
          .remove(AuthorisedLocationPage)
          .flatMap(_.remove(ConsigneeNamePage))
          .flatMap(_.remove(CustomsOfficePage))
          .flatMap(_.remove(ConsigneeEoriNumberPage))
          .flatMap(_.remove(ConsigneeAddressPage))

      case None => super.cleanup(value, userAnswers)
    }
}
