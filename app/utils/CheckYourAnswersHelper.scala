/*
 * Copyright 2021 HM Revenue & Customs
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

import controllers.routes
import models.reference.CustomsOffice
import models.{CheckMode, Mode, MovementReferenceNumber, UserAnswers}
import pages._
import play.api.mvc.Call
import uk.gov.hmrc.viewmodels.SummaryList._
import uk.gov.hmrc.viewmodels.Text.Literal
import uk.gov.hmrc.viewmodels._
import utils.Format.dateFormatter

class CheckYourAnswersHelper(userAnswers: UserAnswers) extends CheckEventAnswersHelper(userAnswers) {

  def eoriNumber: Option[Row] =
    userAnswers.get(ConsigneeNamePage) match {
      case Some(consigneeName) =>
        userAnswers.get(ConsigneeEoriNumberPage) map {
          answer =>
            val messages = msg"eoriNumber.checkYourAnswersLabel".withArgs(consigneeName)
            Row(
              key   = Key(messages, classes = Seq("govuk-!-width-one-half")),
              value = Value(lit"$answer"),
              actions = List(
                Action(
                  content            = msg"site.edit",
                  href               = routes.ConsigneeEoriNumberController.onPageLoad(mrn, CheckMode).url,
                  visuallyHiddenText = Some(messages),
                  attributes         = Map("id" -> s"""change-eori-number""")
                )
              )
            )
        }
      case _ => None
    }

  def consigneeName: Option[Row] = userAnswers.get(ConsigneeNamePage) map {
    answer =>
      Row(
        key   = Key(msg"consigneeName.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value = Value(lit"$answer"),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = routes.ConsigneeNameController.onPageLoad(mrn, CheckMode).url,
            visuallyHiddenText = Some(msg"consigneeName.change.hidden"),
            attributes         = Map("id" -> s"""change-consignee-name""")
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
            visuallyHiddenText = Some(msg"placeOfNotification.change.hidden"),
            attributes         = Map("id" -> s"""change-place-of-notification""")
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
            visuallyHiddenText = Some(message),
            attributes         = Map("id" -> s"""change-trader-address-place-of-notification""")
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
            visuallyHiddenText = Some(msg"incidentOnRoute.change.hidden"),
            attributes         = Map("id" -> s"""change-incident-on-route""")
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
            visuallyHiddenText = Some(msg"traderName.change.hidden"),
            attributes         = Map("id" -> s"""change-trader-name""")
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
            visuallyHiddenText = Some(msg"traderEori.change.hidden"),
            attributes         = Map("id" -> s"""change-trader-eori""")
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
            visuallyHiddenText = Some(msg"traderAddress.change.hidden"),
            attributes         = Map("id" -> s"""change-trader-address""")
          )
        )
      )
  }

  def consigneeAddress: Option[Row] = userAnswers.get(ConsigneeAddressPage) map {
    val consigneeName = userAnswers.get(ConsigneeNamePage).getOrElse("")
    answer =>
      Row(
        key   = Key(msg"consigneeAddress.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value = Value(addressHtml(answer)),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = routes.ConsigneeAddressController.onPageLoad(mrn, CheckMode).url,
            visuallyHiddenText = Some(msg"consigneeAddress.visually.hidden".withArgs(consigneeName)),
            attributes         = Map("id" -> s"""change-consignee-address""")
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
            visuallyHiddenText = Some(msg"authorisedLocation.visually.hidden"),
            attributes         = Map("id" -> s"""change-authorised-location""")
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
            visuallyHiddenText = Some(msg"customsSubPlace.change.hidden"),
            attributes         = Map("id" -> s"""change-customs-sub-place""")
          )
        )
      )
  }

  def pickCustomsOffice: Option[Row] =
    userAnswers.get(SimplifiedCustomsOfficePage) match {
      case Some(_) => simplifiedCustomsOffice
      case None    => customsOffice
    }

  def simplifiedCustomsOffice: Option[Row] =
    customsOffice(SimplifiedCustomsOfficePage, "customsOffice.simplified", routes.SimplifiedCustomsOfficeController.onPageLoad)

  def customsOffice: Option[Row] =
    customsOffice(CustomsOfficePage, "customsOffice", routes.CustomsOfficeController.onPageLoad)

  def goodsLocation: Option[Row] = userAnswers.get(GoodsLocationPage) map {
    answer =>
      Row(
        key   = Key(msg"goodsLocation.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value = Value(msg"goodsLocation.$answer"),
        actions = List(
          Action(
            content            = msg"site.edit",
            href               = routes.GoodsLocationController.onPageLoad(mrn, CheckMode).url,
            visuallyHiddenText = Some(msg"goodsLocation.change.hidden"),
            attributes         = Map("id" -> s"""change-goods-location""")
          )
        )
      )
  }

  private def customsOffice(page: QuestionPage[CustomsOffice], messageKeyPrefix: String, call: (MovementReferenceNumber, Mode) => Call): Option[Row] =
    userAnswers.get(page) flatMap {
      answer =>
        val location: Option[String] = (userAnswers.get(CustomsSubPlacePage), userAnswers.get(ConsigneeNamePage)) match {
          case (Some(customsSubPlace), None) => Some(customsSubPlace)
          case (None, Some(consigneeName))   => Some(consigneeName)
          case _                             => None
        }

        location map {
          arg =>
            val customsOfficeValue = answer.name match {
              case Some(name) => Value(lit"$name (${answer.id})")
              case None       => Value(lit"${answer.id}")
            }

            Row(
              key = Key(
                content = msg"$messageKeyPrefix.checkYourAnswersLabel".withArgs(arg),
                classes = Seq("govuk-!-width-one-half")
              ),
              value = customsOfficeValue,
              actions = List(
                Action(
                  content            = msg"site.edit",
                  href               = call(mrn, CheckMode).url,
                  visuallyHiddenText = Some(msg"$messageKeyPrefix.change.hidden".withArgs(arg)),
                  attributes         = Map("id" -> s"""change-presentation-office""")
                )
              )
            )
        }
    }
}
