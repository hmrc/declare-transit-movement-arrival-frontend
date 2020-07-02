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

package services

import java.time.LocalTime

import config.FrontendAppConfig
import connectors.ArrivalMovementConnector
import javax.inject.Inject
import models.{EoriNumber, UserAnswers}
import models.messages.MessageSender
import play.api.Logger
import repositories.InterchangeControlReferenceIdRepository
import services.conversion.{ArrivalNotificationConversionService, SubmissionModelService}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.{ExecutionContext, Future}

class ArrivalSubmissionService @Inject()(
  converterService: ArrivalNotificationConversionService,
  connector: ArrivalMovementConnector,
  appConfig: FrontendAppConfig,
  submissionModelService: SubmissionModelService,
  interchangeControlReferenceIdRepository: InterchangeControlReferenceIdRepository
)(implicit ec: ExecutionContext) {

  def submit(userAnswers: UserAnswers, eori: EoriNumber)(implicit hc: HeaderCarrier): Future[Option[HttpResponse]] =
    converterService.convertToArrivalNotification(userAnswers) match {
      case Some(notification) =>
        val messageSender = MessageSender(appConfig.env, eori.value)

        interchangeControlReferenceIdRepository
          .nextInterchangeControlReferenceId()
          .flatMap {
            referenceId =>
              val arrivalMovementRequest = {
                submissionModelService
                  .convertToSubmissionModel(
                    arrivalNotification         = notification,
                    messageSender               = messageSender,
                    interchangeControlReference = referenceId,
                    timeOfPresentation          = LocalTime.now()
                  )
              }
              userAnswers.arrivalId match {
                case Some(arrivalId) => connector.updateArrivalMovement(arrivalId, arrivalMovementRequest).map(Some(_))
                case _               => connector.submitArrivalMovement(arrivalMovementRequest).map(Some(_))

              }
          }
          .recover {
            case ex =>
              Logger.error(s"${ex.getMessage}")
              None
          }

      case None => Future.successful(None)
    }
}
