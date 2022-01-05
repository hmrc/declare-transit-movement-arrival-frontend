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
import models.domain.SealDomain
import models.messages.behaviours.JsonBehaviours
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, StreamlinedXmlEquality}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import scala.xml.Node

class SealSpec
    extends AnyFreeSpec
    with Matchers
    with ScalaCheckPropertyChecks
    with ModelGenerators
    with JsonBehaviours
    with StreamlinedXmlEquality
    with OptionValues {

  "Seal" - {
    "must create valid xml" in {

      forAll(arbitrary[Seal]) {
        seal =>
          val expectedXml: Node =
            <SEAIDSI1>
              <SeaIdeSI11>
                {seal.numberOrMark}
              </SeaIdeSI11>
              <SeaIdeSI11LNG>
                {Seal.Constants.languageCode.code}
              </SeaIdeSI11LNG>
            </SEAIDSI1>

          seal.toXml mustEqual expectedXml
      }
    }

    "must read xml as Seal" in {
      forAll(arbitrary[Seal]) {
        seal =>
          val xml: Node =
            <SEAIDSI1>
              <SeaIdeSI11>{seal.numberOrMark}</SeaIdeSI11>
              <SeaIdeSI11LNG>{Seal.Constants.languageCode.code}</SeaIdeSI11LNG>
            </SEAIDSI1>

          val result = XmlReader.of[Seal].read(xml).toOption.value
          result mustEqual seal
      }
    }

    "must read and write xml" in {
      forAll(arbitrary[Seal]) {
        seal =>
          val result = XmlReader.of[Seal].read(seal.toXml).toOption.value
          result mustEqual seal
      }

    }

    "must convert to SealDomain" in {
      forAll(arbitrary[Seal]) {
        seal =>
          Seal.sealToDomain(seal) mustBe an[SealDomain]
      }
    }
  }
}
