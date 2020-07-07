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

import javax.inject.Inject
import models.{ArrivalId, EoriNumber, MovementReferenceNumber, UserAnswers}
import repositories.SessionRepository
import services.conversion.ArrivalMovementRequestToUserAnswersService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class UserAnswersService @Inject()(arrivalNotificationMessageService: ArrivalNotificationMessageService, sessionRepository: SessionRepository)(
  implicit ec: ExecutionContext) {

  def getUserAnswers(arrivalId: ArrivalId, eoriNumber: EoriNumber)(implicit hc: HeaderCarrier): Future[Option[UserAnswers]] =
    arrivalNotificationMessageService
      .getArrivalNotificationMessage(arrivalId)
      .map(_.flatMap(ArrivalMovementRequestToUserAnswersService.apply(_, eoriNumber)))

  def getOrCreateUserAnswers(eoriNumber: EoriNumber, value: MovementReferenceNumber): Future[UserAnswers] = {
    val initialUserAnswers = UserAnswers(id = value, eoriNumber = eoriNumber)

    sessionRepository.get(id = value.toString, eoriNumber = eoriNumber) map {
      userAnswers =>
        userAnswers getOrElse initialUserAnswers
    }
  }
}
