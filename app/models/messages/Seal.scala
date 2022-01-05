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

import com.lucidchart.open.xtract.{__, XmlReader}
import models.domain.SealDomain
import models.{LanguageCode, LanguageCodeEnglish, XMLWrites}

case class Seal(numberOrMark: String)

object Seal {

  object Constants {
    val sealNumberOrMarkLength     = 20
    val languageCode: LanguageCode = LanguageCodeEnglish
  }

  def sealToDomain(seal: Seal): SealDomain = Seal.unapply(seal).map(SealDomain.apply).get

  implicit def writes: XMLWrites[Seal] = XMLWrites[Seal] {
    seal =>
      <SEAIDSI1>
        {
        <SeaIdeSI11>{escapeXml(seal.numberOrMark)}</SeaIdeSI11> ++
          <SeaIdeSI11LNG>{Seal.Constants.languageCode.code}</SeaIdeSI11LNG>
      }
      </SEAIDSI1>
  }

  implicit lazy val xmlReader: XmlReader[Seal] = (__ \ "SeaIdeSI11").read[String] map apply

}
