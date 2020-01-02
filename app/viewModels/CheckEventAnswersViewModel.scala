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

import derivable.DeriveNumberOfContainers
import models.TranshipmentType._
import models.UserAnswers
import pages.events.IsTranshipmentPage
import pages.events.transhipments.TranshipmentTypePage
import play.api.i18n.Messages
import play.api.libs.json.{Json, OWrites}
import uk.gov.hmrc.viewmodels.NunjucksSupport
import uk.gov.hmrc.viewmodels.SummaryList.Row
import utils.{AddContainerHelper, CheckYourAnswersHelper}

case class CheckEventAnswersViewModel(eventInfo: Section, otherInfo: Seq[Section])

object CheckEventAnswersViewModel extends NunjucksSupport {

  def eventInfo(helper: CheckYourAnswersHelper, eventIndex: Int): Seq[Row] =
    Seq(
      helper.eventCountry(eventIndex),
      helper.eventPlace(eventIndex),
      helper.eventReported(eventIndex),
      helper.isTranshipment(eventIndex),
      helper.incidentInformation(eventIndex)
    ).flatten

  def differentVehicleSection(helper: CheckYourAnswersHelper, eventIndex: Int): Section = Section(
    msg"checkEventAnswers.section.title.differentVehicle",
    Seq(helper.isTranshipment(eventIndex),
      helper.transhipmentType(eventIndex),
      helper.transportIdentity(eventIndex),
      helper.transportNationality(eventIndex)
    ).flatten
  )

  def apply(userAnswers: UserAnswers, eventIndex: Int): CheckEventAnswersViewModel = {
    val helper = new CheckYourAnswersHelper(userAnswers)

    def eventTypeSection(key: String): Seq[Section] =
      Seq(
        Some(Section(msg"$key", Seq(helper.isTranshipment(eventIndex), helper.transhipmentType(eventIndex)).flatten)),
        userAnswers
          .get(DeriveNumberOfContainers(eventIndex))
          .map(List.range(0, _))
          .map(_.flatMap(AddContainerHelper(userAnswers).containerRow(eventIndex, _)))
          .map(Section.apply(msg"checkEventAnswers.section.title.containerNumbers", _))
      ).flatten

    val otherInfoSections: Seq[Section] = (userAnswers.get(IsTranshipmentPage(eventIndex)), userAnswers.get(TranshipmentTypePage(eventIndex))) match {
      case (Some(true), Some(DifferentVehicle)) =>
        Seq(differentVehicleSection(helper, eventIndex))
      case (Some(true), Some(DifferentContainer)) =>
        eventTypeSection("checkEventAnswers.section.title.differentContainer")
      case (Some(true), Some(DifferentContainerAndVehicle)) =>
        eventTypeSection("checkEventAnswers.section.title.differentContainerAndVehicle") ++
          Some(
            Section(
              msg"checkEventAnswers.section.title.vehicleInformation",
              Seq(
                helper.transportIdentity(eventIndex),
                helper.transportNationality(eventIndex)
              ).flatten
            ))
      case _ => Seq.empty
    }

    CheckEventAnswersViewModel(
      Section(eventInfo(helper, eventIndex)),
      otherInfoSections
    )
  }

  implicit def writes(implicit messages: Messages): OWrites[CheckEventAnswersViewModel] = Json.writes[CheckEventAnswersViewModel]
}
