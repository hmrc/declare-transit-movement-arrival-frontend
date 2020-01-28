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

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages._
import uk.gov.hmrc.viewmodels.SummaryList._
import uk.gov.hmrc.viewmodels._

class CheckYourAnswersHelper(userAnswers: UserAnswers) extends CheckEventAnswersHelper(userAnswers) {

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
}

object CheckYourAnswersHelper {

  private val dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
}
