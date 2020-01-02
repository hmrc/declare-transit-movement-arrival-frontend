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

  def apply(userAnswers: UserAnswers, index: Int): CheckEventAnswersViewModel = {
    val helper = new CheckYourAnswersHelper(userAnswers)

    val eventInfoIsTranshipmentRow: Option[Row] = userAnswers.get(IsTranshipmentPage(index)) flatMap {
      case false => helper.isTranshipment(index)
      case true  => None
    }

    val eventInfo: Seq[Row] =
      Seq(
        helper.eventCountry(index),
        helper.eventPlace(index),
        helper.eventReported(index),
        eventInfoIsTranshipmentRow,
        helper.incidentInformation(index)
      ).flatten

    val otherInfoIsTranshipmentRow: Option[Row] = userAnswers.get(IsTranshipmentPage(index)) flatMap {
      case false => None
      case true  => helper.isTranshipment(index)
    }

    val differentVehicleSection: Section =
      Section(
        msg"checkEventAnswers.section.title.differentVehicle",
        Seq(
          otherInfoIsTranshipmentRow,
          helper.transhipmentType(index),
          helper.transportIdentity(index),
          helper.transportNationality(index)
        ).flatten
      )

    val addContainerHelper = AddContainerHelper(userAnswers)
    val containers: Option[Section] = userAnswers
      .get(DeriveNumberOfContainers(index))
      .map(List.range(0, _))
      .map(_.flatMap(addContainerHelper.containerRow(index, _)))
      .map(Section.apply(msg"checkEventAnswers.section.title.containerNumbers", _))

    val otherInfoSections: Seq[Section] = (userAnswers.get(IsTranshipmentPage(index)), userAnswers.get(TranshipmentTypePage(index))) match {
      case (Some(true), Some(DifferentVehicle)) => Seq(differentVehicleSection)
      case (Some(true), Some(DifferentContainer)) =>
        Seq(
          Some(
            Section(
              msg"checkEventAnswers.section.title.differentContainer",
              Seq(
                otherInfoIsTranshipmentRow,
                helper.transhipmentType(index)
              ).flatten
            )
          ),
          containers
        ).flatten
      case (Some(true), Some(DifferentContainerAndVehicle)) =>
        Seq(
          Some(
            Section(
              msg"checkEventAnswers.section.title.differentContainerAndVehicle",
              Seq(
                otherInfoIsTranshipmentRow,
                helper.transhipmentType(index)
              ).flatten
            )
          ),
          containers,
          Some(
            Section(
              msg"checkEventAnswers.section.title.vehicleInformation",
              Seq(
                helper.transportIdentity(index),
                helper.transportNationality(index)
              ).flatten
            )
          )
        ).flatten

      case _ =>
        Seq.empty
    }

    CheckEventAnswersViewModel(
      Section(eventInfo),
      otherInfoSections
    )
  }

  implicit def writes(implicit messages: Messages): OWrites[CheckEventAnswersViewModel] = Json.writes[CheckEventAnswersViewModel]
}
