/*
 * Copyright 2023 HM Revenue & Customs
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

package models

import base.SpecBase
import com.lucidchart.open.xtract.{ParseFailure, ParseSuccess, XmlReader}
import generators.Generators
import models.XMLReads._
import org.scalacheck.Arbitrary.arbitrary
import utils.Format
import utils.Format.timeFormatter

import java.time.{LocalDate, LocalTime}
import scala.xml.NodeSeq

class XMLReadsSpec extends SpecBase with Generators {

  "XMLReads" - {

    "xmlDateReads" - {

      "must deserialize XML to LocalDate with correct format" in {

        val date = arbitrary[LocalDate].sample.value

        val xml = <testXml>{Format.dateFormatted(date)}</testXml>

        val result = XmlReader.of[LocalDate].read(xml).toOption.value

        result mustBe date
      }

      "must return ParseFailure when failing to deserialize XML to LocalDate" in {

        val xml = <testXml>Invalid Date</testXml>

        val result = XmlReader.of[LocalDate].read(xml)

        result mustBe an[ParseFailure]
      }
    }

    "xmlTimeReads" - {

      "must deserialize XML to LocalTime with correct format" in {

        val time                = arbitrary[LocalTime].sample.value
        val timeFormatted       = Format.timeFormatted(time)
        val timeFormattedParsed = LocalTime.parse(timeFormatted, timeFormatter)

        val xml = <testXml>{timeFormatted}</testXml>

        val result = XmlReader.of[LocalTime].read(xml).toOption.value

        result mustBe timeFormattedParsed
      }

      "must return ParseFailure when failing to deserialize XML to LocalTime" in {

        val xml = <testXml>Invalid Date</testXml>

        val result = XmlReader.of[LocalTime].read(xml)

        result mustBe an[ParseFailure]
      }

      "must return ParseSuccess(Some(_)) for valid xml sequence" in {
        val xml = NodeSeq.fromSeq(
          <xml>a</xml>
            <xml>b</xml>
            <xml>c</xml>
        )
        strictReadOptionSeq[String].read(xml) mustBe ParseSuccess(Some(List("a", "b", "c")))
      }

      "must return ParseSuccess('None') for empty Sequence" in {
        val xml = NodeSeq.Empty
        strictReadOptionSeq[String].read(xml) mustBe ParseSuccess(None)
      }
    }
  }

}
