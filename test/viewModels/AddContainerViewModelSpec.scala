/*
 * Copyright 2022 HM Revenue & Customs
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
import generators.MessagesModelGenerators
import models.domain.ContainerDomain
import models.{Index, NormalMode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.events.transhipments.ContainerNumberPage
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.viewmodels.Text.Message
import uk.gov.hmrc.viewmodels._

class AddContainerViewModelSpec extends SpecBase with ScalaCheckPropertyChecks with MessagesModelGenerators {
  "must be able to deserialize to a JsObject" in {
    val vm = AddContainerViewModel(msg"Title", None)

    Json.toJsObject(vm) mustBe a[JsObject]
  }

  "pageTitle" - {
    "pageTitle defaults to 0 when there are no containers" in {
      val vm = AddContainerViewModel(eventIndex, emptyUserAnswers, NormalMode)

      vm.pageTitle mustEqual Message("addContainer.title.plural", 0)
    }

    "has the number of containers" in {
      forAll(arbitrary[Seq[ContainerDomain]]) {
        containers =>
          val userAnswers = containers.zipWithIndex.foldLeft(emptyUserAnswers) {
            case (ua, (container, containerIndex)) =>
              ua.set(ContainerNumberPage(eventIndex, Index(containerIndex)), container).success.value
          }

          val vm = AddContainerViewModel(eventIndex, userAnswers, NormalMode)

          if (containers.size == 1) {
            vm.pageTitle mustEqual Message("addContainer.title.singular", 1)
          } else {
            vm.pageTitle mustEqual Message("addContainer.title.plural", containers.size)
          }
      }
    }
  }

  "containers" - {
    "is empty when there are no containers is UserAnswer" in {
      val vm = AddContainerViewModel(eventIndex, emptyUserAnswers, NormalMode)

      vm.containers must not be defined
    }

    "has the number of containers" in {
      forAll(arbitrary[Seq[ContainerDomain]]) {
        containers =>
          val userAnswers = containers.zipWithIndex.foldLeft(emptyUserAnswers) {
            case (ua, (container, containerIndex)) =>
              ua.set(ContainerNumberPage(eventIndex, Index(containerIndex)), container).success.value
          }

          val vm = AddContainerViewModel(eventIndex, userAnswers, NormalMode)

          vm.containers must be(defined)

          val rows = vm.containers.value.rows

          rows.length mustEqual containers.length
      }
    }

  }
}
