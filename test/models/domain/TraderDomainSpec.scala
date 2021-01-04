/*
 * Copyright 2021 HM Revenue & Customs
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

package models.domain

import generators.MessagesModelGenerators
import models.messages.Trader
import models.messages.behaviours.JsonBehaviours
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.{FreeSpec, MustMatchers}

class TraderDomainSpec extends FreeSpec with MustMatchers with MessagesModelGenerators with JsonBehaviours {

  "must convert to Trader model" in {
    forAll(arbitrary[TraderDomain]) {
      traderDomain =>
        TraderDomain.domainTraderToMessagesTrader(traderDomain) mustBe an[Trader]
    }
  }
}
