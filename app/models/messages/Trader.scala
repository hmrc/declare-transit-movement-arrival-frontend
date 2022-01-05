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

package models.messages

import cats.syntax.all._
import com.lucidchart.open.xtract.XmlReader._
import com.lucidchart.open.xtract.{XmlReader, __ => xmlPath}
import models.{LanguageCodeEnglish, XMLWrites}

final case class Trader(name: String, streetAndNumber: String, postCode: String, city: String, countryCode: String, eori: String)

object Trader {

  def messagesTraderToDomainTrader(trader: Trader): models.domain.TraderDomain =
    Trader.unapply(trader).map((models.domain.TraderDomain.apply _).tupled).get

  implicit val writes: XMLWrites[Trader] =
    XMLWrites(
      trader => <TRADESTRD>
      <NamTRD7>{trader.name}</NamTRD7>
      <StrAndNumTRD22>{trader.streetAndNumber}</StrAndNumTRD22>
      <PosCodTRD23>{trader.postCode}</PosCodTRD23>
      <CitTRD24>{trader.city}</CitTRD24>
      <CouTRD25>{trader.countryCode}</CouTRD25>
      <NADLNGRD>{LanguageCodeEnglish.code}</NADLNGRD>
      <TINTRD59>{trader.eori}</TINTRD59>
    </TRADESTRD>
    )

  implicit val XmlReader: XmlReader[Trader] =
    (
      (xmlPath \ "NamTRD7").read[String],
      (xmlPath \ "StrAndNumTRD22").read[String],
      (xmlPath \ "PosCodTRD23").read[String],
      (xmlPath \ "CitTRD24").read[String],
      (xmlPath \ "CouTRD25").read[String],
      (xmlPath \ "TINTRD59").read[String]
    ).mapN(apply)
}
