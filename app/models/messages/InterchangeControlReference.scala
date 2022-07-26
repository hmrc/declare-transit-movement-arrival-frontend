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
import models.XMLWrites

import scala.util.matching.Regex
import scala.xml.NodeSeq

case class InterchangeControlReference(date: String, index: Int)

object InterchangeControlReference {

  private val prefix = "AF"

  implicit val writes: XMLWrites[InterchangeControlReference] =
    XMLWrites {
      a =>
        <IntConRefMES11>{s"$prefix${a.date}${a.index}"}</IntConRefMES11>
    }

  implicit val interchangeControlReferenceXmlReads: XmlReader[InterchangeControlReference] =
    (xml: NodeSeq) => {

      case class InterchangeControlReferenceParseFailure(message: String) extends ParseError

      val controlReferenceFormat: Regex = (prefix + """(\d{8})(\d*)""").r

      xml.text match {
        case controlReferenceFormat(date, index) =>
          ParseSuccess(InterchangeControlReference(date, index.toInt))
        case _ =>
          ParseFailure(InterchangeControlReferenceParseFailure(s"Failed to parse the following value to InterchangeControlReference: ${xml.text}"))
      }
    }

}
