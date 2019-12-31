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

package views

import generators.{DomainModelGenerators, ViewModelGenerators}
import org.jsoup.nodes.Document
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json
import viewModels.Section

class AddContainerViewSpec extends ViewSpecBase with DomainModelGenerators with ViewModelGenerators with ScalaCheckPropertyChecks {

  "addContainer must display a rows for each " in {
    forAll(arbitrary[Section]) {
      section =>
        val json = Json.obj(
          "containers" -> Some(section)
        )

        val doc: Document = renderDocument("events/transhipments/addContainer.njk", json).futureValue

        doc.getElementsByClass("govuk-summary-list__row").size() mustEqual section.rows.length

    }
  }
}
