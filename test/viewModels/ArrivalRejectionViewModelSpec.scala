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
import controllers.routes
import generators.MessagesModelGenerators
import models.ArrivalId
import models.messages.ErrorType.{DuplicateMrn, GenericError, MRNError}
import models.messages.{ArrivalNotificationRejectionMessage, ErrorPointer, ErrorType, FunctionalError}
import org.scalacheck.{Arbitrary, Gen}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsObject, Json}

class ArrivalRejectionViewModelSpec extends SpecBase with ScalaCheckPropertyChecks with MessagesModelGenerators {

  "json data for the view" - {

    "when there is one functional error" - {
      "relating to the MRN" in {
        forAll(Arbitrary.arbitrary[MRNError]) {
          error =>
            val enquiriesUrl = "testEnquiriesUrl"
            val arrivalId    = ArrivalId(1)

            val rejectionMessage =
              ArrivalNotificationRejectionMessage(
                movementReferenceNumber = mrn.toString,
                rejectionDate           = LocalDate.now(),
                action                  = None,
                reason                  = None,
                errors                  = Seq(FunctionalError(error, ErrorPointer(""), None, None))
              )
            val vm = ArrivalRejectionViewModel(rejectionMessage, enquiriesUrl, arrivalId)

            val expectedViewData =
              Json.obj(
                "mrn"                        -> mrn,
                "errorKey"                   -> MrnErrorDescription(error),
                "contactUrl"                 -> enquiriesUrl,
                "movementReferenceNumberUrl" -> routes.UpdateRejectedMRNController.onPageLoad(arrivalId).url
              )

            vm.viewData mustEqual expectedViewData
        }
      }

      "when there a generic error" in {
        forAll(Arbitrary.arbitrary[GenericError]) {
          error =>
            val enquiriesUrl = "testEnquiriesUrl"
            val arrivalId    = ArrivalId(1)

            val rejectionMessage =
              ArrivalNotificationRejectionMessage(
                movementReferenceNumber = mrn.toString,
                rejectionDate           = LocalDate.now(),
                action                  = None,
                reason                  = None,
                errors                  = Seq(FunctionalError(error, ErrorPointer(""), None, None))
              )
            val vm = ArrivalRejectionViewModel(rejectionMessage, enquiriesUrl, arrivalId)

            val expectedViewData =
              Json.obj(
                "mrn"              -> mrn,
                "errors"           -> rejectionMessage.errors,
                "contactUrl"       -> enquiriesUrl,
                "createArrivalUrl" -> routes.MovementReferenceNumberController.onPageLoad().url
              )

            vm.viewData mustEqual expectedViewData
        }
      }
    }

  }

  "page returns" - {
    "view for MRN Rejection when there is a single error for MRN" in {
      forAll(Arbitrary.arbitrary[MRNError]) {
        error =>
          val enquiriesUrl = "testEnquiriesUrl"
          val arrivalId    = ArrivalId(1)

          val rejectionMessage =
            ArrivalNotificationRejectionMessage(
              movementReferenceNumber = mrn.toString,
              rejectionDate           = LocalDate.now(),
              action                  = None,
              reason                  = None,
              errors                  = Seq(FunctionalError(error, ErrorPointer(""), None, None))
            )
          val vm = ArrivalRejectionViewModel(rejectionMessage, enquiriesUrl, arrivalId)

          vm.page mustEqual "movementReferenceNumberRejection.njk"
      }
    }

    "view for Generic Rejections when there is a single error for MRN" in {
      forAll(Arbitrary.arbitrary[GenericError]) {
        error =>
          val enquiriesUrl = "testEnquiriesUrl"
          val arrivalId    = ArrivalId(1)

          val rejectionMessage =
            ArrivalNotificationRejectionMessage(
              movementReferenceNumber = mrn.toString,
              rejectionDate           = LocalDate.now(),
              action                  = None,
              reason                  = None,
              errors                  = Seq(FunctionalError(error, ErrorPointer(""), None, None))
            )
          val vm = ArrivalRejectionViewModel(rejectionMessage, enquiriesUrl, arrivalId)

          vm.page mustEqual "arrivalGeneralRejection.njk"
      }

    }
  }

}
