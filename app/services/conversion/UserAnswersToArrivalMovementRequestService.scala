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

import java.time.LocalTime

import config.FrontendAppConfig
import javax.inject.Inject
import models.{EoriNumber, UserAnswers}
import models.messages.{ArrivalMovementRequest, MessageSender}
import repositories.InterchangeControlReferenceIdRepository

import scala.concurrent.{ExecutionContext, Future}

class UserAnswersToArrivalMovementRequestService @Inject()(
  appConfig: FrontendAppConfig,
  converterService: UserAnswersToArrivalNotificationDomain,
  interchangeControlReferenceIdRepository: InterchangeControlReferenceIdRepository)(implicit ec: ExecutionContext) {

  def convert(userAnswers: UserAnswers): Option[Future[ArrivalMovementRequest]] =
    converterService.convertToArrivalNotification(userAnswers).map {
      notification =>
        val messageSender = MessageSender(appConfig.env, EoriNumber(notification.trader.eori))

        interchangeControlReferenceIdRepository
          .nextInterchangeControlReferenceId()
          .map {
            referenceId =>
              SubmissionModelService
                .convertToSubmissionModel(
                  arrivalNotification         = notification,
                  messageSender               = messageSender,
                  interchangeControlReference = referenceId,
                  timeOfPresentation          = LocalTime.now()
                )
          }
    }

}
