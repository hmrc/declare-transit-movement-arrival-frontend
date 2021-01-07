/*
 * Copyright 2021 HM Revenue & Customs
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

package viewModels

import java.time.LocalDate

import base.SpecBase
import models.ArrivalId
import models.messages.ErrorType.DuplicateMrn
import models.messages.{ArrivalNotificationRejectionMessage, ErrorPointer, FunctionalError}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsObject, Json}

class ArrivalRejectionViewModelSpec extends SpecBase with ScalaCheckPropertyChecks {

  "must be able to deserialize to a JsObject" in {
    val vm = ArrivalRejectionViewModel(
      ArrivalNotificationRejectionMessage(mrn.toString, LocalDate.now(), None, None, Seq(FunctionalError(DuplicateMrn, ErrorPointer(""), None, None))),
      "",
      ArrivalId(1)
    )

    Json.toJsObject(vm) mustBe a[JsObject]
  }

  "when an Arrival Rejection is received" - {

    "that does not contain any error messages" ignore {

      val vm = ArrivalRejectionViewModel(ArrivalNotificationRejectionMessage(mrn.toString, LocalDate.now(), None, None, Seq.empty), "", ArrivalId(1))

    }
  }
}
