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

import models.GoodsLocation.BorderForceOffice
import models.messages.{ArrivalNotification, NormalNotification, Trader}
import models.reference.CustomsOffice
import models.{Address, UserAnswers}
import pages._

import scala.util.{Success, Try}

class UserAnswersConversionService {

  def convertToUserAnswers(arrivalNotification: ArrivalNotification): Option[UserAnswers] =
    arrivalNotification match {

      case normalNotification: NormalNotification =>
        (for {
          ua <- UserAnswers(normalNotification.movementReferenceNumber)
            .set(PresentationOfficePage, CustomsOffice(normalNotification.presentationOfficeId, normalNotification.presentationOfficeName, Nil, None))
          ua1 <- ua
            .set(CustomsSubPlacePage, normalNotification.customsSubPlace.getOrElse(""))
          ua2 <- ua1
            .set(TraderAddressPage, Address(normalNotification.trader.streetAndNumber, normalNotification.trader.city, normalNotification.trader.postCode))
          ua3 <- ua2
            .set(TraderEoriPage, normalNotification.trader.eori)
          ua4 <- ua3
            .set(TraderNamePage, normalNotification.trader.name)
          ua5 <- ua4
            .set(PlaceOfNotificationPage, normalNotification.notificationPlace) //TODO: userAnswers.get(PlaceOfNotificationPage) orElse Some(tradersAddress.postcode)
          ua6 <- ua5
            .set(GoodsLocationPage, BorderForceOffice)
          ua7 <- ua6
            .set(IncidentOnRoutePage, normalNotification.enRouteEvents.isDefined)
        } yield ua7).toOption
      case _ => None
    }

}
