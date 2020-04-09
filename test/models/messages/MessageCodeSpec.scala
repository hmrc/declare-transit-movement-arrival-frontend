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

import models.XMLWrites._
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.{FreeSpec, MustMatchers}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import scala.xml.Node
import scala.xml.Utility.trim

class MessageCodeSpec extends FreeSpec with MustMatchers with ScalaCheckPropertyChecks {

  "MessageCode" - {
    "must create valid xml" in {

      forAll(arbitrary[String]) {
        code =>
          val messageCode: MessageCode = MessageCode(code)
          val expectedResult: Node     = <MesTypMES20>{code}</MesTypMES20>

          messageCode.toXml.map(trim) mustBe expectedResult.map(trim)
      }
    }
  }

}
