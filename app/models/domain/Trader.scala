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

package models.domain

import play.api.libs.json.{Format, Json}

final case class Trader(name: String, streetAndNumber: String, postCode: String, city: String, countryCode: String, eori: String)

object Trader {

  def domainTraderToMessagesTrader(trader: Trader): models.messages.Trader =
    Trader.unapply(trader).map((models.messages.Trader.apply _).tupled).get

  object Constants {
    val eoriLength            = 17
    val nameLength            = 35
    val streetAndNumberLength = 35
    val postCodeLength        = 9
    val cityLength            = 35
    val countryCodeLength     = 2
  }

  val eoriRegex = "[A-Z]{2}[^\n\r]{1,}"

  implicit lazy val format: Format[Trader] =
    Json.format[Trader]

}
