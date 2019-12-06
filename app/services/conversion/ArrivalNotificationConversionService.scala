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

import models.domain._
import models.domain.messages.{ArrivalNotification, NormalNotification}
import models.{TraderAddress, UserAnswers}
import pages._

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
        information          = incidentInformation,
        endorsementDate      = None,
        endorsementAuthority = None,
        endorsementPlace     = None,
        endorsementCountry   = None
      )
    }

  private def enRouteEvents(userAnswers: UserAnswers): Option[Seq[EnRouteEvent]] =
    (for {
      place          <- userAnswers.get(EventPlacePage)
      country        <- userAnswers.get(EventCountryPage)
      isReported     <- userAnswers.get(EventReportedPage)
      isTranshipment <- userAnswers.get(IsTranshipmentPage)
    } yield {
      Some(
        Seq(
          EnRouteEvent(
            place         = place,
            countryCode   = country,
            alreadyInNcts = isReported,
            eventDetails  = eventDetails(isTranshipment, userAnswers.get(IncidentInformationPage)),
            None //TODO Seals
          ))
      )
    }).flatten

  private def traderAddress(traderAddress: TraderAddress, traderEori: String, traderName: String): Trader =
    TraderWithEori(
      eori            = traderEori,
      name            = Some(traderName),
      streetAndNumber = Some(traderAddress.buildingAndStreet),
      postCode        = Some(traderAddress.postcode),
      city            = Some(traderAddress.city),
      countryCode     = Some(countryCode_GB)
    )
}
