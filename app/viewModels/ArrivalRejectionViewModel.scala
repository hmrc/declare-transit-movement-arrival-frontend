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

import controllers.routes
import models.ArrivalId
import models.messages.{ArrivalNotificationRejectionMessage, ErrorType, FunctionalError}
import models.messages.ErrorType._
import play.api.libs.json.{JsObject, Json, OWrites}

final private case class RejectionViewDataNoFunctionalErrors(
  mrn: String,
  errorKey: String,
  contactUrl: String,
  movementReferenceNumberUrl: String
)

private object RejectionViewDataNoFunctionalErrors {
  implicit val writes: OWrites[RejectionViewDataNoFunctionalErrors] = Json.writes[RejectionViewDataNoFunctionalErrors]
}

final private case class RejectionViewDataFunctionalErrors(
  mrn: String,
  errors: Seq[FunctionalError],
  contactUrl: String,
  createArrivalUrl: String
)

private object RejectionViewDataFunctionalErrors {
  implicit val writes: OWrites[RejectionViewDataFunctionalErrors] = Json.writes[RejectionViewDataFunctionalErrors]
}

class ArrivalRejectionViewModel(val rejectionMessage: ArrivalNotificationRejectionMessage, enquiriesUrl: String, arrivalId: ArrivalId) {

  private val mrnRejectionPage     = "movementReferenceNumberRejection.njk"
  private val genericRejectionPage = "arrivalGeneralRejection.njk"

  val (page, viewData): (String, JsObject) =
    rejectionMessage.errors match {
      case FunctionalError(mrnError: MRNError, _, _, _) :: Nil =>
        val data = RejectionViewDataNoFunctionalErrors(
          mrn                        = rejectionMessage.movementReferenceNumber,
          errorKey                   = MrnErrorDescription(mrnError),
          contactUrl                 = enquiriesUrl,
          movementReferenceNumberUrl = routes.UpdateRejectedMRNController.onPageLoad(arrivalId).url
        )

        (mrnRejectionPage, Json.toJsObject(data))

      case _ =>
        val data = RejectionViewDataFunctionalErrors(
          mrn              = rejectionMessage.movementReferenceNumber,
          errors           = rejectionMessage.errors,
          contactUrl       = enquiriesUrl,
          createArrivalUrl = routes.MovementReferenceNumberController.onPageLoad().url
        )

        (genericRejectionPage, Json.toJsObject(data))
    }
}

object ArrivalRejectionViewModel {

  def apply(rejectionMessage: ArrivalNotificationRejectionMessage, enquiriesUrl: String, arrivalId: ArrivalId): ArrivalRejectionViewModel =
    new ArrivalRejectionViewModel(rejectionMessage, enquiriesUrl, arrivalId)

  def unapply(arg: ArrivalRejectionViewModel): Some[(String, JsObject)] = Some((arg.page, arg.viewData))

}
