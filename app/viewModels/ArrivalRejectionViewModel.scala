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

import controllers.routes
import models.ArrivalId
import models.messages.ErrorType._
import models.messages.{ArrivalNotificationRejectionMessage, FunctionalError}
import play.api.libs.json.{JsObject, Json, OWrites}
import uk.gov.hmrc.viewmodels.NunjucksSupport

object NunjucksSupportObject extends NunjucksSupport

sealed trait RejectionViewData

private object RejectionViewData {

  implicit val writes: OWrites[RejectionViewData] = OWrites {
    case x: RejectionViewDataNoFunctionalErrors => Json.toJsObject(x)(RejectionViewDataNoFunctionalErrors.writes)
    case x: RejectionViewDataFunctionalErrors   => Json.toJsObject(x)(RejectionViewDataFunctionalErrors.writes)
  }
}

final private case class RejectionViewDataNoFunctionalErrors(
  mrn: String,
  errorKey: String,
  contactUrl: String,
  movementReferenceNumberUrl: String
) extends RejectionViewData

private object RejectionViewDataNoFunctionalErrors {
  implicit val writes: OWrites[RejectionViewDataNoFunctionalErrors] = Json.writes[RejectionViewDataNoFunctionalErrors]
}

final private case class RejectionViewDataFunctionalErrors(
  mrn: String,
  errors: Seq[FunctionalError],
  contactUrl: String,
  createArrivalUrl: String
) extends RejectionViewData

private object RejectionViewDataFunctionalErrors {
  implicit val writes: OWrites[RejectionViewDataFunctionalErrors] = Json.writes[RejectionViewDataFunctionalErrors]
}

class ArrivalRejectionViewModel(
  rejectionMessage: ArrivalNotificationRejectionMessage,
  enquiriesUrl: String,
  arrivalId: ArrivalId
) {

  private val mrnRejectionPage     = "movementReferenceNumberRejection.njk"
  private val genericRejectionPage = "arrivalGeneralRejection.njk"

  private val (_page, _viewData): (String, RejectionViewData) =
    rejectionMessage.errors match {
      case FunctionalError(mrnError: MRNError, _, _, _) :: Nil =>
        val data = RejectionViewDataNoFunctionalErrors(
          mrn = rejectionMessage.movementReferenceNumber,
          errorKey = MrnErrorDescription(mrnError),
          contactUrl = enquiriesUrl,
          movementReferenceNumberUrl = routes.UpdateRejectedMRNController.onPageLoad(arrivalId).url
        )

        (mrnRejectionPage, data)

      case _ =>
        val data = RejectionViewDataFunctionalErrors(
          mrn = rejectionMessage.movementReferenceNumber,
          errors = rejectionMessage.errors,
          contactUrl = enquiriesUrl,
          createArrivalUrl = routes.MovementReferenceNumberController.onPageLoad().url
        )

        (genericRejectionPage, data)
    }

  val page: String = _page

  def viewData: JsObject = Json.toJsObject(_viewData)
}

object ArrivalRejectionViewModel {

  def apply(rejectionMessage: ArrivalNotificationRejectionMessage, enquiriesUrl: String, arrivalId: ArrivalId): ArrivalRejectionViewModel =
    new ArrivalRejectionViewModel(rejectionMessage, enquiriesUrl, arrivalId)

}
