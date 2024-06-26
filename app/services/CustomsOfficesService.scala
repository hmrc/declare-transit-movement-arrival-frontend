/*
 * Copyright 2023 HM Revenue & Customs
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

import config.FrontendAppConfig
import connectors.ReferenceDataConnector
import models.CustomsOfficeList
import models.reference.{CountryCode, CustomsOffice}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CustomsOfficesService @Inject() (
  referenceDataConnector: ReferenceDataConnector,
  config: FrontendAppConfig
)(implicit ec: ExecutionContext) {

  def getCustomsOfficesOfArrival(implicit hc: HeaderCarrier): Future[CustomsOfficeList] = {

    def getCustomsOfficesForCountry(countryCode: String): Future[Seq[CustomsOffice]] =
      referenceDataConnector.getCustomsOfficesForCountry(CountryCode(countryCode))

    Future
      .sequence(config.countriesOfDestination.map(getCustomsOfficesForCountry))
      .map(_.flatten)
      .map(sort)
  }

  private def sort(customsOffices: Seq[CustomsOffice]): CustomsOfficeList =
    CustomsOfficeList(customsOffices.sortBy(_.name.map(_.toLowerCase)))
}
