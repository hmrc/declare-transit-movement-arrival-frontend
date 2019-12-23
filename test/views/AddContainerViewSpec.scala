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

import generators.DomainModelGenerators
import models.domain.Container
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.scalacheck.Arbitrary.arbitrary
import pages.events.transhipments.ContainerNumberPage
import play.api.libs.json.{JsObject, Json}
import play.twirl.api.Html
import uk.gov.hmrc.viewmodels.SummaryList
import utils.AddContainerHelper
import viewModels.Section

import scala.concurrent.Future

class AddContainerViewSpec extends ViewSpecBase with DomainModelGenerators {

  "addContainer must have a section with rows" in {
    val containterNumber = arbitrary[Container].sample.value.containerNumber
    val ua               = emptyUserAnswers.set(ContainerNumberPage(eventIndex, containerIndex), containterNumber).success.value

    val containerRow              = AddContainerHelper(ua).containerRow(eventIndex, containerIndex).value
    val containerSection: Section = Section(Seq(containerRow))
    val json = Json.obj(
      "containers" -> containerSection
    )

    val doc: Document = renderDocument("events/transhipments/addContainer.njk", json).futureValue

    doc.getElementsByClass("govuk-summary-list__row").size() mustEqual 1
  }
}
