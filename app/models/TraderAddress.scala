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

package models

import play.api.libs.json._
import uk.gov.hmrc.viewmodels.Html

case class TraderAddress (buildingAndStreet: Option[String], city: Option[String],  postcode: Option[String]) {
  def toHtml = Html(
    Seq(buildingAndStreet, city, postcode)
      .flatten
      .mkString(",<br>")
  )
}

object TraderAddress {
  implicit val format = Json.format[TraderAddress]
}
