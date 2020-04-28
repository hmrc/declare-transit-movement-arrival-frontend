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

import generators.MessagesModelGenerators
import models.NormalProcedureFlag
import models.XMLWrites._
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.{FreeSpec, MustMatchers, OptionValues, StreamlinedXmlEquality}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

import scala.xml.Utility.trim
import scala.xml.{Node, NodeSeq}

class ArrivalMovementRequestSpec
    extends FreeSpec
    with MustMatchers
    with GuiceOneAppPerSuite
    with MessagesModelGenerators
    with ScalaCheckDrivenPropertyChecks
    with OptionValues
    with StreamlinedXmlEquality {

  "ArrivalMovementRequest" - {
    "must create valid xml" in {
      forAll(arbitrary[ArrivalMovementRequest]) {
        arrivalMovementRequest =>
          whenever(hasEoriWithNormalProcedure(arrivalMovementRequest)) {

            val validXml: Node =
              <CC007A>
                {arrivalMovementRequest.meta.toXml ++
                arrivalMovementRequest.header.toXml ++
                arrivalMovementRequest.traderDestination.toXml ++
                arrivalMovementRequest.customsOfficeOfPresentation.toXml ++
                arrivalMovementRequest.enRouteEvents.map(_.flatMap(_.toXml)).getOrElse(NodeSeq.Empty)}
              </CC007A>

            arrivalMovementRequest.toXml mustEqual validXml
          }
      }
    }
  }

  private def hasEoriWithNormalProcedure(arrivalMovementRequest: ArrivalMovementRequest): Boolean =
    arrivalMovementRequest.traderDestination.eori.isDefined &&
      arrivalMovementRequest.header.procedureTypeFlag.equals(NormalProcedureFlag)
}
