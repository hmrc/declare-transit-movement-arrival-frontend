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

import com.lucidchart.open.xtract.XmlReader
import generators.ModelGenerators
import models.XMLWrites._
import models.domain.ContainerDomain
import models.messages.behaviours.JsonBehaviours
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, StreamlinedXmlEquality}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import scala.xml.Node

class ContainerSpec
    extends AnyFreeSpec
    with Matchers
    with ScalaCheckPropertyChecks
    with ModelGenerators
    with JsonBehaviours
    with StreamlinedXmlEquality
    with OptionValues {

  "Container" - {
    "must create valid xml" in {

      forAll(arbitrary[Container]) {
        container =>
          val expectedXml: Node =
            <CONNR3>
              <ConNumNR31>{container.containerNumber}</ConNumNR31>
            </CONNR3>

          container.toXml mustEqual expectedXml
      }
    }

    "must read xml as Container" in {
      forAll(arbitrary[Container]) {
        container =>
          val xml: Node =
            <CONNR3>
              <ConNumNR31>{container.containerNumber}</ConNumNR31>
            </CONNR3>

          val result = XmlReader.of[Container].read(xml).toOption.value
          result mustEqual container
      }
    }

    "must read and write xml" in {
      forAll(arbitrary[Container]) {
        container =>
          val result = XmlReader.of[Container].read(container.toXml).toOption.value
          result mustEqual container
      }

    }

    "must convert to ContainerDomain model" in {
      forAll(arbitrary[ContainerDomain]) {
        containerDomain =>
          val container = ContainerDomain.domainContainerToContainer(containerDomain)
          Container.containerToDomain(container) mustBe containerDomain
      }
    }
  }
}
