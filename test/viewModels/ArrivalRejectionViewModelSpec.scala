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

package viewModels

import base.SpecBase
import controllers.routes
import generators.MessagesModelGenerators
import models.ArrivalId
import models.messages.ErrorType.{GenericError, MRNError}
import models.messages.{ArrivalNotificationRejectionMessage, ErrorPointer, ErrorType, FunctionalError}
import org.scalacheck.{Arbitrary, Gen}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json

import java.time.LocalDate

class ArrivalRejectionViewModelSpec extends SpecBase with ScalaCheckPropertyChecks with MessagesModelGenerators {

  implicit val functionalError: Gen[FunctionalError] =
    Arbitrary.arbitrary[ErrorType].map(FunctionalError(_, ErrorPointer(""), None, None))

  "json data for the view" - {
    val rejectionInformation = "reasonValue"

    "when there are no functional errors" - {

      "when there is no reason with the ArrivalNotificationRejectionMessage" in {
        val enquiriesUrl = "testEnquiriesUrl"
        val arrivalId    = ArrivalId(1)

        val rejectionMessage =
          ArrivalNotificationRejectionMessage(
            movementReferenceNumber = mrn.toString,
            rejectionDate = LocalDate.now(),
            action = None,
            reason = None,
            errors = Seq.empty
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

      "when there is a reason with the ArrivalNotificationRejectionMessage" in {
        val enquiriesUrl = "testEnquiriesUrl"
        val arrivalId    = ArrivalId(1)

        val rejectionMessage =
          ArrivalNotificationRejectionMessage(
            movementReferenceNumber = mrn.toString,
            rejectionDate = LocalDate.now(),
            action = None,
            reason = Some(rejectionInformation),
            errors = Seq.empty
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

    "when there is one functional error" - {

      "relating to the MRN" - {

        "when there is no reason with the ArrivalNotificationRejectionMessage" in {
          forAll(Arbitrary.arbitrary[MRNError]) {
            error =>
              val enquiriesUrl = "testEnquiriesUrl"
              val arrivalId    = ArrivalId(1)

              val rejectionMessage =
                ArrivalNotificationRejectionMessage(
                  movementReferenceNumber = mrn.toString,
                  rejectionDate = LocalDate.now(),
                  action = None,
                  reason = None,
                  errors = Seq(FunctionalError(error, ErrorPointer(""), None, None))
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

        "when there is a reason with the ArrivalNotificationRejectionMessage" in {
          forAll(Arbitrary.arbitrary[MRNError]) {
            error =>
              val enquiriesUrl = "testEnquiriesUrl"
              val arrivalId    = ArrivalId(1)

              val rejectionMessage =
                ArrivalNotificationRejectionMessage(
                  movementReferenceNumber = mrn.toString,
                  rejectionDate = LocalDate.now(),
                  action = None,
                  reason = Some(rejectionInformation),
                  errors = Seq(FunctionalError(error, ErrorPointer(""), None, None))
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

      }

      "when there a generic error" - {

        "when there is no reason with the ArrivalNotificationRejectionMessage" in {
          forAll(Arbitrary.arbitrary[GenericError]) {
            error =>
              val enquiriesUrl = "testEnquiriesUrl"
              val arrivalId    = ArrivalId(1)

              val rejectionMessage =
                ArrivalNotificationRejectionMessage(
                  movementReferenceNumber = mrn.toString,
                  rejectionDate = LocalDate.now(),
                  action = None,
                  reason = None,
                  errors = Seq(FunctionalError(error, ErrorPointer(""), None, None))
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

        "asdf when there is a reason with the ArrivalNotificationRejectionMessage" in {
          forAll(Arbitrary.arbitrary[GenericError]) {
            error =>
              val enquiriesUrl = "testEnquiriesUrl"
              val arrivalId    = ArrivalId(1)

              val rejectionMessage =
                ArrivalNotificationRejectionMessage(
                  movementReferenceNumber = mrn.toString,
                  rejectionDate = LocalDate.now(),
                  action = None,
                  reason = Some(rejectionInformation),
                  errors = Seq(FunctionalError(error, ErrorPointer(""), None, None))
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

    "when there are multiple functional errors" - {

      "when there is no reason with the ArrivalNotificationRejectionMessage" in {
        forAll(listInLengthRange(2, 5)(Arbitrary(functionalError))) {
          functionalErrors =>
            val enquiriesUrl = "testEnquiriesUrl"
            val arrivalId    = ArrivalId(1)

            val rejectionMessage =
              ArrivalNotificationRejectionMessage(
                movementReferenceNumber = mrn.toString,
                rejectionDate = LocalDate.now(),
                action = None,
                reason = None,
                errors = functionalErrors
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

      "when there is a reason with the ArrivalNotificationRejectionMessage" in {

        forAll(listInLengthRange(2, 5)(Arbitrary(functionalError))) {
          functionalErrors =>
            val enquiriesUrl = "testEnquiriesUrl"
            val arrivalId    = ArrivalId(1)

            val rejectionMessage =
              ArrivalNotificationRejectionMessage(
                movementReferenceNumber = mrn.toString,
                rejectionDate = LocalDate.now(),
                action = None,
                reason = Some(rejectionInformation),
                errors = functionalErrors
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

  "page" - {

    "when there are no errors, it returns the view for Arrival General Rejections" in {

      val enquiriesUrl = "testEnquiriesUrl"
      val arrivalId    = ArrivalId(1)

      val rejectionMessage =
        ArrivalNotificationRejectionMessage(
          movementReferenceNumber = mrn.toString,
          rejectionDate = LocalDate.now(),
          action = None,
          reason = None,
          errors = Seq.empty
        )
      val vm = ArrivalRejectionViewModel(rejectionMessage, enquiriesUrl, arrivalId)

      vm.page mustEqual "arrivalGeneralRejection.njk"

    }

    "when there is a single error" - {

      "for a MRN Error, then it returns the view for MRN Rejection" in {
        forAll(Arbitrary.arbitrary[MRNError]) {
          error =>
            val enquiriesUrl = "testEnquiriesUrl"
            val arrivalId    = ArrivalId(1)

            val rejectionMessage =
              ArrivalNotificationRejectionMessage(
                movementReferenceNumber = mrn.toString,
                rejectionDate = LocalDate.now(),
                action = None,
                reason = None,
                errors = Seq(FunctionalError(error, ErrorPointer(""), None, None))
              )
            val vm = ArrivalRejectionViewModel(rejectionMessage, enquiriesUrl, arrivalId)

            vm.page mustEqual "movementReferenceNumberRejection.njk"
        }
      }

      "for a Generic Error, then it returns the view for Arrival General Rejections" in {
        forAll(Arbitrary.arbitrary[GenericError]) {
          error =>
            val enquiriesUrl = "testEnquiriesUrl"
            val arrivalId    = ArrivalId(1)

            val rejectionMessage =
              ArrivalNotificationRejectionMessage(
                movementReferenceNumber = mrn.toString,
                rejectionDate = LocalDate.now(),
                action = None,
                reason = None,
                errors = Seq(FunctionalError(error, ErrorPointer(""), None, None))
              )
            val vm = ArrivalRejectionViewModel(rejectionMessage, enquiriesUrl, arrivalId)

            vm.page mustEqual "arrivalGeneralRejection.njk"
        }

      }

    }

    "when there are multiple errors, it returns the view for Arrival General Rejections" in {
      forAll(listInLengthRange(2, 5)(Arbitrary(functionalError))) {
        functionalErrors =>
          val enquiriesUrl = "testEnquiriesUrl"
          val arrivalId    = ArrivalId(1)

          val rejectionMessage =
            ArrivalNotificationRejectionMessage(
              movementReferenceNumber = mrn.toString,
              rejectionDate = LocalDate.now(),
              action = None,
              reason = None,
              errors = functionalErrors
            )
          val vm = ArrivalRejectionViewModel(rejectionMessage, enquiriesUrl, arrivalId)

          vm.page mustEqual "arrivalGeneralRejection.njk"
      }
    }
  }

}
