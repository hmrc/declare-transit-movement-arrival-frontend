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
import models.UserAnswers
import models.domain.{Container, Transhipment}
import org.jsoup.nodes.Document
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.events.transhipments.ContainerNumberPage
import play.api.libs.json.Json
import uk.gov.hmrc.viewmodels.SummaryList.Row
import utils.AddContainerHelper
import viewModels.Section

class AddContainerViewSpec extends ViewSpecBase with DomainModelGenerators with ScalaCheckPropertyChecks {

  "addContainer must display a rows for each " in {
    forAll(listWithMaxLength[Container](Transhipment.Constants.maxContainers)) {
      containers =>
        val ua: UserAnswers = containers.zipWithIndex.foldLeft(emptyUserAnswers) {
          case (userAnswers, (Container(number), index)) =>
            userAnswers.set(ContainerNumberPage(eventIndex, index), number).success.value
        }

        // TODO: create an arbitrary Row to replace this logic. We don't need to generate
        val rows: Seq[Row] = (0 to containers.length).flatMap(AddContainerHelper(ua).containerRow(eventIndex, _))
        val json = Json.obj(
          "containers" -> Some(Section(rows))
        )

        val doc: Document = renderDocument("events/transhipments/addContainer.njk", json).futureValue

        doc.getElementsByClass("govuk-summary-list__row").size() mustEqual containers.length

    }
  }
}
