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
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import utils.Format._

class ArrivalNotificationRejectionMessageSpec extends SpecBase with ScalaCheckDrivenPropertyChecks with MessagesModelGenerators {

  "ArrivalNotificationRejectionMessage" - {
    "must deserialize from XML with minimal answers" in {
      forAll(arbitrary[ArrivalNotificationRejectionMessage], arbitrary[FunctionalError]) {
        (rejectionMessage, functionalError) =>
          val minimalFunctionalError = functionalError.copy(
            reason = None,
            originalAttributeValue = None
          )

          val minimalRejectionMessage = rejectionMessage.copy(
            action = None,
            reason = None,
            errors = Seq(minimalFunctionalError)
          )

          val xml =
            <CC008A>
              <HEAHEA>
                <DocNumHEA5>{minimalRejectionMessage.movementReferenceNumber}</DocNumHEA5>
                <ArrRejDatHEA142>{dateFormatted(minimalRejectionMessage.rejectionDate)}</ArrRejDatHEA142>
              </HEAHEA>
              <FUNERRER1>
                <ErrTypER11>{functionalError.errorType.code}</ErrTypER11>
                <ErrPoiER12>{functionalError.pointer.value}</ErrPoiER12>
              </FUNERRER1>
            </CC008A>

          val result = XmlReader.of[ArrivalNotificationRejectionMessage].read(xml).toOption.value

          result mustEqual minimalRejectionMessage
      }
    }

    "must deserialize from XML with full answers" in {
      forAll(arbitrary[ArrivalNotificationRejectionMessage], arbitrary[FunctionalError]) {
        (rejectionMessage, functionalError) =>
          val fullFunctionalError = functionalError.copy(
            reason = Some(arbitrary[String].sample.value),
            originalAttributeValue = Some(arbitrary[String].sample.value)
          )

          val fullRejectionMessage = rejectionMessage.copy(
            action = Some(arbitrary[String].sample.value),
            reason = Some(arbitrary[String].sample.value),
            errors = Seq(fullFunctionalError)
          )

          val xml =
            <CC008A>
              <HEAHEA>
                <DocNumHEA5>{fullRejectionMessage.movementReferenceNumber}</DocNumHEA5>
                <ArrRejDatHEA142>{dateFormatted(fullRejectionMessage.rejectionDate)}</ArrRejDatHEA142>
                <ActToBeTakHEA238>{fullRejectionMessage.action.value}</ActToBeTakHEA238>
                <ArrRejReaHEA242>{fullRejectionMessage.reason.value}</ArrRejReaHEA242>
              </HEAHEA>
              <FUNERRER1>
                <ErrTypER11>{fullFunctionalError.errorType.code}</ErrTypER11>
                <ErrPoiER12>{fullFunctionalError.pointer.value}</ErrPoiER12>
                <ErrReaER13>{fullFunctionalError.reason.value}</ErrReaER13>
                <OriAttValER14>{fullFunctionalError.originalAttributeValue.value}</OriAttValER14>
              </FUNERRER1>
            </CC008A>

          val result = XmlReader.of[ArrivalNotificationRejectionMessage].read(xml).toOption.value

          result mustEqual fullRejectionMessage
      }
    }
  }

}
