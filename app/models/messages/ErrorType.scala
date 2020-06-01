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

import com.lucidchart.open.xtract._
import models.Enumerable

import scala.xml.NodeSeq

sealed trait ErrorType {
  val code: Int
}

object ErrorType extends Enumerable.Implicits {

  sealed trait GenericError extends ErrorType
  sealed trait MRNError     extends ErrorType

  case object UnknownMrn   extends MRNError {val code: Int = 90}
  case object DuplicateMrn extends MRNError {val code: Int = 91}
  case object InvalidMrn   extends MRNError {val code: Int = 93}


  val values = Seq(
    UnknownMrn,
    DuplicateMrn,
    InvalidMrn
  )

  implicit val xmlDateReads: XmlReader[ErrorType] = {
    new XmlReader[ErrorType] {
      override def read(xml: NodeSeq): ParseResult[ErrorType] = {

        case class ErrorTypeParseError(message: String) extends ParseError

        values.find(x => x.code.toString == xml.text) match {
          case Some(error) => ParseSuccess(error)
          case None        => ParseFailure(ErrorTypeParseError(s"Invalid or missing ErrorType: ${xml.text}"))
        }
      }
    }
  }

}
