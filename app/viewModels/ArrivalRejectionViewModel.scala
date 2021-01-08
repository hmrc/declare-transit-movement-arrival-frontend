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
import models.messages.ArrivalNotificationRejectionMessage
import models.messages.ErrorType.{GenericError, MRNError}
import play.api.libs.json.{JsObject, Json, OWrites}

class ArrivalRejectionViewModel(rejectionMessage: ArrivalNotificationRejectionMessage, enquiriesUrl: String, arrivalId: ArrivalId) {

  private val mrnRejectionPage     = "movementReferenceNumberRejection.njk"
  private val genericRejectionPage = "arrivalGeneralRejection.njk"

  val page: String = rejectionMessage.errors.head.errorType match {
    case mrnError: MRNError => mrnRejectionPage
    case _: GenericError    => genericRejectionPage
  }

  private def mrnJson(mrnError: MRNError, rejectionMessage: ArrivalNotificationRejectionMessage, enquiriesUrl: String, arrivalId: ArrivalId): JsObject =
    Json.obj(
      "mrn"                        -> rejectionMessage.movementReferenceNumber,
      "errorKey"                   -> MrnErrorDescription(mrnError),
      "contactUrl"                 -> enquiriesUrl,
      "movementReferenceNumberUrl" -> routes.UpdateRejectedMRNController.onPageLoad(arrivalId).url
    )

  private def genericJson(rejectionMessage: ArrivalNotificationRejectionMessage, enquiriesUrl: String, arrivalId: ArrivalId): JsObject =
    Json.obj(
      "mrn"              -> rejectionMessage.movementReferenceNumber,
      "errors"           -> rejectionMessage.errors,
      "contactUrl"       -> enquiriesUrl,
      "createArrivalUrl" -> routes.MovementReferenceNumberController.onPageLoad().url
    )

  val viewData: JsObject = rejectionMessage.errors.head.errorType match {
    case mrnError: MRNError => mrnJson(mrnError, rejectionMessage, enquiriesUrl, arrivalId)
    case _: GenericError    => genericJson(rejectionMessage, enquiriesUrl, arrivalId)
  }
}

object ArrivalRejectionViewModel {

  def apply(rejectionMessage: ArrivalNotificationRejectionMessage, enquiriesUrl: String, arrivalId: ArrivalId): ArrivalRejectionViewModel =
    new ArrivalRejectionViewModel(rejectionMessage, enquiriesUrl, arrivalId)

  def unapply(arg: ArrivalRejectionViewModel): Some[(String, JsObject)] = Some((arg.page, arg.viewData))

}
