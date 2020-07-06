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
import models.{EoriNumber, XMLWrites}

import scala.xml.NodeSeq

case class MessageSender(environment: String, eori: EoriNumber)

object MessageSender {

  implicit val writes: XMLWrites[MessageSender] =
    XMLWrites(
      a => <MesSenMES3>{escapeXml(s"${a.environment}-${a.eori.value}")}</MesSenMES3>
    )

  implicit val xmlMessageSenderReads: XmlReader[MessageSender] = {
    new XmlReader[MessageSender] {

      case class MessageSenderParseFailure(message: String) extends ParseError

      override def read(xml: NodeSeq): ParseResult[MessageSender] =
        xml.text.split("-") match {
          case Array(environment, eori) => ParseSuccess(MessageSender(environment, EoriNumber(eori)))
          case _                        => ParseFailure(MessageSenderParseFailure(s"Failed to parse the following value to MessageSender: ${xml.text}"))
        }
    }
  }

}
