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
import models.TranshipmentType._
import models.domain.{ContainerDomain, SealDomain}
import models.reference.{Country, CountryCode}
import models.{CheckMode, CountryList, Index}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.IncidentOnRoutePage
import pages.events._
import pages.events.seals.{HaveSealsChangedPage, SealIdentityPage}
import pages.events.transhipments._
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.viewmodels.SummaryList.Row
import viewModels.sections.Section

// format: off

class CheckEventAnswersViewModelSpec extends SpecBase with ScalaCheckPropertyChecks with MessagesModelGenerators {
  "must be able to deserialize to a JsObject" in {
    val vm = CheckEventAnswersViewModel(Seq(Section(Seq.empty[Row])))

    Json.toJsObject(vm) mustBe a[JsObject]
  }

  val genCountryList = arbitrary[Seq[Country]].sample.value
  val countryList = CountryList(genCountryList)

  "when event is an incident" - {
    "and hasn't been reported and did not move to different vehicle/container and no seals changed" in {

      val ua = emptyUserAnswers
        .set(IncidentOnRoutePage, true).success.value
        .set(EventCountryPage(eventIndex), CountryCode("GB")).success.value
        .set(EventPlacePage(eventIndex), "value").success.value
        .set(EventReportedPage(eventIndex), false).success.value
        .set(IsTranshipmentPage(eventIndex), false).success.value
        .set(IncidentInformationPage(eventIndex), "value").success.value
        .set(HaveSealsChangedPage(eventIndex), false).success.value

      val vm = CheckEventAnswersViewModel(ua, eventIndex, CheckMode, countryList)

      vm.sections.head.sectionTitle must not be defined
      vm.sections.length mustEqual 2
      vm.sections.head.rows.length mustEqual 5
    }

    "and has been reported, did not move to different vehicle/container and no seals changed" in {
      val ua = emptyUserAnswers
        .set(IncidentOnRoutePage, true).success.value
        .set(EventCountryPage(eventIndex), CountryCode("GB")).success.value
        .set(EventPlacePage(eventIndex), "value").success.value
        .set(EventReportedPage(eventIndex), false).success.value
        .set(IsTranshipmentPage(eventIndex), false).success.value
        .set(IncidentInformationPage(eventIndex), "value").success.value
        .set(HaveSealsChangedPage(eventIndex), false).success.value

      val vm = CheckEventAnswersViewModel(ua, eventIndex, CheckMode, countryList)

      vm.sections.head.sectionTitle must not be defined
      vm.sections.head.rows.length mustEqual 5
    }

    "and has been reported, did not move to different vehicle/container and seals changed" in {
      val ua = emptyUserAnswers
        .set(IncidentOnRoutePage, true).success.value
        .set(EventCountryPage(eventIndex), CountryCode("GB")).success.value
        .set(EventPlacePage(eventIndex), "value").success.value
        .set(EventReportedPage(eventIndex), false).success.value
        .set(IsTranshipmentPage(eventIndex), false).success.value
        .set(IncidentInformationPage(eventIndex), "value").success.value
        .set(HaveSealsChangedPage(eventIndex), true).success.value
        .set(SealIdentityPage(eventIndex, Index(0)), SealDomain("seal1")).success.value
        .set(SealIdentityPage(eventIndex, Index(1)), SealDomain("seal2")).success.value

      val vm = CheckEventAnswersViewModel(ua, eventIndex, CheckMode, countryList)

      vm.sections.head.sectionTitle must not be defined
      vm.sections.head.rows.length mustEqual 5
    }

    "and has been reported and did not move to different vehicle/container show the event info only" in {
      val ua = emptyUserAnswers
        .set(IncidentOnRoutePage, true).success.value
        .set(EventCountryPage(eventIndex), CountryCode("GB")).success.value
        .set(EventPlacePage(eventIndex), "value").success.value
        .set(EventReportedPage(eventIndex), true).success.value
        .set(IsTranshipmentPage(eventIndex), false).success.value
        .set(HaveSealsChangedPage(eventIndex), false).success.value

      val vm = CheckEventAnswersViewModel(ua, eventIndex, CheckMode, countryList)

      vm.sections.head.sectionTitle must not be defined
      vm.sections.head.rows.length mustEqual 4
    }
  }

  "when event is a transhipment" - {
    "and the goods have moved to different vehicle display event info and vehicle info sections" in {
      val ua = emptyUserAnswers
        .set(IncidentOnRoutePage, true).success.value
        .set(EventCountryPage(eventIndex), CountryCode("GB")).success.value
        .set(EventPlacePage(eventIndex), "value").success.value
        .set(EventReportedPage(eventIndex), false).success.value
        .set(IsTranshipmentPage(eventIndex), true).success.value
        .set(TranshipmentTypePage(eventIndex), DifferentVehicle).success.value
        .set(TransportIdentityPage(eventIndex), "value").success.value
        .set(TransportNationalityPage(eventIndex), CountryCode("GB")).success.value
        .set(HaveSealsChangedPage(eventIndex), false).success.value

      val vm = CheckEventAnswersViewModel(ua, eventIndex, CheckMode, countryList)

      vm.sections.head.sectionTitle must not be defined
      vm.sections.head.rows.length mustEqual 3
      vm.sections(1).sectionTitle must be(defined)
      vm.sections(1).rows.length mustEqual 4
    }

    "and the goods have moved to different container display event info and container info sections" in {
      val ua = emptyUserAnswers
        .set(IncidentOnRoutePage, true).success.value
        .set(EventCountryPage(eventIndex), CountryCode("GB")).success.value
        .set(EventPlacePage(eventIndex), "value").success.value
        .set(EventReportedPage(eventIndex), false).success.value
        .set(IsTranshipmentPage(eventIndex), true).success.value
        .set(TranshipmentTypePage(eventIndex), DifferentContainer).success.value
        .set(ContainerNumberPage(eventIndex, Index(0)), ContainerDomain("value")).success.value
        .set(ContainerNumberPage(eventIndex, Index(1)), ContainerDomain("value")).success.value
        .set(ContainerNumberPage(eventIndex, Index(2)), ContainerDomain("value")).success.value
        .set(HaveSealsChangedPage(eventIndex), false).success.value

      val vm = CheckEventAnswersViewModel(ua, eventIndex, CheckMode, countryList)
      vm.sections.head.sectionTitle must not be defined
      vm.sections.head.rows.length mustEqual 3
      vm.sections(1).sectionTitle must be(defined)
      vm.sections(1).rows.length mustEqual 2
    }

    "and the goods have moved to both different containers and vehicles  display event info and vehicle and containers info sections" in {
      val ua = emptyUserAnswers
        .set(IncidentOnRoutePage, true).success.value
        .set(EventCountryPage(eventIndex), CountryCode("GB")).success.value
        .set(EventPlacePage(eventIndex), "value").success.value
        .set(EventReportedPage(eventIndex), false).success.value
        .set(IsTranshipmentPage(eventIndex), true).success.value
        .set(TranshipmentTypePage(eventIndex), DifferentContainerAndVehicle).success.value
        .set(ContainerNumberPage(eventIndex, Index(0)), ContainerDomain("value")).success.value
        .set(ContainerNumberPage(eventIndex, Index(1)), ContainerDomain("value")).success.value
        .set(ContainerNumberPage(eventIndex, Index(2)), ContainerDomain("value")).success.value
        .set(TransportIdentityPage(eventIndex), "value").success.value
        .set(TransportNationalityPage(eventIndex), CountryCode("GB")).success.value
        .set(HaveSealsChangedPage(eventIndex), false).success.value

      val vm = CheckEventAnswersViewModel(ua, eventIndex, CheckMode, countryList)

      vm.sections.length mustEqual 5
      vm.sections.head.sectionTitle must not be defined
      vm.sections(1).sectionTitle must be(defined)
      vm.sections(1).rows.length mustEqual 2
      vm.sections(2).sectionTitle must be(defined)
      vm.sections(2).rows.length mustEqual 3

    }

  }

}
// format: on
