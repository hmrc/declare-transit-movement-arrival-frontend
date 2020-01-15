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

import java.time.format.DateTimeFormatter

import controllers.events.seals.{routes => sealRoutes}
import controllers.events.transhipments.{routes => transhipmentRoutes}
import controllers.events.{routes => eventRoutes}
import controllers.routes
import models.{CheckMode, MovementReferenceNumber, TraderAddress, UserAnswers}
import pages._
import pages.events._
import pages.events.seals.{AddSealPage, HaveSealsChangedPage, RemoveSealPage, SealIdentityPage}
import pages.events.transhipments._
import uk.gov.hmrc.viewmodels.SummaryList._
import uk.gov.hmrc.viewmodels._

class CheckYourAnswersHelper(userAnswers: UserAnswers) {

  def removeSeal: Option[Row] = userAnswers.get(RemoveSealPage) map {
    answer =>
      Row(
        key   = Key(msg"removeSeal.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value = Value(yesOrNo(answer)),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = sealRoutes.RemoveSealController.onPageLoad(mrn, CheckMode).url,
            visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"removeSeal.checkYourAnswersLabel"))
          )
        )
      )
  }

  def haveSealsChanged(eventIndex: Int): Option[Row] = userAnswers.get(HaveSealsChangedPage(eventIndex)) map {
    answer =>
      Row(
        key   = Key(msg"haveSealsChanged.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value = Value(yesOrNo(answer)),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = sealRoutes.HaveSealsChangedController.onPageLoad(mrn, eventIndex, CheckMode).url,
            visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"haveSealsChanged.checkYourAnswersLabel"))
          )
        )
      )
  }

  def addSeal(eventIndex: Int): Option[Row] = userAnswers.get(AddSealPage(eventIndex)) map {
    answer =>
      Row(
        key   = Key(msg"addSeal.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value = Value(yesOrNo(answer)),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = sealRoutes.AddSealController.onPageLoad(mrn, eventIndex, CheckMode).url,
            visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"addSeal.checkYourAnswersLabel"))
          )
        )
      )
  }

  def sealIdentity(eventIndex: Int): Option[Row] = userAnswers.get(SealIdentityPage(eventIndex)) map {
    answer =>
      Row(
        key   = Key(msg"sealIdentity.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value = Value(lit"$answer"),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = sealRoutes.SealIdentityController.onPageLoad(mrn, eventIndex, CheckMode).url,
            visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"sealIdentity.checkYourAnswersLabel"))
          )
        )
      )
  }

  def addContainer(eventIndex: Int): Option[Row] = userAnswers.get(AddContainerPage(eventIndex)) map {
    answer =>
      Row(
        key   = Key(msg"addContainer.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value = Value(yesOrNo(answer)),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = transhipmentRoutes.AddContainerController.onPageLoad(mrn, eventIndex, CheckMode).url,
            visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"addContainer.checkYourAnswersLabel"))
          )
        )
      )
  }

  def containerNumber(eventIndex: Int, containerIndex: Int): Option[Row] = userAnswers.get(ContainerNumberPage(eventIndex, containerIndex)) map {
    answer =>
      Row(
        key   = Key(msg"containerNumber.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value = Value(lit"$answer"),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = transhipmentRoutes.ContainerNumberController.onPageLoad(mrn, eventIndex, containerIndex, CheckMode).url,
            visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"containerNumber.checkYourAnswersLabel"))
          )
        )
      )
  }

  def transportNationality(eventIndex: Int): Option[Row] = userAnswers.get(TransportNationalityPage(eventIndex)) map {
    answer =>
      Row(
        key   = Key(msg"transportNationality.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value = Value(lit"${answer.description}"),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = transhipmentRoutes.TransportNationalityController.onPageLoad(mrn, eventIndex, CheckMode).url,
            visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"transportNationality.checkYourAnswersLabel"))
          )
        )
      )
  }

  def transportIdentity(eventIndex: Int): Option[Row] = userAnswers.get(TransportIdentityPage(eventIndex)) map {
    answer =>
      Row(
        key   = Key(msg"transportIdentity.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value = Value(lit"$answer"),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = transhipmentRoutes.TransportIdentityController.onPageLoad(mrn, eventIndex, CheckMode).url,
            visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"transportIdentity.checkYourAnswersLabel"))
          )
        )
      )
  }

  def transhipmentType(eventIndex: Int): Option[Row] = userAnswers.get(TranshipmentTypePage(eventIndex)) map {
    answer =>
      Row(
        key   = Key(msg"transhipmentType.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value = Value(msg"transhipmentType.$answer"),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = transhipmentRoutes.TranshipmentTypeController.onPageLoad(mrn, eventIndex, CheckMode).url,
            visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"transhipmentType.checkYourAnswersLabel"))
          )
        )
      )
  }

  def placeOfNotification: Option[Row] = userAnswers.get(PlaceOfNotificationPage) map {
    answer =>
      Row(
        key   = Key(msg"placeOfNotification.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value = Value(lit"$answer"),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = routes.PlaceOfNotificationController.onPageLoad(mrn, CheckMode).url,
            visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"placeOfNotification.checkYourAnswersLabel"))
          )
        )
      )
  }

  def isTraderAddressPlaceOfNotification: Option[Row] = userAnswers.get(IsTraderAddressPlaceOfNotificationPage) map {
    answer =>
      val postcode = userAnswers.get(TraderAddressPage).map(_.postcode).get // TODO remove get at the end of the option
      val message  = msg"isTraderAddressPlaceOfNotification.checkYourAnswersLabel".withArgs(postcode)
      Row(
        key   = Key(message, classes = Seq("govuk-!-width-one-half")),
        value = Value(yesOrNo(answer)),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = routes.IsTraderAddressPlaceOfNotificationController.onPageLoad(mrn, CheckMode).url,
            visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(message))
          )
        )
      )
  }

  def isTranshipment(index: Int): Option[Row] = userAnswers.get(IsTranshipmentPage(index)) map {
    answer =>
      Row(
        key   = Key(msg"isTranshipment.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value = Value(yesOrNo(answer)),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = eventRoutes.IsTranshipmentController.onPageLoad(mrn, index, CheckMode).url,
            visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"isTranshipment.checkYourAnswersLabel"))
          )
        )
      )
  }

  def incidentInformation(index: Int): Option[Row] = userAnswers.get(IncidentInformationPage(index)) map {
    answer =>
      Row(
        key   = Key(msg"incidentInformation.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value = Value(lit"$answer"),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = eventRoutes.IncidentInformationController.onPageLoad(mrn, index, CheckMode).url,
            visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"incidentInformation.checkYourAnswersLabel"))
          )
        )
      )
  }

  def eventReported(index: Int): Option[Row] = userAnswers.get(EventReportedPage(index)) map {
    answer =>
      Row(
        key   = Key(msg"eventReported.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value = Value(yesOrNo(answer)),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = eventRoutes.EventReportedController.onPageLoad(mrn, index, CheckMode).url,
            visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"eventReported.checkYourAnswersLabel"))
          )
        )
      )
  }

  def eventPlace(index: Int): Option[Row] = userAnswers.get(EventPlacePage(index)) map {
    answer =>
      Row(
        key   = Key(msg"eventPlace.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value = Value(lit"$answer"),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = eventRoutes.EventPlaceController.onPageLoad(mrn, index, CheckMode).url,
            visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"eventPlace.checkYourAnswersLabel"))
          )
        )
      )
  }

  def eventCountry(index: Int): Option[Row] = userAnswers.get(EventCountryPage(index)) map {
    answer =>
      Row(
        key   = Key(msg"eventCountry.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value = Value(lit"${answer.description}"),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = eventRoutes.EventCountryController.onPageLoad(mrn, index, CheckMode).url,
            visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"eventCountry.checkYourAnswersLabel"))
          )
        )
      )
  }

  def incidentOnRoute: Option[Row] = userAnswers.get(IncidentOnRoutePage) map {
    answer =>
      Row(
        key   = Key(msg"incidentOnRoute.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value = Value(yesOrNo(answer)),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = routes.IncidentOnRouteController.onPageLoad(mrn, CheckMode).url,
            visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"incidentOnRoute.checkYourAnswersLabel"))
          )
        )
      )
  }

  def traderName: Option[Row] = userAnswers.get(TraderNamePage) map {
    answer =>
      Row(
        key   = Key(msg"traderName.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value = Value(lit"$answer"),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = routes.TraderNameController.onPageLoad(mrn, CheckMode).url,
            visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"traderName.checkYourAnswersLabel"))
          )
        )
      )
  }

  def traderEori: Option[Row] = userAnswers.get(TraderEoriPage) map {
    answer =>
      Row(
        key   = Key(msg"traderEori.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value = Value(lit"$answer"),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = routes.TraderEoriController.onPageLoad(mrn, CheckMode).url,
            visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"traderEori.checkYourAnswersLabel"))
          )
        )
      )
  }

  def traderAddress: Option[Row] = userAnswers.get(TraderAddressPage) map {
    answer =>
      Row(
        key   = Key(msg"traderAddress.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value = Value(addressHtml(answer)),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = routes.TraderAddressController.onPageLoad(mrn, CheckMode).url,
            visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"traderAddress.checkYourAnswersLabel"))
          )
        )
      )
  }

  def authorisedLocation: Option[Row] = userAnswers.get(AuthorisedLocationPage) map {
    answer =>
      Row(
        key   = Key(msg"authorisedLocation.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value = Value(lit"$answer"),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = routes.AuthorisedLocationController.onPageLoad(mrn, CheckMode).url,
            visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"authorisedLocation.checkYourAnswersLabel"))
          )
        )
      )
  }

  def customsSubPlace: Option[Row] = userAnswers.get(CustomsSubPlacePage) map {
    answer =>
      Row(
        key   = Key(msg"customsSubPlace.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value = Value(lit"$answer"),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = routes.CustomsSubPlaceController.onPageLoad(mrn, CheckMode).url,
            visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"customsSubPlace.checkYourAnswersLabel"))
          )
        )
      )
  }

  def presentationOffice: Option[Row] = userAnswers.get(PresentationOfficePage) map {
    answer =>
      val customsSubPlace: String = userAnswers.get(CustomsSubPlacePage).getOrElse("this location")
      val message                 = msg"presentationOffice.checkYourAnswersLabel".withArgs(customsSubPlace)

      Row(
        key   = Key(message, classes = Seq("govuk-!-width-one-half")),
        value = Value(lit"${answer.name} (${answer.id})"),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = routes.PresentationOfficeController.onPageLoad(mrn, CheckMode).url,
            visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(message))
          )
        )
      )
  }

  def goodsLocation: Option[Row] = userAnswers.get(GoodsLocationPage) map {
    answer =>
      Row(
        key   = Key(msg"goodsLocation.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value = Value(msg"goodsLocation.$answer"),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = routes.GoodsLocationController.onPageLoad(mrn, CheckMode).url,
            visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"goodsLocation.checkYourAnswersLabel"))
          )
        )
      )
  }

  def movementReferenceNumber: Row = Row(
    key   = Key(msg"movementReferenceNumber.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
    value = Value(lit"${mrn.toString}")
  )

  private def yesOrNo(answer: Boolean): Content =
    if (answer) {
      msg"site.yes"
    } else {
      msg"site.no"
    }

  private def mrn: MovementReferenceNumber = userAnswers.id

  private def addressHtml(address: TraderAddress): Html = Html(
    Seq(address.buildingAndStreet, address.city, address.postcode)
      .mkString(",<br>")
  )
}

object CheckYourAnswersHelper {

  private val dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
}
