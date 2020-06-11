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

package services.conversion

import java.time.LocalDate

import models.messages.{ArrivalNotification, NormalNotification, Trader}
import models.reference.CustomsOffice
import models.{Address, UserAnswers}
import pages._

class UserAnswersConversionService {


  def convertToUserAnswers(arrivalNotification: ArrivalNotification): UserAnswers = {
???
//    arrivalNotification match {
//
//      case normalNotification: NormalNotification =>
//        for {
//          ua <- UserAnswers(normalNotification.movementReferenceNumber)
//            .set(PresentationOfficePage, CustomsOffice(normalNotification.presentationOfficeId,
//              normalNotification.presentationOfficeName, Nil, None))
//          ua1 <- ua
//            .set(CustomsSubPlacePage, normalNotification.customsSubPlace.getOrElse(""))
//          ua2 <- ua1
//            .set(TraderAddressPage, traderAddress(normalNotification.trader))
//          ua3 <- ua2
//              .(TraderEoriPage, normalNotification. )
//        }
//          for {
//
//            presentationOffice <- userAnswers.get(PresentationOfficePage)
//            customsSubPlace <- userAnswers.get(CustomsSubPlacePage)
//            tradersAddress <- userAnswers.get(TraderAddressPage)
//            traderEori <- userAnswers.get(TraderEoriPage)
//            traderName <- userAnswers.get(TraderNamePage)
//            notificationPlace <- userAnswers.get(PlaceOfNotificationPage) orElse Some(tradersAddress.postcode)
//          } yield {
//            NormalNotification(
//              movementReferenceNumber = userAnswers.id,
//              notificationPlace = notificationPlace,
//              notificationDate = LocalDate.now(),
//              customsSubPlace = Some(customsSubPlace),
//              trader = traderAddress(tradersAddress, traderEori, traderName),
//              presentationOfficeId = presentationOffice.id,
//              presentationOfficeName = presentationOffice.name,
//              enRouteEvents = enRouteEvents(userAnswers)
//            )
//
//          }
//    }
  }

    private def traderWithEori(trader: Trader): Address

    = trader match {
      case traderWithEori: TraderWithEori => Address(traderWithEori.streetAndNumber.getOrElse(""), traderWithEori.city.getOrElse(""), traderWithEori.postCode.getOrElse(""))
      case traderWithoutEori: TraderWithoutEori => Address(traderWithoutEori.streetAndNumber, traderWithoutEori.city, traderWithoutEori.postCode)
    }
  }
