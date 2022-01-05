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

import base.SpecBase
import com.lucidchart.open.xtract.{ParseFailure, XmlReader}
import generators.MessagesModelGenerators
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.StreamlinedXmlEquality
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class ProcedureTypeFlagSpec extends SpecBase with ScalaCheckPropertyChecks with MessagesModelGenerators with StreamlinedXmlEquality {

  //format off
  "ProcedureTypeFlag" - {

    "must deserialize from xml" in {
      forAll(arbitrary[ProcedureTypeFlag]) {
        flag =>
          val xml    = <testXml>{flag.code}</testXml>
          val result = XmlReader.of[ProcedureTypeFlag].read(xml).toOption.value

          result mustBe flag
      }
    }

    "must fail to deserialize from xml if invalid format" in {

      val invalidXml = <testXml>Invalid format</testXml>
      val result     = XmlReader.of[ProcedureTypeFlag].read(invalidXml)

      result mustBe an[ParseFailure]
    }
  }
  // format: on
}
