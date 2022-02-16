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

package services

import base.SpecBase
import connectors.ReferenceDataConnector
import models.CustomsOfficeList
import models.reference.{CountryCode, CustomsOffice}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CustomsOfficesServiceSpec extends SpecBase with BeforeAndAfterEach {

  val mockRefDataConnector: ReferenceDataConnector = mock[ReferenceDataConnector]

  val gbCustomsOffice1: CustomsOffice      = CustomsOffice("1", Some("BOSTON"), None)
  val gbCustomsOffice2: CustomsOffice      = CustomsOffice("2", Some("Appledore"), None)
  val xiCustomsOffice1: CustomsOffice      = CustomsOffice("3", Some("Belfast"), None)
  val xiCustomsOffice2: CustomsOffice      = CustomsOffice("4", None, None)
  val gbCustomsOffices: Seq[CustomsOffice] = Seq(gbCustomsOffice1, gbCustomsOffice2)
  val xiCustomsOffices: Seq[CustomsOffice] = Seq(xiCustomsOffice1, xiCustomsOffice2)
  val customsOffices: CustomsOfficeList    = CustomsOfficeList(gbCustomsOffices ++ xiCustomsOffices)

  implicit val hc: HeaderCarrier = new HeaderCarrier()

  val service = new CustomsOfficesService(mockRefDataConnector)

  override def beforeEach(): Unit = {
    reset(mockRefDataConnector)
    super.beforeEach()
  }

  "CustomsOfficesService" - {

    "must return a list of GB and NI customs offices" in {

      when(mockRefDataConnector.getCustomsOfficesForCountry(eqTo(CountryCode("XI")))(any(), any())).thenReturn(Future.successful(xiCustomsOffices))
      when(mockRefDataConnector.getCustomsOfficesForCountry(eqTo(CountryCode("GB")))(any(), any())).thenReturn(Future.successful(gbCustomsOffices))

      service.getCustomsOfficesOfArrival.futureValue mustBe
        CustomsOfficeList(Seq(xiCustomsOffice2, gbCustomsOffice2, xiCustomsOffice1, gbCustomsOffice1))

      verify(mockRefDataConnector).getCustomsOfficesForCountry(eqTo(CountryCode("XI")))(any(), any())
      verify(mockRefDataConnector).getCustomsOfficesForCountry(eqTo(CountryCode("GB")))(any(), any())
    }

  }
}
