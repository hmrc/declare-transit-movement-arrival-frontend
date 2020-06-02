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

package viewModels

import controllers.routes
import models.ArrivalId
import models.messages.ArrivalNotificationRejectionMessage
import models.messages.ErrorType.{DuplicateMrn, GenericError, InvalidMrn, MRNError, UnknownMrn}
import play.api.libs.json.{JsObject, Json}

case class ArrivalRejectionViewModel(page: String, json: JsObject)

object ArrivalRejectionViewModel {

  def apply(rejectionMessage: ArrivalNotificationRejectionMessage, enquiriesUrl: String, arrivalId: ArrivalId): ArrivalRejectionViewModel = {

    def mrnJson(mrnError: MRNError): JsObject =
      Json.obj(
        "mrn"                        -> rejectionMessage.movementReferenceNumber,
        "errorKey"                   -> mrnMessage(mrnError),
        "contactUrl"                 -> enquiriesUrl,
        "movementReferenceNumberUrl" -> routes.UpdateRejectedMRNController.onPageLoad(arrivalId).url
      )

    def genericJson: JsObject =
      Json.obj(
        "mrn"              -> rejectionMessage.movementReferenceNumber,
        "errors"           -> rejectionMessage.errors,
        "contactUrl"       -> enquiriesUrl,
        "createArrivalUrl" -> routes.MovementReferenceNumberController.onPageLoad().url
      )

    val mrnRejectionPage     = "movementReferenceNumberRejection.njk"
    val genericRejectionPage = "arrivalGeneralRejection.njk"

    rejectionMessage.errors.head.errorType match {
      case mrnError: MRNError => new ArrivalRejectionViewModel(mrnRejectionPage, mrnJson(mrnError))
      case _: GenericError    => new ArrivalRejectionViewModel(genericRejectionPage, genericJson)
    }
  }

  val mrnMessage: Map[MRNError, String] =
    Map(
      UnknownMrn   -> "movementReferenceNumberRejection.error.unknown",
      DuplicateMrn -> "movementReferenceNumberRejection.error.duplicate",
      InvalidMrn   -> "movementReferenceNumberRejection.error.invalid"
    )

}
