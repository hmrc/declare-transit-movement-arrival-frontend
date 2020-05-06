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

package models

import java.time.LocalDate

import com.lucidchart.open.xtract.{ParseError, ParseFailure, ParseResult, ParseSuccess, XmlReader}
import utils.Format.dateFormatter

import scala.util.{Failure, Success, Try}
import scala.xml.NodeSeq

object XMLReads {

  case class LocalDateParseFailure(message: String) extends ParseError

  implicit val xmlDateReads: XmlReader[LocalDate] = {
    new XmlReader[LocalDate] {
      override def read(xml: NodeSeq): ParseResult[LocalDate] =
        Try(LocalDate.parse(xml.text, dateFormatter)) match {
          case Success(value) => ParseSuccess(value)
          case Failure(e)     => ParseFailure(LocalDateParseFailure(e.getMessage))
        }
    }
  }
}