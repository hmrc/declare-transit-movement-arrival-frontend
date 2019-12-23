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

package viewModels

import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json, OWrites}
import uk.gov.hmrc.viewmodels.SummaryList

case class Section(sectionTitle: Option[String], rows: Seq[SummaryList.Row])

object Section {
  def apply(sectionTitle: String, rows: Seq[SummaryList.Row]): Section = new Section(Some(sectionTitle), rows)

  def apply(rows: Seq[SummaryList.Row]): Section = new Section(None, rows)

  implicit def sectionWrites(implicit messages: Messages): OWrites[Section] = new OWrites[Section] {
    override def writes(o: Section): JsObject =
      Json.obj(
        "sectionTitle" -> o.sectionTitle,
        "rows"         -> Json.toJson(o.rows)
      )
  }
}
