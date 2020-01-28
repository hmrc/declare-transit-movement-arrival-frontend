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

package viewModels.sections

import derivable.DeriveNumberOfContainers
import models.TranshipmentType.{DifferentContainer, DifferentContainerAndVehicle, DifferentVehicle}
import models.UserAnswers
import pages.events.transhipments.TranshipmentTypePage
import uk.gov.hmrc.viewmodels.{NunjucksSupport, Text}
import utils.CheckYourAnswersHelper

object EventTypeSection extends NunjucksSupport {

  val differentContainerTitleKey: Text =
    msg"checkEventAnswers.section.title.differentContainer"

  val differentContainerAndVehicleTitleKey: Text =
    msg"checkEventAnswers.section.title.differentContainerAndVehicle"

  def apply(userAnswers: UserAnswers, eventIndex: Int, isTranshipment: Boolean): Seq[Section] =
    userAnswers
      .get(TranshipmentTypePage(eventIndex))
      .map {

        case DifferentVehicle =>
          DifferentVehicleSection(userAnswers, eventIndex, isTranshipment)
        case DifferentContainer =>
          DifferentContainerSection(userAnswers, eventIndex, isTranshipment, differentContainerTitleKey)
        case DifferentContainerAndVehicle =>
          DifferentContainerSection(userAnswers, eventIndex, isTranshipment, differentContainerAndVehicleTitleKey) ++
            VehicleInformationSection(userAnswers, eventIndex)

      }
      .getOrElse(Seq.empty)
}

object VehicleInformationSection extends NunjucksSupport {

  def apply(userAnswers: UserAnswers, eventIndex: Int): Seq[Section] = {

    val helper: CheckYourAnswersHelper = new CheckYourAnswersHelper(userAnswers)

    Seq(
      Section(
        msg"checkEventAnswers.section.title.vehicleInformation",
        Seq(
          helper.transportIdentity(eventIndex),
          helper.transportNationality(eventIndex)
        ).flatten
      ))
  }
}

object DifferentContainerSection extends NunjucksSupport {

  def apply(userAnswers: UserAnswers, eventIndex: Int, isTranshipment: Boolean, sectionText: Text): Seq[Section] = {

    val helper: CheckYourAnswersHelper = new CheckYourAnswersHelper(userAnswers)

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
  }
}

object DifferentVehicleSection extends NunjucksSupport {

  def apply(userAnswers: UserAnswers, eventIndex: Int, isTranshipment: Boolean): Seq[Section] = {

    val helper: CheckYourAnswersHelper = new CheckYourAnswersHelper(userAnswers)

    Seq(
      Section(
        msg"checkEventAnswers.section.title.differentVehicle",
        Seq(
          if (isTranshipment) { helper.isTranshipment(eventIndex) } else None,
          helper.transhipmentType(eventIndex),
          helper.transportIdentity(eventIndex),
          helper.transportNationality(eventIndex)
        ).flatten
      ))
  }
}
