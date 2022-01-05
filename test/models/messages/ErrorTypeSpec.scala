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

import com.lucidchart.open.xtract.{ParseFailure, ParseSuccess}
import generators.MessagesModelGenerators
import models.messages.ErrorType.UnknownErrorType
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class ErrorTypeSpec extends AnyFreeSpec with ScalaCheckPropertyChecks with Matchers with MessagesModelGenerators {

  "ErrorType" - {
    "read integer as object" in {

      forAll(arbitrary[ErrorType]) {
        errorType =>
          val xml = <ErrTypER11>{errorType.code}</ErrTypER11>
          ErrorType.xmlErrorTypeReads.read(xml) mustBe ParseSuccess(errorType)
      }
    }

    "read anything not supported as UnknownErrorCode" in {
      val knownSet: Set[Int] = (ErrorType.genericValues ++ ErrorType.mrnValues).map(_.code).toSet
      forAll(arbitrary[Int].suchThat(!knownSet.apply(_))) {
        errorType =>
          val xml = <ErrTypER11>{errorType}</ErrTypER11>
          ErrorType.xmlErrorTypeReads.read(xml) mustBe ParseSuccess(UnknownErrorType(errorType))
      }
    }

    "return ParseFailureError for invalid value" in {
      val xml = <ErrTypER11>Invalid</ErrTypER11>
      ErrorType.xmlErrorTypeReads.read(xml) mustBe an[ParseFailure]
    }
  }

}
