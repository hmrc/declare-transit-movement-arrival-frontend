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

package models.domain

import models.messages.Trader
import play.api.libs.json.{Format, Json}

final case class TraderDomain(
  name: String,
  streetAndNumber: String,
  postCode: String,
  city: String,
  countryCode: String,
  eori: String
)

object TraderDomain {

  def domainTraderToMessagesTrader(trader: TraderDomain): Trader =
    TraderDomain.unapply(trader).map((Trader.apply _).tupled).get

  object Constants {
    val eoriLength            = 14
    val nameLength            = 35
    val streetAndNumberLength = 35
    val postCodeLength        = 9
    val cityLength            = 35
  }

  /** letters a to z
    * numbers 0 to 9
    * ampersands (&)
    * apostrophes
    * asterisks,
    * forward slashes
    * full stops
    * hyphens
    * question marks
    * and greater than (>) and less than (<) signs
    */
  val eoriRegex       = "(\\s*[a-zA-Z]\\s*){2}(\\s*[0-9]\\s*){1,}"
  val eoriUkXiRegex   = "(?i)\\s*(g\\s*b|x\\s*i)(\\s*[0-9 ]\\s*){1,}"
  val eoriLengthRegex = "^(\\s*[A-Za-z0-9]\\s*){1,14}$"

  implicit lazy val format: Format[TraderDomain] =
    Json.format[TraderDomain]

}
