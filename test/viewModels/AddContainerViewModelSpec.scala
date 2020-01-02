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
import generators.DomainModelGenerators
import models.domain.Container
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.events.transhipments.ContainerNumberPage
import play.api.libs.json.Json
import uk.gov.hmrc.viewmodels.Text.Literal
import uk.gov.hmrc.viewmodels._

class AddContainerViewModelSpec extends SpecBase with ScalaCheckPropertyChecks with DomainModelGenerators {
  "pageTitle" - {
    "pageTitle defaults to 0 when there are no containers" in {
      val vm = AddContainerViewModel(eventIndex, emptyUserAnswers)

      assert(vm.pageTitle.resolve.contains("0"))
    }

    "has the number of containers" in {
      forAll(arbitrary[Seq[Container]]) {
        containers =>
          val userAnswers = containers.zipWithIndex.foldLeft(emptyUserAnswers) {
            case (ua, (container, containerIndex)) =>
              ua.set(ContainerNumberPage(eventIndex, containerIndex), container).success.value
          }

          val vm = AddContainerViewModel(eventIndex, userAnswers)

          assert(vm.pageTitle.resolve.contains(containers.length.toString))
      }
    }
  }

  "containers" - {
    "is empty when there are no containers is UserAnswer" in {
      val vm = AddContainerViewModel(eventIndex, emptyUserAnswers)

      vm.containers must not be (defined)
    }

    "has the number of containers" in {
      forAll(arbitrary[Seq[Container]]) {
        containers =>
          val userAnswers = containers.zipWithIndex.foldLeft(emptyUserAnswers) {
            case (ua, (container, containerIndex)) =>
              ua.set(ContainerNumberPage(eventIndex, containerIndex), container).success.value
          }

          val vm = AddContainerViewModel(eventIndex, userAnswers)

          vm.containers must be(defined)

          val rows: Seq[Content] = vm.containers.value.rows.map(_.value.content)

          rows.length mustEqual containers.length

          containers.foreach(c => {
            rows must contain(Literal(c.containerNumber))
          })
      }
    }

  }
}
