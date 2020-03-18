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

package models.messages

import models._
import java.time.LocalDate
import play.api.libs.json._

final case class ArrivalNotificationRejectionMessage(
  movementReferenceNumber: String,
  rejectionDate: LocalDate,
  action: Option[String],
  reason: Option[String],
  errors: Seq[RejectionError]
)

object ArrivalNotificationRejectionMessage {

  implicit lazy val reads: Reads[ArrivalNotificationRejectionMessage] = {

    import play.api.libs.functional.syntax._

    (
      (__ \ "movementReferenceNumber").read[String] and
        (__ \ "rejectionDate").read[LocalDate] and
        (__ \ "action").readNullable[String] and
        (__ \ "reason").readNullable[String] and
        ((__ \ "errors").read[Seq[RejectionError]] or Reads.pure(Seq[RejectionError]()))
    )(ArrivalNotificationRejectionMessage(_, _, _, _, _))
  }

  implicit lazy val writes: OWrites[ArrivalNotificationRejectionMessage] =
    OWrites[ArrivalNotificationRejectionMessage] {
      rejection =>
        Json
          .obj(
            "movementReferenceNumber" -> rejection.movementReferenceNumber,
            "rejectionDate"           -> rejection.rejectionDate,
            "reason"                  -> rejection.reason,
            "action"                  -> rejection.action,
            "errors"                  -> rejection.errors
          )
          .filterNulls
    }
}
