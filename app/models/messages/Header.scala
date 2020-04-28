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

import java.time.LocalDate

import models.{LanguageCode, LanguageCodeEnglish, ProcedureTypeFlag, XMLWrites}
import utils.Format

import scala.xml.NodeSeq

case class Header(movementReferenceNumber: String,
                  customsSubPlace: Option[String] = None,
                  arrivalNotificationPlace: String,
                  presentationOfficeId: String,
                  presentationOfficeName: String,
                  arrivalAgreedLocationOfGoods: Option[String] = None,
                  procedureTypeFlag: ProcedureTypeFlag,
                  notificationDate: LocalDate)

object Header {

  object Constants {
    val languageCode: LanguageCode     = LanguageCodeEnglish
    val customsSubPlaceLength          = 17
    val arrivalNotificationPlaceLength = 35
  }

  implicit def writes: XMLWrites[Header] = XMLWrites[Header] {
    header =>
      <HEAHEA>
        <DocNumHEA5>{escapeXml(header.movementReferenceNumber)}</DocNumHEA5>
          {
            header.customsSubPlace.fold(NodeSeq.Empty) { place =>
                <CusSubPlaHEA66>{escapeXml(place)}</CusSubPlaHEA66>
              }
          }
          <ArrNotPlaHEA60>{escapeXml(header.arrivalNotificationPlace)}</ArrNotPlaHEA60>
          <ArrNotPlaHEA60LNG>{Header.Constants.languageCode.code}</ArrNotPlaHEA60LNG>
          <ArrAgrLocCodHEA62>{escapeXml(header.presentationOfficeId)}</ArrAgrLocCodHEA62>
          <ArrAgrLocOfGooHEA63>{escapeXml(header.presentationOfficeName)}</ArrAgrLocOfGooHEA63>
          <ArrAgrLocOfGooHEA63LNG>{Header.Constants.languageCode.code}</ArrAgrLocOfGooHEA63LNG>
          {
            header.arrivalAgreedLocationOfGoods.fold(NodeSeq.Empty) { location =>
                <ArrAutLocOfGooHEA65>{escapeXml(location)}</ArrAutLocOfGooHEA65>
              }
          }
          <SimProFlaHEA132>{header.procedureTypeFlag.code}</SimProFlaHEA132>
          <ArrNotDatHEA141>{Format.dateFormatted(header.notificationDate)}</ArrNotDatHEA141>
      </HEAHEA>
  }
}
