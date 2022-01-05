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

package utils

import controllers.events.seals.{routes => sealRoutes}
import controllers.events.transhipments.{routes => transhipmentRoutes}
import controllers.events.{routes => eventRoutes}
import models.domain.{ContainerDomain, SealDomain}
import models.reference.CountryCode
import models.{CountryList, Index, Mode, TranshipmentType, UserAnswers}
import pages.events._
import pages.events.seals._
import pages.events.transhipments._
import uk.gov.hmrc.viewmodels.SummaryList._
import uk.gov.hmrc.viewmodels._

class CheckEventAnswersHelper(userAnswers: UserAnswers, mode: Mode) extends SummaryListRowHelper(userAnswers) {

  def isTranshipment(eventIndex: Index): Option[Row] = getAnswerAndBuildRow[Boolean](
    page = IsTranshipmentPage(eventIndex),
    formatAnswer = formatAsYesOrNo,
    prefix = "isTranshipment",
    id = Some(s"change-is-transhipment-${eventIndex.display}"),
    call = eventRoutes.IsTranshipmentController.onPageLoad(mrn, eventIndex, mode)
  )

  def transhipmentType(eventIndex: Index): Option[Row] = getAnswerAndBuildRow[TranshipmentType](
    page = TranshipmentTypePage(eventIndex),
    formatAnswer = transhipmentType => msg"transhipmentType.checkYourAnswers.$transhipmentType",
    prefix = "transhipmentType",
    id = Some(s"transhipment-type-${eventIndex.display}"),
    call = transhipmentRoutes.TranshipmentTypeController.onPageLoad(mrn, eventIndex, mode)
  )

  def containerNumber(eventIndex: Index, containerIndex: Index): Option[Row] = getAnswerAndBuildSectionRow[ContainerDomain](
    page = ContainerNumberPage(eventIndex, containerIndex),
    formatAnswer = _.containerNumber,
    prefix = "containerNumber",
    label = msg"addContainer.containerList.label".withArgs(containerIndex.display),
    id = Some(s"change-container-${containerIndex.display}"),
    call = transhipmentRoutes.ContainerNumberController.onPageLoad(mrn, eventIndex, containerIndex, mode)
  )

  def eventCountry(eventIndex: Index)(codeList: CountryList): Option[Row] = getAnswerAndBuildRow[CountryCode](
    page = EventCountryPage(eventIndex),
    formatAnswer = formatAsCountry(codeList),
    prefix = "eventCountry",
    id = Some(s"change-event-country-${eventIndex.display}"),
    call = eventRoutes.EventCountryController.onPageLoad(mrn, eventIndex, mode)
  )

  def eventPlace(eventIndex: Index): Option[Row] = getAnswerAndBuildRow[String](
    page = EventPlacePage(eventIndex),
    formatAnswer = formatAsLiteral,
    prefix = "eventPlace",
    id = Some(s"change-event-place-${eventIndex.display}"),
    call = eventRoutes.EventPlaceController.onPageLoad(mrn, eventIndex, mode)
  )

  def eventReported(eventIndex: Index): Option[Row] = getAnswerAndBuildRow[Boolean](
    page = EventReportedPage(eventIndex),
    formatAnswer = formatAsYesOrNo,
    prefix = "eventReported",
    id = Some(s"change-event-reported-${eventIndex.display}"),
    call = eventRoutes.EventReportedController.onPageLoad(mrn, eventIndex, mode)
  )

  def incidentInformation(eventIndex: Index): Option[Row] = getAnswerAndBuildRow[String](
    page = IncidentInformationPage(eventIndex),
    formatAnswer = formatAsLiteral,
    prefix = "incidentInformation",
    id = Some(s"change-incident-information-${eventIndex.display}"),
    call = eventRoutes.IncidentInformationController.onPageLoad(mrn, eventIndex, mode)
  )

  def transportIdentity(eventIndex: Index): Option[Row] = getAnswerAndBuildRow[String](
    page = TransportIdentityPage(eventIndex),
    formatAnswer = formatAsLiteral,
    prefix = "transportIdentity",
    id = Some(s"transport-identity-${eventIndex.display}"),
    call = transhipmentRoutes.TransportIdentityController.onPageLoad(mrn, eventIndex, mode)
  )

  def transportNationality(eventIndex: Index)(codeList: CountryList): Option[Row] = getAnswerAndBuildRow[CountryCode](
    page = TransportNationalityPage(eventIndex),
    formatAnswer = formatAsCountry(codeList),
    prefix = "transportNationality",
    id = Some(s"transport-nationality-${eventIndex.display}"),
    call = transhipmentRoutes.TransportNationalityController.onPageLoad(mrn, eventIndex, mode)
  )

  def haveSealsChanged(eventIndex: Index): Option[Row] = getAnswerAndBuildRow[Boolean](
    page = HaveSealsChangedPage(eventIndex),
    formatAnswer = formatAsYesOrNo,
    prefix = "haveSealsChanged",
    id = Some(s"seals-changed-${eventIndex.display}"),
    call = sealRoutes.HaveSealsChangedController.onPageLoad(mrn, eventIndex, mode)
  )

  def sealIdentity(eventIndex: Index, sealIndex: Index): Option[Row] = getAnswerAndBuildSectionRow[SealDomain](
    page = SealIdentityPage(eventIndex, sealIndex),
    formatAnswer = _.numberOrMark,
    prefix = "sealIdentity",
    label = msg"addSeal.sealList.label".withArgs(sealIndex.display),
    id = Some(s"change-seal-${sealIndex.display}"),
    call = sealRoutes.SealIdentityController.onPageLoad(mrn, eventIndex, sealIndex, mode)
  )

}
