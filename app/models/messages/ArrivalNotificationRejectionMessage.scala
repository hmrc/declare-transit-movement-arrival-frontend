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

package models.messages

import java.time.LocalDate

import cats.syntax.all._
import com.lucidchart.open.xtract.XmlReader._
import com.lucidchart.open.xtract.{__, XmlReader}
import models.XMLReads._

final case class ArrivalNotificationRejectionMessage(
  movementReferenceNumber: String,
  rejectionDate: LocalDate,
  action: Option[String],
  reason: Option[String],
  errors: Seq[FunctionalError]
)

object ArrivalNotificationRejectionMessage {

  implicit val xmlReader: XmlReader[ArrivalNotificationRejectionMessage] = (
    (__ \ "HEAHEA" \ "DocNumHEA5").read[String],
    (__ \ "HEAHEA" \ "ArrRejDatHEA142").read[LocalDate],
    (__ \ "HEAHEA" \ "ActToBeTakHEA238").read[String].optional,
    (__ \ "HEAHEA" \ "ArrRejReaHEA242").read[String].optional,
    (__ \ "FUNERRER1").read(strictReadSeq[FunctionalError])
  ).mapN(apply)
}
