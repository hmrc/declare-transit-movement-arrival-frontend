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

import com.lucidchart.open.xtract._
import logging.Logging
import models.Enumerable
import play.api.libs.json.{JsNumber, Writes}

sealed trait ErrorType {
  val code: Int
}

object ErrorType extends Enumerable.Implicits with Logging {

  sealed abstract class GenericError(val code: Int) extends ErrorType
  sealed abstract class MRNError(val code: Int) extends ErrorType

  case class UnknownErrorType(override val code: Int) extends GenericError(code)

  case object IncorrectValue extends GenericError(12)
  case object MissingValue extends GenericError(13)
  case object ValueNotSupported extends GenericError(14)
  case object NotSupportedPosition extends GenericError(15)
  case object InvalidDecimal extends GenericError(19)
  case object DuplicateDetected extends GenericError(26)
  case object TooManyRepetitions extends GenericError(35)
  case object InvalidTypeCharacters extends GenericError(37)
  case object MissingDigit extends GenericError(38)
  case object ElementTooLong extends GenericError(39)
  case object ElementTooShort extends GenericError(40)

  case object UnknownMrn extends MRNError(90)
  case object DuplicateMrn extends MRNError(91)
  case object InvalidMrn extends MRNError(93)

  val mrnValues = Seq(
    UnknownMrn,
    DuplicateMrn,
    InvalidMrn
  )

  val genericValues = Seq(
    IncorrectValue,
    MissingValue,
    ValueNotSupported,
    NotSupportedPosition,
    InvalidDecimal,
    DuplicateDetected,
    TooManyRepetitions,
    InvalidTypeCharacters,
    MissingDigit,
    ElementTooLong,
    ElementTooShort
  )

  implicit val writes: Writes[ErrorType] = Writes[ErrorType] {
    case genericError: GenericError => JsNumber(genericError.code)
    case mrnError: MRNError         => JsNumber(mrnError.code)
  }

  implicit val xmlErrorTypeReads: XmlReader[ErrorType] =
    XmlReader.of[Int].map {
      code =>
        (mrnValues ++ genericValues).find(
          knownError => knownError.code == code
        ) match {
          case Some(errorType) => errorType
          case None =>
            logger.warn(s"[read] No known error type found instead found errorType: $code")
            UnknownErrorType(code)
        }
    }

}
