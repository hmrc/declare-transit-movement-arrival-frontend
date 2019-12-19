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

import computable.DeriveNumberOfEvents
import models.domain._
import models.domain.messages.{ArrivalNotification, NormalNotification}
import models.{TraderAddress, UserAnswers}
import pages._
import pages.events._

class ArrivalNotificationConversionService {

  val countryCode_GB = "GB"

  def convertToArrivalNotification(userAnswers: UserAnswers): Option[ArrivalNotification] =
    for {
      presentationOffice <- userAnswers.get(PresentationOfficePage)
      customsSubPlace    <- userAnswers.get(CustomsSubPlacePage)
      tradersAddress     <- userAnswers.get(TraderAddressPage)
      traderEori         <- userAnswers.get(TraderEoriPage)
      traderName         <- userAnswers.get(TraderNamePage)
      notificationPlace  <- userAnswers.get(PlaceOfNotificationPage) orElse Some(tradersAddress.postcode)
    } yield {
      NormalNotification(
        movementReferenceNumber = userAnswers.id.toString,
        notificationPlace       = notificationPlace,
        notificationDate        = LocalDate.now(),
        customsSubPlace         = Some(customsSubPlace),
        trader                  = traderAddress(tradersAddress, traderEori, traderName),
        presentationOffice      = presentationOffice,
        enRouteEvents           = enRouteEvents(userAnswers)
      )
    }

  private def eventDetails(isTranshipment: Boolean, incidentInformation: Option[String]): EventDetails =
    if (isTranshipment) {
      ???
    } else {
      Incident(
        information = incidentInformation,
        endorsement = Endorsement(None, None, None, None) // TODO: Find out where this data comes from
      )
    }

  private def enRouteEvents(userAnswers: UserAnswers): Option[Seq[EnRouteEvent]] =
    userAnswers.get(DeriveNumberOfEvents).map {
      numberOfEvents =>
        (0 to numberOfEvents).flatMap {
          index =>
            for {
              place          <- userAnswers.get(EventPlacePage(index))
              country        <- userAnswers.get(EventCountryPage(index))
              isReported     <- userAnswers.get(EventReportedPage(index))
              isTranshipment <- userAnswers.get(IsTranshipmentPage(index))
              incidentInformation = userAnswers.get(IncidentInformationPage(index))
            } yield {
              EnRouteEvent(
                place         = place,
                countryCode   = country,
                alreadyInNcts = isReported,
                eventDetails  = eventDetails(isTranshipment, incidentInformation),
                None
              )
            }
        }
    }

  private def traderAddress(traderAddress: TraderAddress, traderEori: String, traderName: String): TraderWithEori =
    TraderWithEori(
      eori            = traderEori,
      name            = Some(traderName),
      streetAndNumber = Some(traderAddress.buildingAndStreet),
      postCode        = Some(traderAddress.postcode),
      city            = Some(traderAddress.city),
      countryCode     = Some(countryCode_GB)
    )
}
