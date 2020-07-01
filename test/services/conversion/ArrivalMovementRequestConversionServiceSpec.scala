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

package services.conversion

import base.SpecBase
import connectors.ReferenceDataConnector
import generators.MessagesModelGenerators
import models.domain.{NormalNotification, TranshipmentDomain, VehicularTranshipmentDomain}
import models.messages.{ArrivalMovementRequest, EnRouteEvent, Header, Transhipment, VehicularTranshipment}
import models.reference.Country
import org.mockito.Matchers.any
import org.mockito.Mockito._
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ArrivalMovementRequestConversionServiceSpec extends SpecBase with MessagesModelGenerators with ScalaCheckPropertyChecks {

  private val mockReferenceDataConnector = mock[ReferenceDataConnector]

  "convertToArrivalNotification" - {

    "must return None if MRN is malformed" in {

      val genCountry: Country                            = arbitrary[Country].sample.value
      val arrivalMovementRequest: ArrivalMovementRequest = arbitrary[ArrivalMovementRequest].sample.value

      when(mockReferenceDataConnector.getCountry(any())(any(), any()))
        .thenReturn(Future.successful(genCountry))

      val application = applicationBuilder(Some(emptyUserAnswers))
        .overrides(bind[ReferenceDataConnector].toInstance(mockReferenceDataConnector))
        .build()

      val arrivalMovementRequestConversionService                        = application.injector.instanceOf[ArrivalMovementRequestConversionService]
      val header: Header                                                 = arrivalMovementRequest.header.copy(movementReferenceNumber = "Invalid MRN")
      val arrivalMovementRequestWithMalformedMrn: ArrivalMovementRequest = arrivalMovementRequest.copy(header = header)

      arrivalMovementRequestConversionService.convertToArrivalNotification(arrivalMovementRequestWithMalformedMrn).futureValue mustBe None
    }

    "must return None when the CustomsSubPlace is not defined" in {
      val application                             = applicationBuilder(Some(emptyUserAnswers)).build()
      val arrivalMovementRequestConversionService = application.injector.instanceOf[ArrivalMovementRequestConversionService]

      val arrivalMovementRequest = {
        val sample = arbitrary[ArrivalMovementRequest].sample.value
        sample.copy(header = sample.header.copy(customsSubPlace = None))
      }

      arrivalMovementRequestConversionService.convertToArrivalNotification(arrivalMovementRequest).futureValue mustBe None
    }

    "must convert ArrivalMovementRequest to NormalNotification for trader" in {

      val genCountry: Country           = arbitrary[Country].sample.value
      val genArrivalNotificationRequest = arbitrary[ArrivalMovementRequest].sample.value

      when(mockReferenceDataConnector.getCountry(any())(any(), any()))
        .thenReturn(Future.successful(genCountry))

      val application = applicationBuilder(Some(emptyUserAnswers))
        .overrides(bind[ReferenceDataConnector].toInstance(mockReferenceDataConnector))
        .build()

      val arrivalMovementRequestConversionService = application.injector.instanceOf[ArrivalMovementRequestConversionService]

      arrivalMovementRequestConversionService.convertToArrivalNotification(genArrivalNotificationRequest).futureValue.value mustBe an[NormalNotification]
    }

    "must fail when call to reference data is unsuccessful" in {

      val genArrivalNotificationRequest           = arbitrary[ArrivalMovementRequest].sample.value
      val arrivalMovementRequestConversionService = applicationBuilder().injector.instanceOf[ArrivalMovementRequestConversionService]
      val result                                  = arrivalMovementRequestConversionService.convertToArrivalNotification(genArrivalNotificationRequest)

      whenReady(result.failed) {
        _ mustBe an[Exception]
      }
    }
  }

}
