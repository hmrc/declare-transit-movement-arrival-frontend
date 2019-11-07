/*
 * Copyright 2019 HM Revenue & Customs
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

package utils

import base.SpecBase
import pages._
import uk.gov.hmrc.viewmodels.Text.{Literal, Message}

class CheckYourAnswersHelperSpec extends SpecBase {

  "CheckYourAnswersHelper" - {
    "traderName row is generated with correct meesafge key and value with a given useransewrs" in {
      val traderName = "TraderName"
      val ua = emptyUserAnswers.set[String](TraderNamePage, traderName).success.value
      val helper =  new CheckYourAnswersHelper(ua)

      helper.traderName.get.key.content mustBe Message("traderName.checkYourAnswersLabel")
      helper.traderName.get.value.content mustBe Literal(traderName)
    }
  }

}
