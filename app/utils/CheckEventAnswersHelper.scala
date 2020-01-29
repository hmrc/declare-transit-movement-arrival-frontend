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

package utils

import controllers.events.seals.{routes => sealRoutes}
import controllers.events.transhipments.{routes => transhipmentRoutes}
import controllers.events.{routes => eventRoutes}
import models.{CheckMode, Index, MovementReferenceNumber, TraderAddress, UserAnswers}
import pages.events._
import pages.events.seals._
import pages.events.transhipments._
import uk.gov.hmrc.viewmodels.SummaryList._
import uk.gov.hmrc.viewmodels._

class CheckEventAnswersHelper(userAnswers: UserAnswers) {

  def isTranshipment(eventIndex: Index): Option[Row] = userAnswers.get(IsTranshipmentPage(eventIndex)) map {
    answer =>
      Row(
        key   = Key(msg"isTranshipment.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value = Value(yesOrNo(answer)),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = eventRoutes.IsTranshipmentController.onPageLoad(mrn, eventIndex, CheckMode).url,
            visuallyHiddenText = Some(msg"isTranshipment.change.hidden")
          )
        )
      )
  }

  def transhipmentType(eventIndex: Index): Option[Row] = userAnswers.get(TranshipmentTypePage(eventIndex)) map {
    answer =>
      Row(
        key   = Key(msg"transhipmentType.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value = Value(msg"transhipmentType.$answer"),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = transhipmentRoutes.TranshipmentTypeController.onPageLoad(mrn, eventIndex, CheckMode).url,
            visuallyHiddenText = Some(msg"transhipmentType.change.hidden")
          )
        )
      )
  }

  def containerNumber(eventIndex: Index, containerIndex: Index): Option[Row] = userAnswers.get(ContainerNumberPage(eventIndex, containerIndex)) map {
    answer =>
      Row(
        key   = Key(msg"addContainer.containerList.label".withArgs(containerIndex.display), classes = Seq("govuk-!-width-one-half")),
        value = Value(lit"${answer.containerNumber}"),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = transhipmentRoutes.ContainerNumberController.onPageLoad(mrn, eventIndex, containerIndex, CheckMode).url,
            visuallyHiddenText = Some(msg"containerNumber.change.hidden".withArgs(answer.containerNumber))
          )
        )
      )
  }

  def eventCountry(eventIndex: Index): Option[Row] = userAnswers.get(EventCountryPage(eventIndex)) map {
    answer =>
      Row(
        key   = Key(msg"eventCountry.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value = Value(lit"${answer.description}"),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = eventRoutes.EventCountryController.onPageLoad(mrn, eventIndex, CheckMode).url,
            visuallyHiddenText = Some(msg"eventCountry.change.hidden")
          )
        )
      )
  }

  def eventPlace(eventIndex: Index): Option[Row] = userAnswers.get(EventPlacePage(eventIndex)) map {
    answer =>
      Row(
        key   = Key(msg"eventPlace.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value = Value(lit"$answer"),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = eventRoutes.EventPlaceController.onPageLoad(mrn, eventIndex, CheckMode).url,
            visuallyHiddenText = Some(msg"eventPlace.change.hidden")
          )
        )
      )
  }

  def eventReported(eventIndex: Index): Option[Row] = userAnswers.get(EventReportedPage(eventIndex)) map {
    answer =>
      Row(
        key   = Key(msg"eventReported.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value = Value(yesOrNo(answer)),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = eventRoutes.EventReportedController.onPageLoad(mrn, eventIndex, CheckMode).url,
            visuallyHiddenText = Some(msg"eventReported.change.hidden")
          )
        )
      )
  }

  def incidentInformation(eventIndex: Index): Option[Row] = userAnswers.get(IncidentInformationPage(eventIndex)) map {
    answer =>
      Row(
        key   = Key(msg"incidentInformation.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value = Value(lit"$answer"),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = eventRoutes.IncidentInformationController.onPageLoad(mrn, eventIndex, CheckMode).url,
            visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"incidentInformation.checkYourAnswersLabel"))
          )
        )
      )
  }

  def transportIdentity(eventIndex: Index): Option[Row] = userAnswers.get(TransportIdentityPage(eventIndex)) map {
    answer =>
      Row(
        key   = Key(msg"transportIdentity.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value = Value(lit"$answer"),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = transhipmentRoutes.TransportIdentityController.onPageLoad(mrn, eventIndex, CheckMode).url,
            visuallyHiddenText = Some(msg"transportIdentity.change.hidden")
          )
        )
      )
  }

  def transportNationality(eventIndex: Index): Option[Row] = userAnswers.get(TransportNationalityPage(eventIndex)) map {
    answer =>
      Row(
        key   = Key(msg"transportNationality.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value = Value(lit"${answer.description}"),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = transhipmentRoutes.TransportNationalityController.onPageLoad(mrn, eventIndex, CheckMode).url,
            visuallyHiddenText = Some(msg"transportNationality.change.hidden")
          )
        )
      )
  }

  def haveSealsChanged(eventIndex: Index): Option[Row] = userAnswers.get(HaveSealsChangedPage(eventIndex)) map {
    answer =>
      Row(
        key   = Key(msg"haveSealsChanged.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value = Value(yesOrNo(answer)),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = sealRoutes.HaveSealsChangedController.onPageLoad(mrn, eventIndex, CheckMode).url,
            visuallyHiddenText = Some(msg"haveSealsChanged.change.hidden")
          )
        )
      )
  }

  def sealIdentity(eventIndex: Index, sealIndex: Index): Option[Row] = userAnswers.get(SealIdentityPage(eventIndex, sealIndex)) map {
    answer =>
      Row(
        key   = Key(msg"addSeal.sealList.label".withArgs(sealIndex.display), classes = Seq("govuk-!-width-one-half")),
        value = Value(lit"$answer"),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = sealRoutes.SealIdentityController.onPageLoad(mrn, eventIndex, sealIndex, CheckMode).url,
            visuallyHiddenText = Some(msg"sealIdentity.change.hidden".withArgs(answer))
          )
        )
      )
  }

  def movementReferenceNumber: Row = Row(
    key   = Key(msg"movementReferenceNumber.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
    value = Value(lit"${mrn.toString}")
  )

  def yesOrNo(answer: Boolean): Content =
    if (answer) {
      msg"site.yes"
    } else {
      msg"site.no"
    }

  def mrn: MovementReferenceNumber = userAnswers.id

  def addressHtml(address: TraderAddress): Html = Html(
    Seq(address.buildingAndStreet, address.city, address.postcode)
      .mkString("<br>")
  )
}
