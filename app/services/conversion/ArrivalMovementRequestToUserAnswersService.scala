/*
 * Copyright 2022 HM Revenue & Customs
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

import models.messages.ArrivalMovementRequest
import models.reference.CustomsOffice
import models.{EoriNumber, MovementReferenceNumber, UserAnswers}
import play.api.libs.json.Json

object ArrivalMovementRequestToUserAnswersService {

  def convertToUserAnswers(
    arrivalMovementRequest: ArrivalMovementRequest,
    eoriNumber: EoriNumber,
    movementReferenceNumber: MovementReferenceNumber,
    customsOffice: CustomsOffice
  ): Option[UserAnswers] =
    ArrivalMovementRequestToArrivalNotificationService
      .convertToArrivalNotification(arrivalMovementRequest, customsOffice, eoriNumber)
      .map {
        value =>
          UserAnswers(
            movementReferenceNumber = movementReferenceNumber,
            eoriNumber = eoriNumber,
            data = Json.toJsObject(value)
          )
      }
}
