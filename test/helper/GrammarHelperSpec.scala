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

package helper

import base.SpecBase
import uk.gov.hmrc.viewmodels.Text
import utils.ImplicitGrammarConversion._

class GrammarHelperSpec extends SpecBase {

  val testKey = "test.key"

  "GrammarHelper" - {

    "singularOrPlural" - {

      "return plural message text if condition is true" in {
        singularOrPlural(key = testKey, condition = true) mustBe Text.Message("test.key.plural")
      }

      "return singular message text if condition is false" in {
        singularOrPlural(key = testKey, condition = false) mustBe Text.Message("test.key.singular")
      }
    }

    "singularOrPluralWithArgs" - {

      val arguments = 1

      "return plural message text if condition is true" in {
        singularOrPluralWithArgs(key = testKey, condition = true, arguments) mustBe Text.Message("test.key.plural", 1)
      }

      "return singular message text if condition is false" in {
        singularOrPluralWithArgs(key = testKey, condition = false, arguments) mustBe Text.Message("test.key.singular", 1)
      }
    }
  }

}