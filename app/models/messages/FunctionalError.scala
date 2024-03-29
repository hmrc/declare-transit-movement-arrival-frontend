/*
 * Copyright 2023 HM Revenue & Customs
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

import cats.syntax.all._
import com.lucidchart.open.xtract.{__, XmlReader}
import play.api.libs.json.{Json, OWrites}
import models.messages.ErrorType._

final case class FunctionalError(
  errorType: ErrorType,
  pointer: ErrorPointer,
  reason: Option[String],
  originalAttributeValue: Option[String]
)

object FunctionalError {

  implicit val writes: OWrites[FunctionalError] = Json.writes[FunctionalError]

  implicit val xmlReader: XmlReader[FunctionalError] = (
    (__ \ "ErrTypER11").read[ErrorType],
    (__ \ "ErrPoiER12").read[ErrorPointer],
    (__ \ "ErrReaER13").read[String].optional,
    (__ \ "OriAttValER14").read[String].optional
  ).mapN(apply)
}
