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

///*
// * Copyright 2020 HM Revenue & Customs
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package models.messages
//
//import com.lucidchart.open.xtract.XmlReader
//import generators.MessagesModelGenerators
//import models.LanguageCodeEnglish
//import models.XMLWrites._
//import org.scalacheck.Arbitrary.arbitrary
//import org.scalatest.{FreeSpec, MustMatchers, StreamlinedXmlEquality}
//import org.scalatest.OptionValues._
//import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
//
//import scala.xml.NodeSeq
//import scala.xml.Utility.trim
//
//class TraderDestinationSpec extends FreeSpec with MustMatchers with ScalaCheckPropertyChecks with MessagesModelGenerators with StreamlinedXmlEquality {
//
//  "TraderDestination" - {
//    "must create minimal valid xml" in {
//
//      val traderDestination = TraderDestination(None, None, None, None, None, None)
//
//      val expectedResult: NodeSeq =
//        <TRADESTRD>
//          <NADLNGRD>{LanguageCodeEnglish.code}</NADLNGRD>
//        </TRADESTRD>
//
//      traderDestination.toXml mustEqual expectedResult
//    }
//
//    "must create valid xml" in {
//      forAll(arbitrary[TraderDestination]) {
//        traderDestination =>
//          val nameNode = traderDestination.name.map(
//            name => <NamTRD7>{name}</NamTRD7>
//          )
//          val streetNameNode = traderDestination.streetAndNumber.map(
//            streetName => <StrAndNumTRD22>{streetName}</StrAndNumTRD22>
//          )
//          val postCodeNode = traderDestination.postCode.map(
//            postcode => <PosCodTRD23>{postcode}</PosCodTRD23>
//          )
//          val cityNode = traderDestination.city.map(
//            city => <CitTRD24>{city}</CitTRD24>
//          )
//          val countryCodeNode = traderDestination.countryCode.map(
//            countryCode => <CouTRD25>{countryCode}</CouTRD25>
//          )
//          val eoriNode = traderDestination.eori.map(
//            eori => <TINTRD59>{eori}</TINTRD59>
//          )
//
//          val expectedResult =
//            <TRADESTRD>
//            {
//              nameNode.getOrElse(NodeSeq.Empty) ++
//              streetNameNode.getOrElse(NodeSeq.Empty) ++
//              postCodeNode.getOrElse(NodeSeq.Empty) ++
//              cityNode.getOrElse(NodeSeq.Empty) ++
//              countryCodeNode.getOrElse(NodeSeq.Empty) ++
//              <NADLNGRD>{LanguageCodeEnglish.code}</NADLNGRD> ++
//              eoriNode.getOrElse(NodeSeq.Empty)
//            }
//            </TRADESTRD>
//
//          traderDestination.toXml mustEqual expectedResult
//      }
//    }
//
//    "must read xml into valid model" in {
//
//      forAll(arbitrary[TraderDestination]) {
//        traderDestination =>
//          val nameNode = traderDestination.name.map(
//            name => <NamTRD7>{name}</NamTRD7>
//          )
//          val streetNameNode = traderDestination.streetAndNumber.map(
//            streetName => <StrAndNumTRD22>{streetName}</StrAndNumTRD22>
//          )
//          val postCodeNode = traderDestination.postCode.map(
//            postcode => <PosCodTRD23>{postcode}</PosCodTRD23>
//          )
//          val cityNode = traderDestination.city.map(
//            city => <CitTRD24>{city}</CitTRD24>
//          )
//          val countryCodeNode = traderDestination.countryCode.map(
//            countryCode => <CouTRD25>{countryCode}</CouTRD25>
//          )
//          val eoriNode = traderDestination.eori.map(
//            eori => <TINTRD59>{eori}</TINTRD59>
//          )
//
//          val inputXml =
//            <TRADESTRD>
//            {
//            nameNode.getOrElse(NodeSeq.Empty) ++
//              streetNameNode.getOrElse(NodeSeq.Empty) ++
//              postCodeNode.getOrElse(NodeSeq.Empty) ++
//              cityNode.getOrElse(NodeSeq.Empty) ++
//              countryCodeNode.getOrElse(NodeSeq.Empty) ++
//              <NADLNGRD>{LanguageCodeEnglish.code}</NADLNGRD> ++
//              eoriNode.getOrElse(NodeSeq.Empty)
//            }
//          </TRADESTRD>
//
//          val result = XmlReader.of[TraderDestination].read(inputXml).toOption.value
//
//          result mustEqual traderDestination
//
//      }
//
//    }
//
//    "must write to xml and read xml as trader destination" in {
//      forAll(arbitrary[TraderDestination]) {
//        traderDestination =>
//          val result = XmlReader.of[TraderDestination].read(traderDestination.toXml).toOption.value
//
//          result mustEqual traderDestination
//
//      }
//    }
//
//  }
//}
