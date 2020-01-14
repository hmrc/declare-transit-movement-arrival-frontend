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

import derivable.{DeriveNumberOfContainers, DeriveNumberOfSeals}
import models.TranshipmentType._
import models.UserAnswers
import pages.events._
import pages.events.seals.HaveSealsChangedPage
import pages.events.transhipments.TranshipmentTypePage
import play.api.i18n.Messages
import play.api.libs.json.{Json, OWrites}
import uk.gov.hmrc.viewmodels.SummaryList.Row
import uk.gov.hmrc.viewmodels.{NunjucksSupport, Text}
import utils.{AddContainerHelper, CheckYourAnswersHelper}

case class CheckEventAnswersViewModel(eventInfo: Section, otherInfo: Seq[Section])

//noinspection ScalaStyle
object CheckEventAnswersViewModel extends NunjucksSupport {

  def apply(userAnswers: UserAnswers, eventIndex: Int): CheckEventAnswersViewModel = {
    val helper = new CheckYourAnswersHelper(userAnswers)

    val isTranshipment = userAnswers.get(IsTranshipmentPage(eventIndex)).getOrElse(false)

    def eventTypeSection(sectionText: Text): Seq[Section] =
      Seq(
        Some(Section(sectionText, Seq(helper.isTranshipment(eventIndex), helper.transhipmentType(eventIndex)).flatten)),
        userAnswers
          .get(DeriveNumberOfContainers(eventIndex))
          .map(List.range(0, _))
          .map(_.flatMap(AddContainerHelper(userAnswers).containerRow(eventIndex, _)))
          .map(Section.apply(msg"checkEventAnswers.section.title.containerNumbers", _))
      ).flatten

    val eventInfo: Seq[Row] =
      Seq(
        helper.eventCountry(eventIndex),
        helper.eventPlace(eventIndex),
        helper.eventReported(eventIndex),
        if (isTranshipment) None else { helper.isTranshipment(eventIndex) },
        helper.incidentInformation(eventIndex)
      ).flatten

    val differentVehicleSection: Section = Section(
      msg"checkEventAnswers.section.title.differentVehicle",
      Seq(
        if (isTranshipment) { helper.isTranshipment(eventIndex) } else None,
        helper.transhipmentType(eventIndex),
        helper.transportIdentity(eventIndex),
        helper.transportNationality(eventIndex)
      ).flatten
    )

    val sealSection: Option[Section] = {
      userAnswers
        .get(HaveSealsChangedPage(eventIndex))
        .map {
          haveSealsChanged =>
              Seq(
                helper.haveSealsChanged(eventIndex),
                if (haveSealsChanged) {
                  userAnswers.get(DeriveNumberOfSeals(eventIndex)).map { size =>
                    List.range(0, size).map {
                      helper.sealIdentity(eventIndex, _)
                    }
                  }
                } else None
              ).flatten
        }
    }

    val otherInfoSections: Seq[Section] = {
      userAnswers
        .get(TranshipmentTypePage(eventIndex))
        .map {
          case DifferentVehicle   => Seq(differentVehicleSection)
          case DifferentContainer => eventTypeSection(msg"checkEventAnswers.section.title.differentContainer")
          case DifferentContainerAndVehicle =>
            eventTypeSection(msg"checkEventAnswers.section.title.differentContainerAndVehicle") :+
              Section(
                msg"checkEventAnswers.section.title.vehicleInformation",
                Seq(
                  helper.transportIdentity(eventIndex),
                  helper.transportNationality(eventIndex)
                ).flatten
              )
        }
        .getOrElse(Seq.empty)
    }

    CheckEventAnswersViewModel(
      Section(eventInfo),
      otherInfoSections ++ sealSection.toSeq
    )
  }

  implicit def writes(implicit messages: Messages): OWrites[CheckEventAnswersViewModel] = Json.writes[CheckEventAnswersViewModel]
}
