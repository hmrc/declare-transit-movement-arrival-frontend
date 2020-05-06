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

package models

import java.time.LocalDate

import base.SpecBase
import com.lucidchart.open.xtract.{ParseFailure, XmlReader}
import generators.Generators
import models.XMLReads._
import org.scalacheck.Arbitrary.arbitrary
import utils.Format

class XMLReadsSpec extends SpecBase with Generators {

  "XMLReads" - {

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

}
