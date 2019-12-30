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

import forms.events.transhipments.AddContainerFormProvider
import generators.DomainModelGenerators
import models.{MovementReferenceNumber, NormalMode}
import models.domain.Container
import org.jsoup.nodes.Document
import org.scalacheck.Arbitrary.arbitrary
import pages.events.transhipments.ContainerNumberPage
import play.api.data.Form
import play.api.libs.json.Json
import uk.gov.hmrc.viewmodels.Radios
import utils.AddContainerHelper
import viewModels.Section

class AddContainerViewSpec extends ViewSpecBase with DomainModelGenerators {

  private val form = {
    val fp = injector.instanceOf[AddContainerFormProvider]
    fp()
  }

  "addContainer must have a section with rows" ignore {
    val containterNumber = arbitrary[Container].sample.value.containerNumber
    val mrn              = arbitrary[MovementReferenceNumber].sample.value

    val ua                                = emptyUserAnswers.set(ContainerNumberPage(eventIndex, containerIndex), containterNumber).success.value
    val containerRow                      = AddContainerHelper(ua).containerRow(eventIndex, containerIndex).value
    val containerSection: Option[Section] = Some(Section(Seq(containerRow)))

    val json = Json.obj(
      "form"       -> form,
      "mode"       -> NormalMode,
      "mrn"        -> mrn,
      "radios"     -> Radios.yesNo(form("value")),
      "pageTitle"  -> "foo",
      "containers" -> containerSection
    )

    val doc: Document = renderDocument("events/transhipments/addContainer.njk", json).futureValue

    doc.getElementsByClass("govuk-summary-list__row").size() mustEqual 1
  }
}
