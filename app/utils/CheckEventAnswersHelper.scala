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
import models.{Address, CheckMode, CountryList, DraftArrivalRef, Index, MovementReferenceNumber, UserAnswers}
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
            href               = eventRoutes.IsTranshipmentController.onPageLoad(ref, eventIndex, CheckMode).url,
            visuallyHiddenText = Some(msg"isTranshipment.change.hidden"),
            attributes         = Map("id" -> s"""change-is-transhipment-${eventIndex.display}""")
          )
        )
      )
  }

  def transhipmentType(eventIndex: Index): Option[Row] = userAnswers.get(TranshipmentTypePage(eventIndex)) map {
    answer =>
      Row(
        key   = Key(msg"transhipmentType.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value = Value(msg"transhipmentType.checkYourAnswers.$answer"),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = transhipmentRoutes.TranshipmentTypeController.onPageLoad(ref, eventIndex, CheckMode).url,
            visuallyHiddenText = Some(msg"transhipmentType.change.hidden"),
            attributes         = Map("id" -> s"""transhipment-type-${eventIndex.display}""")
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
            href               = transhipmentRoutes.ContainerNumberController.onPageLoad(ref, eventIndex, containerIndex, CheckMode).url,
            visuallyHiddenText = Some(msg"containerNumber.change.hidden".withArgs(answer.containerNumber)),
            attributes         = Map("id" -> s"""change-container-${containerIndex.display}""")
          )
        )
      )
  }

  def eventCountry(eventIndex: Index)(codeList: CountryList): Option[Row] =
    userAnswers
      .get(EventCountryPage(eventIndex))
      .map({
        answer =>
          val countryName = codeList.getCountry(answer).map(_.description).getOrElse(answer.code)

          Row(
            key   = Key(msg"eventCountry.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
            value = Value(lit"$countryName"),
            actions = List(
              Action(
                content            = msg"site.edit",
                href               = eventRoutes.EventCountryController.onPageLoad(ref, eventIndex, CheckMode).url,
                visuallyHiddenText = Some(msg"eventCountry.change.hidden"),
                attributes         = Map("id" -> s"""change-event-country-${eventIndex.display}""")
              )
            )
          )
      })

  def eventPlace(eventIndex: Index): Option[Row] = userAnswers.get(EventPlacePage(eventIndex)) map {
    answer =>
      Row(
        key   = Key(msg"eventPlace.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value = Value(lit"$answer"),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = eventRoutes.EventPlaceController.onPageLoad(ref, eventIndex, CheckMode).url,
            visuallyHiddenText = Some(msg"eventPlace.change.hidden"),
            attributes         = Map("id" -> s"""change-event-place-${eventIndex.display}""")
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
            href               = eventRoutes.EventReportedController.onPageLoad(ref, eventIndex, CheckMode).url,
            visuallyHiddenText = Some(msg"eventReported.change.hidden"),
            attributes         = Map("id" -> s"""change-event-reported-${eventIndex.display}""")
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
            href               = eventRoutes.IncidentInformationController.onPageLoad(ref, eventIndex, CheckMode).url,
            visuallyHiddenText = Some(msg"incidentInformation.change.hidden"),
            attributes         = Map("id" -> s"""change-incident-information-${eventIndex.display}""")
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
            href               = transhipmentRoutes.TransportIdentityController.onPageLoad(ref, eventIndex, CheckMode).url,
            visuallyHiddenText = Some(msg"transportIdentity.change.hidden"),
            attributes         = Map("id" -> s"""transport-identity-${eventIndex.display}""")
          )
        )
      )
  }

  def transportNationality(eventIndex: Index)(codeList: CountryList): Option[Row] = userAnswers.get(TransportNationalityPage(eventIndex)) map {
    answer =>
      val countryName = codeList.getCountry(answer).map(_.description).getOrElse(answer.code)

      Row(
        key   = Key(msg"transportNationality.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value = Value(lit"$countryName"),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = transhipmentRoutes.TransportNationalityController.onPageLoad(ref, eventIndex, CheckMode).url,
            visuallyHiddenText = Some(msg"transportNationality.change.hidden"),
            attributes         = Map("id" -> s"""transport-nationality-${eventIndex.display}""")
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
            href               = sealRoutes.HaveSealsChangedController.onPageLoad(ref, eventIndex, CheckMode).url,
            visuallyHiddenText = Some(msg"haveSealsChanged.change.hidden"),
            attributes         = Map("id" -> s"""seals-changed-${eventIndex.display}""")
          )
        )
      )
  }

  def sealIdentity(eventIndex: Index, sealIndex: Index): Option[Row] = userAnswers.get(SealIdentityPage(eventIndex, sealIndex)) map {
    answer =>
      Row(
        key   = Key(msg"addSeal.sealList.label".withArgs(sealIndex.display), classes = Seq("govuk-!-width-one-half")),
        value = Value(lit"${answer.numberOrMark}"),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = sealRoutes.SealIdentityController.onPageLoad(ref, eventIndex, sealIndex, CheckMode).url,
            visuallyHiddenText = Some(msg"sealIdentity.change.hidden".withArgs(answer.numberOrMark)),
            attributes         = Map("id" -> s"""change-seal-${sealIndex.display}""")
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
  def ref: DraftArrivalRef         = userAnswers.ref

  def addressHtml(address: Address): Html = Html(
    Seq(address.buildingAndStreet, address.city, address.postcode)
      .mkString("<br>")
  )
}
