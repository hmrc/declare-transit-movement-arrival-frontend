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

import cats.syntax.all._
import com.lucidchart.open.xtract.XmlReader._
import com.lucidchart.open.xtract.{XmlReader, __ => xmlPath}
import models.{LanguageCodeEnglish, XMLWrites}

import scala.xml.NodeSeq

case class TraderDestination(name: Option[String],
                             streetAndNumber: Option[String],
                             postCode: Option[String],
                             city: Option[String],
                             countryCode: Option[String],
                             eori: Option[String])

object TraderDestination {

  implicit val writes: XMLWrites[TraderDestination] = {
    XMLWrites(a => <TRADESTRD>
        {
          a.name.fold(NodeSeq.Empty) {
            name =>
            <NamTRD7>{name}</NamTRD7>
          } ++ {
            a.streetAndNumber.fold(NodeSeq.Empty) {
              streetAndNumber =>
                <StrAndNumTRD22>{streetAndNumber}</StrAndNumTRD22>
            }
          } ++ {
            a.postCode.fold(NodeSeq.Empty) {
              postCode =>
                <PosCodTRD23>{postCode}</PosCodTRD23>
            }
          } ++ {
            a.city.fold(NodeSeq.Empty) {
              city =>
                <CitTRD24>{city}</CitTRD24>
            }
          } ++ {
            a.countryCode.fold(NodeSeq.Empty) {
              countryCode =>
                <CouTRD25>{countryCode}</CouTRD25>
            }
          } ++
          <NADLNGRD>{LanguageCodeEnglish.code}</NADLNGRD> ++ {
            a.eori.fold(NodeSeq.Empty) {
              eori =>
                <TINTRD59>{eori}</TINTRD59>
            }
          }
        }
      </TRADESTRD>)
  }
  implicit val XmlReader: XmlReader[TraderDestination] =
    (
      (xmlPath \ "NamTRD7").read[String].optional,
      (xmlPath \ "StrAndNumTRD22").read[String].optional,
      (xmlPath \ "PosCodTRD23").read[String].optional,
      (xmlPath \ "CitTRD24").read[String].optional,
      (xmlPath \ "CouTRD25").read[String].optional,
      (xmlPath \ "TINTRD59").read[String].optional
    ).mapN(apply)

  object Constants {
    val eoriLength            = 17
    val nameLength            = 35
    val streetAndNumberLength = 35
    val postCodeLength        = 9
    val cityLength            = 35
    val countryCodeLength     = 2
  }
}
