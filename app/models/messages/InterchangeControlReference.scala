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

import models.XMLWrites
import play.twirl.api.utils.StringEscapeUtils

import scala.xml.NodeSeq

case class InterchangeControlReference(date: String, index: Int)

object InterchangeControlReference {

  implicit val writes: XMLWrites[InterchangeControlReference] = new XMLWrites[InterchangeControlReference] {
    override def writes(a: InterchangeControlReference): NodeSeq =
      <IntConRefMES11> {StringEscapeUtils.escapeXml11(s"WE${a.date}${a.index}")} </IntConRefMES11>
  }
}
