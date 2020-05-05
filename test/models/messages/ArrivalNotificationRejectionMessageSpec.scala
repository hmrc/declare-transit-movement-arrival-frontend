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
import models.{FunctionalError, MovementReferenceNumber}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import utils.Format._

class ArrivalNotificationRejectionMessageSpec extends SpecBase with ScalaCheckDrivenPropertyChecks with MessagesModelGenerators {

  val rejectionMessageXmlGen: Gen[ArrivalNotificationRejectionMessage] =
    for {
      mrn    <- arbitrary[MovementReferenceNumber].map(_.toString())
      date   <- datesBetween(pastDate, dateNow)
      action <- arbitrary[String]
      reason <- arbitrary[String]
      errors <- arbitrary[FunctionalError]
    } yield {
      ArrivalNotificationRejectionMessage(mrn, date, Some(action), Some(reason), Seq(errors))
    }

  "deserialization from XML" - {
    "do the thing" in {
      forAll(rejectionMessageXmlGen) {
        rejectionMessage =>
          val xml = {
            <CC008A>
              <HEAHEA>
                <DocNumHEA5>{rejectionMessage.movementReferenceNumber}</DocNumHEA5>
                <ArrRejDatHEA142>{dateFormatted(rejectionMessage.rejectionDate)}</ArrRejDatHEA142>
                <ActToBeTakHEA238>{rejectionMessage.action.getOrElse("test")}</ActToBeTakHEA238>
                <ArrRejReaHEA242>{rejectionMessage.reason.getOrElse("test")}</ArrRejReaHEA242>
              </HEAHEA>
              <FUNERRER1>
                <ErrTypER11>{rejectionMessage.errors.head.errorType}</ErrTypER11>
                <ErrPoiER12>{rejectionMessage.errors.head.pointer}</ErrPoiER12>
                <ErrReaER13>{rejectionMessage.errors.head.reason.getOrElse("test")}</ErrReaER13>
                <OriAttValER14>{rejectionMessage.errors.head.originalAttributeValue.getOrElse("test")}</OriAttValER14>
              </FUNERRER1>
            </CC008A>
          }

          val result = XmlReader.of[ArrivalNotificationRejectionMessage].read(xml).toOption.value

          result mustEqual rejectionMessage

      }
    }
  }
}
