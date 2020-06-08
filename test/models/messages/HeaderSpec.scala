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

import base.SpecBase
import com.lucidchart.open.xtract.XmlReader
import generators.MessagesModelGenerators
import models.LanguageCodeEnglish
import models.XMLWrites._
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.StreamlinedXmlEquality
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import utils.Format

import scala.xml.NodeSeq

class HeaderSpec extends SpecBase with ScalaCheckPropertyChecks with MessagesModelGenerators with StreamlinedXmlEquality {

  "Header" - {
    "must create minimal valid xml" in {

      forAll(arbitrary[Header], arbitrary[LocalDate]) {
        (header, arrivalNotificationDate) =>
          val minimalHeader = Header(
            movementReferenceNumber  = header.movementReferenceNumber,
            procedureTypeFlag        = header.procedureTypeFlag,
            arrivalNotificationPlace = header.arrivalNotificationPlace,
            notificationDate         = arrivalNotificationDate,
            presentationOfficeId     = header.presentationOfficeId,
            presentationOfficeName   = header.presentationOfficeName
          )

          val expectedResult: NodeSeq =
            <HEAHEA>
              <DocNumHEA5>{escapeXml(minimalHeader.movementReferenceNumber)}</DocNumHEA5>
              <ArrNotPlaHEA60>{escapeXml(minimalHeader.arrivalNotificationPlace)}</ArrNotPlaHEA60>
              <ArrNotPlaHEA60LNG>{LanguageCodeEnglish.code}</ArrNotPlaHEA60LNG>
              <ArrAgrLocCodHEA62>{escapeXml(minimalHeader.presentationOfficeId)}</ArrAgrLocCodHEA62>
              <ArrAgrLocOfGooHEA63>{escapeXml(minimalHeader.presentationOfficeName)}</ArrAgrLocOfGooHEA63>
              <ArrAgrLocOfGooHEA63LNG>{LanguageCodeEnglish.code}</ArrAgrLocOfGooHEA63LNG>
              <SimProFlaHEA132>{escapeXml(minimalHeader.procedureTypeFlag.code)}</SimProFlaHEA132>
              <ArrNotDatHEA141>{Format.dateFormatted(arrivalNotificationDate)}</ArrNotDatHEA141>
            </HEAHEA>

          minimalHeader.toXml mustEqual expectedResult
      }
    }

    "must create valid xml" in {

      forAll(arbitrary[Header]) {
        header =>
          val customsSubPlaceNode = header.customsSubPlace.map(
            customsSubPlace => <CusSubPlaHEA66>{escapeXml(customsSubPlace)}</CusSubPlaHEA66>
          )

          val authorisedLocationOfGoods = header.arrivalAgreedLocationOfGoods.map(
            arrivalAgreedLocationOfGoods => <ArrAutLocOfGooHEA65>{escapeXml(arrivalAgreedLocationOfGoods)}</ArrAutLocOfGooHEA65>
          )

          val expectedResult: NodeSeq =
            <HEAHEA>
              <DocNumHEA5>{escapeXml(header.movementReferenceNumber)}</DocNumHEA5>
              {customsSubPlaceNode.getOrElse(NodeSeq.Empty)}
              <ArrNotPlaHEA60>{escapeXml(header.arrivalNotificationPlace)}</ArrNotPlaHEA60>
              <ArrNotPlaHEA60LNG>{LanguageCodeEnglish.code}</ArrNotPlaHEA60LNG>
              <ArrAgrLocCodHEA62>{escapeXml(header.presentationOfficeId)}</ArrAgrLocCodHEA62>
              <ArrAgrLocOfGooHEA63>{escapeXml(header.presentationOfficeName)}</ArrAgrLocOfGooHEA63>
              <ArrAgrLocOfGooHEA63LNG>{LanguageCodeEnglish.code}</ArrAgrLocOfGooHEA63LNG>
              {authorisedLocationOfGoods.getOrElse(NodeSeq.Empty)}
              <SimProFlaHEA132>{header.procedureTypeFlag.code}</SimProFlaHEA132>
              <ArrNotDatHEA141>{Format.dateFormatted(header.notificationDate)}</ArrNotDatHEA141>
            </HEAHEA>

          header.toXml mustEqual expectedResult
      }
    }

    "must deserialize from xml" in {
      forAll(arbitrary[Header]) {
        header =>
          val xml    = header.toXml
          val result = XmlReader.of[Header].read(xml).toOption.value

          result mustBe header
      }
    }
  }

}
