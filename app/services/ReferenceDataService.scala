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

package services

import connectors.ReferenceDataConnector
import javax.inject.Inject
import models.reference.CustomsOffice
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class ReferenceDataService @Inject()(referenceDataConnector: ReferenceDataConnector) {

  def getCustomsOffice(customsOfficeId: String)(implicit hc: HeaderCarrier): Future[Option[CustomsOffice]] =
    referenceDataConnector.getCustomsOffices() map (_.find(_.id == customsOfficeId))

  def getCustomsOfficesAsJson(value: Option[String])(implicit hc: HeaderCarrier): Future[Seq[JsObject]] =
    referenceDataConnector.getCustomsOffices() map {
      offices =>
        val jsOfficeObjects = offices.map {
          office =>
            Json.obj(
              "value"    -> office.id,
              "text"     -> s"${office.name} (${office.id})",
              "selected" -> value.contains(office.id)
            )
        }
        Json.obj("value" -> "", "text" -> "") +: jsOfficeObjects
    }
}
