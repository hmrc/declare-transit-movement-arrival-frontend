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
import models.{CountryList, Index, UserAnswers}
import pages.events.transhipments.TranshipmentTypePage
import uk.gov.hmrc.viewmodels.{NunjucksSupport, Text}
import utils.CheckEventAnswersHelper

object EventTypeSection extends NunjucksSupport {

  val differentContainerTitleKey: Text =
    msg"checkEventAnswers.section.title.differentContainer"

  val differentContainerAndVehicleTitleKey: Text =
    msg"checkEventAnswers.section.title.differentContainerAndVehicle"

  def apply(userAnswers: UserAnswers, eventIndex: Index, isTranshipment: Boolean, codeList: CountryList): Seq[Section] =
    userAnswers
      .get(TranshipmentTypePage(eventIndex))
      .map {
        case DifferentVehicle =>
          DifferentVehicleSection(userAnswers, eventIndex, isTranshipment, codeList)
        case DifferentContainer =>
          DifferentContainerSection(userAnswers, eventIndex, isTranshipment, differentContainerTitleKey)
        case DifferentContainerAndVehicle =>
          DifferentContainerSection(userAnswers, eventIndex, isTranshipment, differentContainerAndVehicleTitleKey) ++
            VehicleInformationSection(userAnswers, eventIndex, codeList)
      }
      .getOrElse(Seq.empty)
}

object VehicleInformationSection extends NunjucksSupport {

  def apply(userAnswers: UserAnswers, eventIndex: Index, codeList: CountryList): Seq[Section] = {

    val helper = new CheckEventAnswersHelper(userAnswers)

    Seq(
      Section(
        msg"checkEventAnswers.section.title.vehicleInformation",
        Seq(
          helper.transportIdentity(eventIndex),
          helper.transportNationality(eventIndex)(codeList)
        ).flatten
      ))
  }
}

object DifferentContainerSection extends NunjucksSupport {

  def apply(userAnswers: UserAnswers, eventIndex: Index, isTranshipment: Boolean, sectionText: Text): Seq[Section] = {

    val helper: CheckEventAnswersHelper = new CheckEventAnswersHelper(userAnswers)

    Seq(
      Some(
        Section(sectionText, Seq(helper.isTranshipment(eventIndex), helper.transhipmentType(eventIndex)).flatten)
      ),
      userAnswers
        .get(DeriveNumberOfContainers(eventIndex))
        .map {
          containerCount =>
            val listOfContainerIndexes = List.range(0, containerCount).map(Index(_))
            val rows = listOfContainerIndexes.flatMap {
              index =>
                helper.containerNumber(eventIndex, index)
            }
            Section(msg"checkEventAnswers.section.title.containerNumbers", rows)
        }
    ).flatten
  }
}

object DifferentVehicleSection extends NunjucksSupport {

  def apply(userAnswers: UserAnswers, eventIndex: Index, isTranshipment: Boolean, codeList: CountryList): Seq[Section] = {

    val helper: CheckEventAnswersHelper = new CheckEventAnswersHelper(userAnswers)

    Seq(
      Section(
        msg"checkEventAnswers.section.title.differentVehicle",
        Seq(
          if (isTranshipment) { helper.isTranshipment(eventIndex) } else None,
          helper.transhipmentType(eventIndex),
          helper.transportIdentity(eventIndex),
          helper.transportNationality(eventIndex)(codeList)
        ).flatten
      ))
  }
}
