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

import models.{TraderAddress, UserAnswers}
import models.domain.{Trader, TraderWithEori}
import models.domain.messages.{ArrivalNotification, NormalNotification}
import pages.{CustomsSubPlacePage, PresentationOfficePage, TraderAddressPage, TraderEoriPage, TraderNamePage}

class ArrivalNotificationConversionService {

  val countryCode_GB = "GB"

  def convertToArrivalNotification(userAnswers: UserAnswers): Option[ArrivalNotification] = {

    for {
      presentationOffice <- userAnswers.get(PresentationOfficePage)
      customsSubPlace <- userAnswers.get(CustomsSubPlacePage)
      tradersAddress <- userAnswers.get(TraderAddressPage)
      traderEori <- userAnswers.get(TraderEoriPage)
      traderName <- userAnswers.get(TraderNamePage)
    } yield {
      NormalNotification(
        userAnswers.id.value,
        "", //TODO notificationPlace
        LocalDate.now(),
        Some(customsSubPlace),
        traderAddress(tradersAddress, traderEori, traderName),
        presentationOffice,
        Seq.empty //TODO EnRouteEvent conversion
      )
    }
  }


  private def traderAddress(traderAddress: TraderAddress, traderEori: String,
                            traderName: String): Trader =
    TraderWithEori(
      traderEori,
      Some(traderName),
      Some(traderAddress.buildingAndStreet),
      Some(traderAddress.postcode),
      Some(traderAddress.city),
      Some(countryCode_GB))
}
