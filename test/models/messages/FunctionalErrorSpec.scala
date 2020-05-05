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

import base.SpecBase
import com.lucidchart.open.xtract.XmlReader
import generators.MessagesModelGenerators
import models.FunctionalError
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class FunctionalErrorSpec extends SpecBase with ScalaCheckDrivenPropertyChecks with MessagesModelGenerators {

  "deserialization from XML" - {
    "must deserialize to a FunctionalError" in {
      forAll(arbitrary[FunctionalError]) {
        functionalError =>
          val error = functionalError.copy(reason = Some("test"), originalAttributeValue = Some("test"))

          val xml = {
            <FUNERRER1>
              <ErrTypER11>{error.errorType.value}</ErrTypER11>
              <ErrPoiER12>{error.pointer.value}</ErrPoiER12>
              <ErrReaER13>{error.reason.get}</ErrReaER13>
              <OriAttValER14>{error.originalAttributeValue.get}</OriAttValER14>
            </FUNERRER1>
          }

          val result = XmlReader.of[FunctionalError].read(xml).toOption.value

          result mustEqual error
      }
    }
  }
}
