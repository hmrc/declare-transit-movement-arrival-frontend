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

package pages.events.transhipments

import models.{Index, TranshipmentType, UserAnswers}
import pages.QuestionPage
import pages.events.SectionConstants
import play.api.libs.json.JsPath
import queries.ContainersQuery

import scala.util.Try

final case class TranshipmentTypePage(eventIndex: Index) extends QuestionPage[TranshipmentType] {

  override def path: JsPath = JsPath \ SectionConstants.events \ eventIndex.position \ toString

  override def toString: String = "transhipmentType"

  override def cleanup(value: Option[TranshipmentType], userAnswers: UserAnswers): Try[UserAnswers] = value match {
    case _ =>
      userAnswers
        .remove(TransportIdentityPage(eventIndex))
        .flatMap(_.remove(TransportNationalityPage(eventIndex)))
        .flatMap(_.remove(ContainersQuery(eventIndex)))
  }
}
