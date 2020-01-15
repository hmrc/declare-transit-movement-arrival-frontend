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
import generators.MessagesModelGenerators
import models.CheckMode
import models.TranshipmentType._
import models.messages.Container
import models.reference.Country
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.IncidentOnRoutePage
import pages.events._
import pages.events.seals.{HaveSealsChangedPage, SealIdentityPage}
import pages.events.transhipments._
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.viewmodels.SummaryList.Row

// format: off

class CheckEventAnswersViewModelSpec extends SpecBase with ScalaCheckPropertyChecks with MessagesModelGenerators {
  "must be able to deserialize to a JsObject" in {
    val vm = CheckEventAnswersViewModel(Section(Seq.empty[Row]), Seq(Section(Seq.empty[Row])))

    Json.toJsObject(vm) mustBe a[JsObject]
  }

  "when event is an incident" - {
    "and hasn't been reported and did not move to different vehicle/container and no seals changed" in {
      val ua = emptyUserAnswers
        .set(IncidentOnRoutePage, true).success.value
        .set(EventCountryPage(eventIndex), Country("Valid", "value", "Country Name")).success.value
        .set(EventPlacePage(eventIndex), "value").success.value
        .set(EventReportedPage(eventIndex), false).success.value
        .set(IsTranshipmentPage(eventIndex), false).success.value
        .set(IncidentInformationPage(eventIndex), "value").success.value
        .set(HaveSealsChangedPage(eventIndex), false).success.value

      val vm = CheckEventAnswersViewModel(ua, eventIndex, CheckMode)

      vm.eventInfo.sectionTitle must not be defined
      vm.eventInfo.rows.length mustEqual 5
      vm.otherInfo.size mustEqual 1
    }

    "and has been reported, did not move to different vehicle/container and no seals changed" in {
      val ua = emptyUserAnswers
        .set(IncidentOnRoutePage, true).success.value
        .set(EventCountryPage(eventIndex), Country("Valid", "value", "Country Name")).success.value
        .set(EventPlacePage(eventIndex), "value").success.value
        .set(EventReportedPage(eventIndex), false).success.value
        .set(IsTranshipmentPage(eventIndex), false).success.value
        .set(IncidentInformationPage(eventIndex), "value").success.value
        .set(HaveSealsChangedPage(eventIndex), false).success.value

      val vm = CheckEventAnswersViewModel(ua, eventIndex)

      vm.eventInfo.sectionTitle must not be defined
      vm.eventInfo.rows.length mustEqual 5
      vm.otherInfo.size mustEqual 1
    }

    "and has been reported, did not move to different vehicle/container and seals changed" in {
      val ua = emptyUserAnswers
        .set(IncidentOnRoutePage, true).success.value
        .set(EventCountryPage(eventIndex), Country("Valid", "value", "Country Name")).success.value
        .set(EventPlacePage(eventIndex), "value").success.value
        .set(EventReportedPage(eventIndex), false).success.value
        .set(IsTranshipmentPage(eventIndex), false).success.value
        .set(IncidentInformationPage(eventIndex), "value").success.value
        .set(HaveSealsChangedPage(eventIndex), true).success.value
        .set(SealIdentityPage(eventIndex, 0), "seal1").success.value
        .set(SealIdentityPage(eventIndex, 1), "seal2").success.value

      val vm = CheckEventAnswersViewModel(ua, eventIndex)

      vm.eventInfo.sectionTitle must not be defined
      vm.eventInfo.rows.length mustEqual 5
      vm.otherInfo.head.rows.size mustEqual 3
    }

    "and has been reported and did not move to different vehicle/container show the event info only" in {
      val ua = emptyUserAnswers
        .set(IncidentOnRoutePage, true).success.value
        .set(EventCountryPage(eventIndex), Country("Valid", "value", "Country Name")).success.value
        .set(EventPlacePage(eventIndex), "value").success.value
        .set(EventReportedPage(eventIndex), true).success.value
        .set(IsTranshipmentPage(eventIndex), false).success.value

      val vm = CheckEventAnswersViewModel(ua, eventIndex, CheckMode)

      vm.eventInfo.sectionTitle must not be defined
      vm.eventInfo.rows.length mustEqual 4
      vm.otherInfo must be(empty)
    }
  }

  "when event is a transhipment" - {
    "and the goods have moved to different vehicle display event info and vehicle info sections" in {
      val ua = emptyUserAnswers
        .set(IncidentOnRoutePage, true).success.value
        .set(EventCountryPage(eventIndex), Country("Valid", "value", "Country Name")).success.value
        .set(EventPlacePage(eventIndex), "value").success.value
        .set(EventReportedPage(eventIndex), false).success.value
        .set(IsTranshipmentPage(eventIndex), true).success.value
        .set(TranshipmentTypePage(eventIndex), DifferentVehicle).success.value
        .set(TransportIdentityPage(eventIndex), "value").success.value
        .set(TransportNationalityPage(eventIndex), Country("Valid","TT","Some country")).success.value

      val vm = CheckEventAnswersViewModel(ua, eventIndex, CheckMode)

      vm.eventInfo.sectionTitle must not be defined
      vm.eventInfo.rows.length mustEqual 3

      vm.otherInfo.length mustEqual 1
      vm.otherInfo.head.sectionTitle must be(defined)

      vm.otherInfo.head.rows.length mustEqual 4
    }

    "and the goods have moved to different container display event info and container info sections" in {
      val ua = emptyUserAnswers
        .set(IncidentOnRoutePage, true).success.value
        .set(EventCountryPage(eventIndex), Country("Valid", "value", "Country Name")).success.value
        .set(EventPlacePage(eventIndex), "value").success.value
        .set(EventReportedPage(eventIndex), false).success.value
        .set(IsTranshipmentPage(eventIndex), true).success.value
        .set(TranshipmentTypePage(eventIndex), DifferentContainer).success.value
        .set(ContainerNumberPage(eventIndex, 0), Container("value")).success.value
        .set(ContainerNumberPage(eventIndex, 1), Container("value")).success.value
        .set(ContainerNumberPage(eventIndex, 2), Container("value")).success.value

      val vm = CheckEventAnswersViewModel(ua, eventIndex, CheckMode)

      vm.eventInfo.sectionTitle must not be defined
      vm.eventInfo.rows.length mustEqual 3

      vm.otherInfo.length mustEqual 2

      vm.otherInfo.head.sectionTitle must be(defined)
      vm.otherInfo.head.rows.length mustEqual 2

      vm.otherInfo(1).sectionTitle must be(defined)
      vm.otherInfo(1).rows.length mustEqual 3
    }

    "and the goods have moved to both different containers and vehicles  display event info and vehicle and containers info sections" in {
      val ua = emptyUserAnswers
        .set(IncidentOnRoutePage, true).success.value
        .set(EventCountryPage(eventIndex), Country("Valid", "value", "Country Name")).success.value
        .set(EventPlacePage(eventIndex), "value").success.value
        .set(EventReportedPage(eventIndex), false).success.value
        .set(IsTranshipmentPage(eventIndex), true).success.value
        .set(TranshipmentTypePage(eventIndex), DifferentContainerAndVehicle).success.value
        .set(ContainerNumberPage(eventIndex, 0), Container("value")).success.value
        .set(ContainerNumberPage(eventIndex, 1), Container("value")).success.value
        .set(ContainerNumberPage(eventIndex, 2), Container("value")).success.value
        .set(TransportIdentityPage(eventIndex), "value").success.value
        .set(TransportNationalityPage(eventIndex), Country("Valid","TT","Some country")).success.value

      val vm = CheckEventAnswersViewModel(ua, eventIndex, CheckMode)

      vm.eventInfo.sectionTitle must not be defined
      vm.eventInfo.rows.length mustEqual 3

      vm.otherInfo.length mustEqual 3

      vm.otherInfo.head.sectionTitle must be(defined)
      vm.otherInfo.head.rows.length mustEqual 2

      vm.otherInfo(1).sectionTitle must be(defined)
      vm.otherInfo(1).rows.length mustEqual 3

      vm.otherInfo(2).sectionTitle must be(defined)
      vm.otherInfo(2).rows.length mustEqual 2
    }
  }

  "generate a view with no other info section" - {
    "when TranshipmentTypePage is missing" in {
      val ua = emptyUserAnswers
        .set(IncidentOnRoutePage, true).success.value
        .set(EventCountryPage(eventIndex), Country("Valid", "value", "Country Name")).success.value
        .set(EventPlacePage(eventIndex), "value").success.value
        .set(IsTranshipmentPage(eventIndex), true).success.value
        .set(EventReportedPage(eventIndex), false).success.value

      val vm = CheckEventAnswersViewModel(ua, eventIndex, CheckMode)

      vm.eventInfo.sectionTitle must not be defined
      vm.eventInfo.rows.length mustEqual 3

      vm.otherInfo mustBe Seq.empty

    }
  }
}
// format: on
