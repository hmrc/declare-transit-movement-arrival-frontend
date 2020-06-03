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

import com.lucidchart.open.xtract.XmlReader
import models.XMLWrites
import models.XMLWrites._
import cats.syntax.all._
import com.lucidchart.open.xtract.XmlReader._
import com.lucidchart.open.xtract.{__, XmlReader}
import models.XMLReads._

import scala.xml.{Elem, Node, NodeSeq}

case class ArrivalMovementRequest(meta: Meta,
                                  header: Header,
                                  traderDestination: TraderDestination,
                                  customsOfficeOfPresentation: CustomsOfficeOfPresentation,
                                  enRouteEvents: Option[Seq[EnRouteEvent]])

object ArrivalMovementRequest {

  implicit def writes: XMLWrites[ArrivalMovementRequest] = XMLWrites[ArrivalMovementRequest] {
    arrivalRequest =>
      val parentNode: Node = <CC007A></CC007A>

      val childNodes: NodeSeq = {
        arrivalRequest.meta.toXml ++
          arrivalRequest.header.toXml ++
          arrivalRequest.traderDestination.toXml ++
          arrivalRequest.customsOfficeOfPresentation.toXml ++ {
          arrivalRequest.enRouteEvents.map(_.flatMap(_.toXml)).getOrElse(NodeSeq.Empty)
        }
      }

      Elem(parentNode.prefix, parentNode.label, parentNode.attributes, parentNode.scope, parentNode.child.isEmpty, parentNode.child ++ childNodes: _*)
  }

  implicit val xmlReads: XmlReader[ArrivalMovementRequest] = ???
}
