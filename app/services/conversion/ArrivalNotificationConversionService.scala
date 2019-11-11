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

package services.conversion

import java.time.LocalDate

import models.UserAnswers
import models.domain.{Trader, TraderWithEori}
import models.domain.messages.{ArrivalNotification, NormalNotification}
import pages.{CustomsSubPlacePage, PresentationOfficePage, TraderAddressPage, TraderEoriPage, TraderNamePage}

class ArrivalNotificationConversionService {

  def convertToArrivalNotification(userAnswers: UserAnswers): Option[ArrivalNotification] = {
    userAnswers.get(PresentationOfficePage) map {
      presentationOffice =>
        NormalNotification(
          userAnswers.id.value,
          "", //TODO notificationPlace
          LocalDate.now(),
          userAnswers.get(CustomsSubPlacePage),
          traderAddress(userAnswers),
          presentationOffice,
          Seq.empty
        )
    }
  }

  private def traderAddress(userAnswers: UserAnswers): Trader = {
    val traderAddress = userAnswers.get(TraderAddressPage)

    TraderWithEori(
      userAnswers.get(TraderEoriPage).getOrElse(""),
      userAnswers.get(TraderNamePage),
      traderAddress.map(_.buildingAndStreet),
      traderAddress.map(_.postcode),
      traderAddress.map(_.city),
      Some("GB")
    )
  }

}
