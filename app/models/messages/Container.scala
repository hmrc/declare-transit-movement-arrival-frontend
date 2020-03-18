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

import forms.mappings.StringEquivalence
import helpers.XmlBuilderHelper
import play.api.libs.json.{Json, OFormat}

import scala.xml.Node

case class Container(containerNumber: String) extends XmlBuilderHelper {

  def toXml: Node =
    <CONNR3>
      {buildAndEncodeElem(containerNumber, "ConNumNR31")}
    </CONNR3>

}

object Container {

  object Constants {
    val containerNumberLength = 17
  }

  implicit val formats: OFormat[Container] = Json.format[Container]

  implicit val containerStringEquivalenceCheck: StringEquivalence[Container] =
    StringEquivalence[Container]((container, stringContainer) => container.containerNumber == stringContainer)
}
