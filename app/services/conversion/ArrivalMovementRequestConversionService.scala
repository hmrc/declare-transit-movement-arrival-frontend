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

import com.google.inject.Inject
import connectors.ReferenceDataConnector
import models.MovementReferenceNumber
import models.domain.{ArrivalNotificationDomain, EnRouteEventDomain, NormalNotification}
import models.messages.{ArrivalMovementRequest, EnRouteEvent, Trader}
import uk.gov.hmrc.http.HeaderCarrier
import cats.implicits._

import scala.concurrent.{ExecutionContext, Future}

class ArrivalMovementRequestConversionService @Inject()(referenceDataConnector: ReferenceDataConnector) {

  def convertToArrivalNotification(arrivalMovementRequest: ArrivalMovementRequest)(implicit ec: ExecutionContext,
                                                                                   hc: HeaderCarrier): Future[Option[ArrivalNotificationDomain]] =
    MovementReferenceNumber(arrivalMovementRequest.header.movementReferenceNumber) traverse {
      mrn =>
        val buildEnrouteEvents: Future[Option[Seq[EnRouteEventDomain]]] = arrivalMovementRequest.enRouteEvents.traverse {
          events =>
            Future.sequence(
              events.map {
                event =>
                  referenceDataConnector.getCountry(event.countryCode).map {
                    country =>
                      EnRouteEvent.enRouteEventToDomain(event, country)
                  }
              }
            )
        }

        buildEnrouteEvents.map {
          enRouteEvents =>
            NormalNotification(
              mrn,
              arrivalMovementRequest.header.arrivalNotificationPlace,
              arrivalMovementRequest.header.notificationDate,
              arrivalMovementRequest.header.customsSubPlace.get, // TODO need to address the case when there is no subsplace
              Trader.messagesTraderToDomainTrader(arrivalMovementRequest.trader),
              arrivalMovementRequest.header.presentationOfficeId,
              arrivalMovementRequest.header.presentationOfficeName,
              enRouteEvents
            )
        }
    }
}
