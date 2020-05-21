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

package models

import org.scalatest.{FreeSpec, MustMatchers}
import play.api.libs.json.Json

class MessageActionsSpec extends FreeSpec with MustMatchers {

  "MessageActionsSpec" - {
    "De-serialise to Model" in {

      val messageActions = MessageActions(ArrivalId(123), MessageAction("/movements/arrivals/1234/messages/3", Some("/movements/arrivals/1234/messages/5")))

      val json = Json.obj("arrivalId" -> 123,
                          "messages" -> Json.obj(
                            "IE007" -> "/movements/arrivals/1234/messages/3",
                            "IE008" -> "/movements/arrivals/1234/messages/5"
                          ))

      json.as[MessageActions] mustBe messageActions
    }
  }
}
