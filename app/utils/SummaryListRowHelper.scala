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

import models.reference.CountryCode
import models.{Address, CountryList, MovementReferenceNumber, UserAnswers}
import pages.QuestionPage
import play.api.libs.json.Reads
import play.api.mvc.Call
import uk.gov.hmrc.viewmodels.SummaryList.{Action, Key, Row, Value}
import uk.gov.hmrc.viewmodels.{Content, Html, MessageInterpolators, Text}

private[utils] class SummaryListRowHelper(userAnswers: UserAnswers) {

  def mrn: MovementReferenceNumber = userAnswers.movementReferenceNumber

  def formatAsYesOrNo(answer: Boolean): Content =
    if (answer) {
      msg"site.yes"
    } else {
      msg"site.no"
    }

  def formatAsAddress(address: Address): Html = Html(
    Seq(address.buildingAndStreet, address.city, address.postcode)
      .mkString("<br>")
  )

  def formatAsLiteral[T](answer: T): Content = lit"$answer"

  def formatAsCountry(countryList: CountryList)(answer: CountryCode): Content =
    lit"${countryList.getCountry(answer).map(_.description).getOrElse(answer.code)}"

  def getAnswerAndBuildRow[T](
    page: QuestionPage[T],
    formatAnswer: T => Content,
    prefix: String,
    id: Option[String],
    call: Call,
    args: Any*
  )(implicit rds: Reads[T]): Option[Row] =
    userAnswers.get(page) map {
      answer =>
        buildRow(
          prefix = prefix,
          answer = formatAnswer(answer),
          id = id,
          call = call,
          args = args: _*
        )
    }

  def getAnswerAndBuildNamedRow[T](
    namePage: QuestionPage[String],
    answerPage: QuestionPage[T],
    formatAnswer: T => Content,
    prefix: String,
    id: Option[String],
    call: Call
  )(implicit rds: Reads[T]): Option[Row] =
    userAnswers.get(namePage) flatMap {
      name =>
        getAnswerAndBuildRow[T](
          page = answerPage,
          formatAnswer = formatAnswer,
          prefix = prefix,
          id = id,
          call = call,
          args = name
        )
    }

  def getAnswerAndBuildSectionRow[T](
    page: QuestionPage[T],
    formatAnswer: T => String,
    prefix: String,
    label: Content,
    id: Option[String],
    call: Call
  )(implicit rds: Reads[T]): Option[Row] =
    userAnswers.get(page) map {
      answer =>
        buildSimpleRow(
          prefix = prefix,
          label = label,
          answer = lit"${formatAnswer(answer)}",
          id = id,
          call = call,
          args = formatAnswer(answer)
        )
    }

  def getAnswerAndBuildRemovableRow[T](
    page: QuestionPage[T],
    formatAnswer: T => Text,
    id: String,
    changeCall: Call,
    removeCall: Call
  )(implicit rds: Reads[T]): Option[Row] =
    userAnswers.get(page) map {
      answer =>
        buildRemovableRow(
          label = formatAnswer(answer),
          id = id,
          changeCall = changeCall,
          removeCall = removeCall
        )
    }

  def buildRow(
    prefix: String,
    answer: Content,
    id: Option[String],
    call: Call,
    args: Any*
  ): Row =
    buildSimpleRow(
      prefix = prefix,
      label = msg"$prefix.checkYourAnswersLabel".withArgs(args: _*),
      answer = answer,
      id = id,
      call = call,
      args = args: _*
    )

  def buildSimpleRow(
    prefix: String,
    label: Content,
    answer: Content,
    id: Option[String],
    call: Call,
    args: Any*
  ): Row =
    Row(
      key = Key(label, classes = Seq("govuk-!-width-one-half")),
      value = Value(answer),
      actions = List(
        Action(
          content = msg"site.edit",
          href = call.url,
          visuallyHiddenText = Some(msg"$prefix.change.hidden".withArgs(args: _*)),
          attributes = id.fold[Map[String, String]](Map.empty)(
            id => Map("id" -> id)
          )
        )
      )
    )

  def buildRemovableRow(
    label: Text,
    value: String = "",
    id: String,
    changeCall: Call,
    removeCall: Call
  ): Row =
    Row(
      key = Key(label),
      value = Value(lit"$value"),
      actions = List(
        Action(
          content = msg"site.edit",
          href = changeCall.url,
          visuallyHiddenText = Some(label),
          attributes = Map("id" -> s"change-$id")
        ),
        Action(
          content = msg"site.delete",
          href = removeCall.url,
          visuallyHiddenText = Some(label),
          attributes = Map("id" -> s"remove-$id")
        )
      )
    )

}
