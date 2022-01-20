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

import controllers.routes
import models.reference.CustomsOffice
import models.{Address, GoodsLocation, Mode, MovementReferenceNumber, UserAnswers}
import pages._
import play.api.mvc.Call
import uk.gov.hmrc.viewmodels.SummaryList._
import uk.gov.hmrc.viewmodels._

class CheckYourAnswersHelper(userAnswers: UserAnswers, mode: Mode) extends SummaryListRowHelper(userAnswers) {

  def eoriNumber: Option[Row] = getAnswerAndBuildNamedRow[String](
    namePage = ConsigneeNamePage,
    answerPage = ConsigneeEoriNumberPage,
    formatAnswer = formatAsLiteral,
    prefix = "eoriNumber",
    id = Some("change-eori-number"),
    call = routes.ConsigneeEoriNumberController.onPageLoad(mrn, mode)
  )

  def consigneeName: Option[Row] = getAnswerAndBuildRow[String](
    page = ConsigneeNamePage,
    formatAnswer = formatAsLiteral,
    prefix = "consigneeName",
    id = Some("change-consignee-name"),
    call = routes.ConsigneeNameController.onPageLoad(mrn, mode)
  )

  def placeOfNotification: Option[Row] = getAnswerAndBuildRow[String](
    page = PlaceOfNotificationPage,
    formatAnswer = formatAsLiteral,
    prefix = "placeOfNotification",
    id = Some("change-place-of-notification"),
    call = routes.PlaceOfNotificationController.onPageLoad(mrn, mode)
  )

  def isTraderAddressPlaceOfNotification: Option[Row] = getAnswerAndBuildRow[Boolean](
    page = IsTraderAddressPlaceOfNotificationPage,
    formatAnswer = formatAsYesOrNo,
    prefix = "isTraderAddressPlaceOfNotification",
    id = Some("change-trader-address-place-of-notification"),
    call = routes.IsTraderAddressPlaceOfNotificationController.onPageLoad(mrn, mode)
  )

  def incidentOnRoute: Option[Row] = getAnswerAndBuildRow[Boolean](
    page = IncidentOnRoutePage,
    formatAnswer = formatAsYesOrNo,
    prefix = "incidentOnRoute",
    id = Some("change-incident-on-route"),
    call = routes.IncidentOnRouteController.onPageLoad(mrn, mode)
  )

  def traderName: Option[Row] = getAnswerAndBuildRow[String](
    page = TraderNamePage,
    formatAnswer = formatAsLiteral,
    prefix = "traderName",
    id = Some("change-trader-name"),
    call = routes.TraderNameController.onPageLoad(mrn, mode)
  )

  def traderEori: Option[Row] = getAnswerAndBuildRow[String](
    page = TraderEoriPage,
    formatAnswer = formatAsLiteral,
    prefix = "traderEori",
    id = Some("change-trader-eori"),
    call = routes.TraderEoriController.onPageLoad(mrn, mode)
  )

  def traderAddress: Option[Row] = getAnswerAndBuildRow[Address](
    page = TraderAddressPage,
    formatAnswer = formatAsAddress,
    prefix = "traderAddress",
    id = Some("change-trader-address"),
    call = routes.TraderAddressController.onPageLoad(mrn, mode)
  )

  def consigneeAddress: Option[Row] = getAnswerAndBuildNamedRow[Address](
    namePage = ConsigneeNamePage,
    answerPage = ConsigneeAddressPage,
    formatAnswer = formatAsAddress,
    prefix = "consigneeAddress",
    id = Some("change-consignee-address"),
    call = routes.ConsigneeAddressController.onPageLoad(mrn, mode)
  )

  def authorisedLocation: Option[Row] = getAnswerAndBuildRow[String](
    page = AuthorisedLocationPage,
    formatAnswer = formatAsLiteral,
    prefix = "authorisedLocation",
    id = Some("change-authorised-location"),
    call = routes.AuthorisedLocationController.onPageLoad(mrn, mode)
  )

  def customsSubPlace: Option[Row] = getAnswerAndBuildRow[String](
    page = CustomsSubPlacePage,
    formatAnswer = formatAsLiteral,
    prefix = "customsSubPlace",
    id = Some("change-customs-sub-place"),
    call = routes.CustomsSubPlaceController.onPageLoad(mrn, mode)
  )

  def movementReferenceNumber: Row = Row(
    key = Key(msg"movementReferenceNumber.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
    value = Value(lit"$mrn")
  )

  def pickCustomsOffice: Option[Row] =
    userAnswers.get(SimplifiedCustomsOfficePage) match {
      case Some(_) => simplifiedCustomsOffice
      case None    => customsOffice
    }

  def simplifiedCustomsOffice: Option[Row] =
    customsOffice(SimplifiedCustomsOfficePage, "customsOffice.simplified", routes.SimplifiedCustomsOfficeController.onPageLoad)

  def customsOffice: Option[Row] =
    customsOffice(CustomsOfficePage, "customsOffice", routes.CustomsOfficeController.onPageLoad)

  def goodsLocation: Option[Row] = getAnswerAndBuildRow[GoodsLocation](
    page = GoodsLocationPage,
    formatAnswer = goodsLocation => msg"goodsLocation.$goodsLocation",
    prefix = "goodsLocation",
    id = Some("change-goods-location"),
    call = routes.GoodsLocationController.onPageLoad(mrn, mode)
  )

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
            buildRow(
              prefix = messageKeyPrefix,
              answer = lit"${answer.toString}",
              id = Some("change-presentation-office"),
              call = call(mrn, mode),
              args = arg
            )
        }
    }
}
