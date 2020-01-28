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
import models.{Mode, UserAnswers}
import pages.events._
import pages.events.transhipments.TranshipmentTypePage
import play.api.i18n.Messages
import play.api.libs.json.{Json, OWrites}
import uk.gov.hmrc.viewmodels.{NunjucksSupport, Text}
import utils.CheckYourAnswersHelper

case class CheckEventAnswersViewModel(sections: Seq[Section])

object CheckEventAnswersViewModel extends NunjucksSupport {

  def apply(userAnswers: UserAnswers, eventIndex: Int, mode: Mode): CheckEventAnswersViewModel = {

    implicit val helper: CheckYourAnswersHelper = new CheckYourAnswersHelper(userAnswers)

    implicit val isTranshipment: Boolean = userAnswers.get(IsTranshipmentPage(eventIndex)).getOrElse(false)

    val eventInfoSection =
      Section(
        Seq(
          helper.eventCountry(eventIndex),
          helper.eventPlace(eventIndex),
          helper.eventReported(eventIndex),
          if (isTranshipment) None else { helper.isTranshipment(eventIndex) },
          helper.incidentInformation(eventIndex)
        ).flatten)

    val sealSection: Section =
      Section(msg"addSeal.sealList.heading", Seq(helper.haveSealsChanged(eventIndex) ++ helper.seals(eventIndex)).flatten)

    CheckEventAnswersViewModel(
      Seq(eventInfoSection) ++ eventTypeSection(userAnswers, eventIndex) :+ sealSection
    )
  }

  private def eventTypeSection(userAnswers: UserAnswers, eventIndex: Int)(implicit isTranshipment: Boolean, helper: CheckYourAnswersHelper): Seq[Section] = {

    val differentVehicleSection: Seq[Section] = Seq(
      Section(
        msg"checkEventAnswers.section.title.differentVehicle",
        Seq(
          if (isTranshipment) { helper.isTranshipment(eventIndex) } else None,
          helper.transhipmentType(eventIndex),
          helper.transportIdentity(eventIndex),
          helper.transportNationality(eventIndex)
        ).flatten
      ))

    val vehicleInformationSection: Seq[Section] = Seq(
      Section(
        msg"checkEventAnswers.section.title.vehicleInformation",
        Seq(
          helper.transportIdentity(eventIndex),
          helper.transportNationality(eventIndex)
        ).flatten
      ))

    def differentContainerSection(sectionText: Text): Seq[Section] =
      Seq(
        Some(
          Section(sectionText, Seq(helper.isTranshipment(eventIndex), helper.transhipmentType(eventIndex)).flatten)
        ),
        userAnswers
          .get(DeriveNumberOfContainers(eventIndex))
          .map(List.range(0, _))
          .map(_.flatMap(helper.containerNumber(eventIndex, _)))
          .map(Section.apply(msg"checkEventAnswers.section.title.containerNumbers", _))
      ).flatten

    userAnswers
      .get(TranshipmentTypePage(eventIndex))
      .map {
        case DifferentVehicle   => differentVehicleSection
        case DifferentContainer => differentContainerSection(msg"checkEventAnswers.section.title.differentContainer")
        case DifferentContainerAndVehicle =>
          differentContainerSection(msg"checkEventAnswers.section.title.differentContainerAndVehicle") ++
            vehicleInformationSection
      }
      .getOrElse(Seq.empty)
  }

  implicit def writes(implicit messages: Messages): OWrites[CheckEventAnswersViewModel] = Json.writes[CheckEventAnswersViewModel]
}
