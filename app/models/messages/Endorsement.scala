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

package models.messages

import java.time.LocalDate

import play.api.libs.json.{Format, Json}

final case class Endorsement(date: Option[LocalDate], authority: Option[String], place: Option[String], country: Option[String])

object Endorsement {

  object Constants {
    val authorityLength = 35
    val placeLength     = 35
    val countryLength   = 2
  }

  implicit lazy val format: Format[Endorsement] =
    Json.format[Endorsement]
}