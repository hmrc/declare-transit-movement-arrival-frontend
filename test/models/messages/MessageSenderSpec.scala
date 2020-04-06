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

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.{FreeSpec, MustMatchers}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import scala.xml.XML.loadString

class MessageSenderSpec extends FreeSpec with MustMatchers with ScalaCheckPropertyChecks {

  "MessageSender" - {

    "must convert to xml and convert to correct format" in {

      val environment: Gen[String] = Gen.oneOf(Seq("LOCAL", "QA", "STAGING", "PRODUCTION"))

      forAll(arbitrary[String], environment) {
        (eori, environment) =>
          val expectedResult = <MesSenMES3>{s"$environment-$eori"}</MesSenMES3>

          val result = MessageSender(environment, eori).toXml

          result mustBe loadString(expectedResult.toString)
      }
    }
  }
}