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

package models

import com.lucidchart.open.xtract.{ParseError, ParseFailure, ParseResult, ParseSuccess, XmlReader}

import scala.xml.NodeSeq

sealed trait ProcedureTypeFlag {
  val code: String
}

case object SimplifiedProcedureFlag extends ProcedureTypeFlag {
  val code: String = "1"
}

case object NormalProcedureFlag extends ProcedureTypeFlag {
  val code: String = "0"
}

object ProcedureTypeFlag {

  implicit val procedureTypeFlagXmlReads: XmlReader[ProcedureTypeFlag] =
    new XmlReader[ProcedureTypeFlag] {

      override def read(xml: NodeSeq): ParseResult[ProcedureTypeFlag] = {

        case class ProcedureTypeFlagParseFailure(message: String) extends ParseError

        xml.text match {
          case NormalProcedureFlag.code     => ParseSuccess(NormalProcedureFlag)
          case SimplifiedProcedureFlag.code => ParseSuccess(SimplifiedProcedureFlag)
          case _ =>
            ParseFailure(ProcedureTypeFlagParseFailure(s"Failed to parse the following value to ProcedureTypeFlag: ${xml.text}"))
        }
      }
    }

}
