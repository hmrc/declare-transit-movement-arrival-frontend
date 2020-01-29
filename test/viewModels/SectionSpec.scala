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

package viewModels

import base.SpecBase
import play.api.libs.json.JsNull
import play.api.libs.json.Json
import uk.gov.hmrc.viewmodels.SummaryList.Action
import uk.gov.hmrc.viewmodels.SummaryList.Key
import uk.gov.hmrc.viewmodels.SummaryList.Row
import uk.gov.hmrc.viewmodels.SummaryList.Value
import uk.gov.hmrc.viewmodels.{NunjucksSupport, Text}
import viewModels.sections.Section

class SectionSpec extends SpecBase with NunjucksSupport {

  "Section" - {
    "must serialise to Json" in {
      val key          = Key(lit"foo")
      val value        = Value(lit"bar")
      val action       = Action(lit"baz", "quux")
      val sectionTitle = lit"Section title"

      val expectedSection = Json.obj(
        "sectionTitle" -> sectionTitle.resolve,
        "rows" -> Json.arr(
          Json.obj(
            "key"   -> key,
            "value" -> value,
            "actions" -> Json.obj(
              "items" -> Json.arr(
                action
              )
            )
          )
        )
      )

      val section = Section(
        sectionTitle = Some(sectionTitle),
        rows = Seq(
          Row(
            key   = key,
            value = value,
            actions = List(
              action
            )
          )
        )
      )

      Json.toJson(section) mustBe expectedSection
    }

    "must serialise empty section" in {
      Json.toJson(Section(None, Nil)) mustBe Json.obj("sectionTitle" -> JsNull, "rows" -> Json.arr())
    }
  }

}
