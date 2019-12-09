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

package queries

import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours
import play.api.libs.json.{JsObject, JsString, Json}

class EventsQuerySpec extends PageBehaviours {

  "AddEventPage" - {

    implicit val asdf: Arbitrary[JsObject] = Arbitrary {
      for {
        (key, value) <- arbitrary[(String, String)]
      } yield Json.obj(key -> value)
    }

    beRetrievable[List[JsObject]](EventsQuery)

  }

}