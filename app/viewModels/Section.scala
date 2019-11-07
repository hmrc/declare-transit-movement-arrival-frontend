/*
 * Copyright 2019 HM Revenue & Customs
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
import play.api.libs.json.{JsValue, Json, Writes}
import uk.gov.hmrc.viewmodels.SummaryList

case class Section(sectionTitle: Option[String], rows: Seq[SummaryList.Row])

object Section {
  implicit def sectionWrites(implicit messages: Messages): Writes[Section] = new Writes[Section] {
    override def writes(o: Section): JsValue =
      Json.obj(
        "sectionTitle" -> o.sectionTitle,
        "rows" -> Json.toJson(o.rows)
      )
  }
}