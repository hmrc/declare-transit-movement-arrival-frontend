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

import base.SpecBase
import com.lucidchart.open.xtract.XmlReader
import generators.MessagesModelGenerators
import models.XMLWrites._
import models.{LanguageCodeEnglish, NormalProcedureFlag}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.StreamlinedXmlEquality
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import utils.Format

import java.time.LocalDate
import scala.xml.NodeSeq

class HeaderSpec extends SpecBase with ScalaCheckPropertyChecks with MessagesModelGenerators with StreamlinedXmlEquality {

  "Header" - {
    "must create minimal valid xml" in {

      forAll(arbitrary[Header], arbitrary[LocalDate]) {
        (header, arrivalNotificationDate) =>
          val minimalHeader = Header(
            movementReferenceNumber = header.movementReferenceNumber,
            procedureTypeFlag = NormalProcedureFlag,
            arrivalNotificationPlace = header.arrivalNotificationPlace,
            notificationDate = arrivalNotificationDate
          )

          val expectedResult: NodeSeq =
            <HEAHEA>
              <DocNumHEA5>{escapeXml(minimalHeader.movementReferenceNumber)}</DocNumHEA5>
              <ArrNotPlaHEA60>{escapeXml(minimalHeader.arrivalNotificationPlace)}</ArrNotPlaHEA60>
              <ArrNotPlaHEA60LNG>{LanguageCodeEnglish.code}</ArrNotPlaHEA60LNG>
              <SimProFlaHEA132>{escapeXml(minimalHeader.procedureTypeFlag.code)}</SimProFlaHEA132>
              <ArrNotDatHEA141>{Format.dateFormatted(arrivalNotificationDate)}</ArrNotDatHEA141>
            </HEAHEA>

          minimalHeader.toXml mustEqual expectedResult
      }
    }

    "must create valid xml" in {

      forAll(arbitrary[Header]) {
        header =>
          val normalHeader: Header = header.copy(procedureTypeFlag = NormalProcedureFlag)

          val customsSubPlaceNode = normalHeader.customsSubPlace.map(
            customsSubPlace => <CusSubPlaHEA66>{escapeXml(customsSubPlace)}</CusSubPlaHEA66>
          )

          val authorisedLocationOfGoods = normalHeader.arrivalAuthorisedLocationOfGoods.map(
            arrivalAgreedLocationOfGoods => <ArrAutLocOfGooHEA65>{escapeXml(arrivalAgreedLocationOfGoods)}</ArrAutLocOfGooHEA65>
          )

          val expectedResult: NodeSeq =
            <HEAHEA>
              <DocNumHEA5>{escapeXml(normalHeader.movementReferenceNumber)}</DocNumHEA5>
              {customsSubPlaceNode.getOrElse(NodeSeq.Empty)}
              <ArrNotPlaHEA60>{escapeXml(normalHeader.arrivalNotificationPlace)}</ArrNotPlaHEA60>
              <ArrNotPlaHEA60LNG>{LanguageCodeEnglish.code}</ArrNotPlaHEA60LNG>
              {authorisedLocationOfGoods.getOrElse(NodeSeq.Empty)}
              <SimProFlaHEA132>{normalHeader.procedureTypeFlag.code}</SimProFlaHEA132>
              <ArrNotDatHEA141>{Format.dateFormatted(normalHeader.notificationDate)}</ArrNotDatHEA141>
            </HEAHEA>

          normalHeader.toXml mustEqual expectedResult
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
