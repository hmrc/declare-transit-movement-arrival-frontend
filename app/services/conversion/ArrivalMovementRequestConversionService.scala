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

import cats.implicits._
import com.google.inject.Inject
import connectors.ReferenceDataConnector
import models.MovementReferenceNumber
import models.domain.{EnRouteEventDomain, EventDetailsDomain, NormalNotification}
import models.messages._
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class ArrivalMovementRequestConversionService @Inject()(referenceDataConnector: ReferenceDataConnector) {

  def convertToArrivalNotification(arrivalMovementRequest: ArrivalMovementRequest)(implicit ec: ExecutionContext,
                                                                                   hc: HeaderCarrier): Future[Option[NormalNotification]] =
    MovementReferenceNumber(arrivalMovementRequest.header.movementReferenceNumber) traverse {
      mrn =>
        val buildEnrouteEvents: Future[Option[Seq[EnRouteEventDomain]]] = arrivalMovementRequest.enRouteEvents.traverse {
          events =>
            Future.sequence(events.map(buildEnRouteEventDomain))
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

  private def buildEnRouteEventDomain(enRouteEvent: EnRouteEvent)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[EnRouteEventDomain] =
    referenceDataConnector.getCountry(enRouteEvent.countryCode).flatMap {
      eventCountry =>
        buildEventDetailsDomain(enRouteEvent.eventDetails).map {
          eventDetails =>
            EnRouteEvent.enRouteEventToDomain(enRouteEvent, eventCountry, eventDetails)
        }
    }

  private def buildEventDetailsDomain(eventDetails: Option[EventDetails])(implicit hc: HeaderCarrier,
                                                                          ec: ExecutionContext): Future[Option[EventDetailsDomain]] =
    eventDetails.traverse {
      case vehicularTranshipment: VehicularTranshipment => {
        referenceDataConnector.getCountry(vehicularTranshipment.transportCountry).map {
          country =>
            VehicularTranshipment.vehicularTranshipmentToDomain(vehicularTranshipment, country)
        }
      }
      case incident: Incident                           => Future.successful(Incident.incidentToDomain(incident))
      case containerTranshipment: ContainerTranshipment => Future.successful(ContainerTranshipment.containerTranshipmentToDomain(containerTranshipment))
    }
}
