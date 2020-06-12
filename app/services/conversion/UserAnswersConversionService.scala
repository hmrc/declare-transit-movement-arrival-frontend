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

import models.GoodsLocation.BorderForceOffice
import models.messages._
import models.reference.{Country, CustomsOffice}
import models.{Address, Index, UserAnswers}
import pages._
import pages.events._
import play.api.libs.json.JsObject

import scala.util.Try

class UserAnswersConversionService {

  def convertToUserAnswers(arrivalNotification: ArrivalNotification): Option[UserAnswers] =
    arrivalNotification match {

      case normalNotification: NormalNotification =>
        val userAnswers: UserAnswers = UserAnswers(normalNotification.movementReferenceNumber)
        (for {
          ua <- userAnswers.set(PresentationOfficePage,
                                CustomsOffice(normalNotification.presentationOfficeId, normalNotification.presentationOfficeName, Nil, None))
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
        } yield
          enRouteEvents(normalNotification, userAnswers) match {
            case Some(eventObj) => ua7.copy(data = ua7.data ++ eventObj)
            case _              => ua7
          }).toOption
      case _ => None
    }

  def setEventDetsils(userAnswers: UserAnswers, event: EnRouteEvent, index: Int): Try[UserAnswers] =
    event.eventDetails match {
      case Some(incident: Incident)                           => userAnswers.set(IncidentInformationPage(Index(index)), incident.information.getOrElse(""))
      case Some(vehicularTranshipment: VehicularTranshipment) => ???
      case Some(containerTranshipment: ContainerTranshipment) => ???
      case _                                                  => Try(userAnswers)
    }

  private def enRouteEvents(normalNotification: NormalNotification, userAnswers: UserAnswers): Option[JsObject] =
    normalNotification.enRouteEvents.flatMap {
      events =>
        (events.zipWithIndex flatMap {
          case (event, index) =>
            (for {
              ua1 <- userAnswers.set(EventPlacePage(Index(index)), event.place)
              ua2 <- ua1.set(EventCountryPage(Index(index)), Country("active", event.countryCode, "United Kingdom")) //TODO need to fetch it from reference data service
              ua3 <- ua2.set(EventReportedPage(Index(index)), event.alreadyInNcts)
              ua4 <- ua3.set(IsTranshipmentPage(Index(index)), false) //TODO investigate the logic around this
              ua5 <- setEventDetsils(ua4, event, index)
            } yield ua5).toOption
        }).map(_.data).headOption
    }
}
