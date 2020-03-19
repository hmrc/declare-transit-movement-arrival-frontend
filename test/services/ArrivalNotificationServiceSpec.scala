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

import java.time.LocalDate

import base.SpecBase
import connectors.DestinationConnector
import models.messages.{InterchangeControlReference, NormalNotification, TraderWithoutEori}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status._
import play.api.inject.bind
import repositories.InterchangeControlReferenceIdRepository
import services.conversion.ArrivalNotificationConversionService
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.Future
import scala.xml.Node

class ArrivalNotificationServiceSpec extends SpecBase with MockitoSugar {

  private val mockConverterService               = mock[ArrivalNotificationConversionService]
  private val mockDestinationConnector           = mock[DestinationConnector]
  private val mockInterchangeControllerReference = mock[InterchangeControlReferenceIdRepository]

  private val traderWithoutEori  = TraderWithoutEori("", "", "", "", "")
  private val normalNotification = NormalNotification(mrn, "", LocalDate.now(), None, traderWithoutEori, "", None)

  "ArrivalNotificationService" - {
    "must submit data for valid input " in {

      when(mockConverterService.convertToArrivalNotification(any()))
        .thenReturn(Some(normalNotification))
      when(mockDestinationConnector.submitArrivalNotification(any())(any()))
        .thenReturn(Future.successful(HttpResponse(OK)))

      val application = applicationBuilder(Some(emptyUserAnswers))
        .overrides(
          bind[ArrivalNotificationConversionService].toInstance(mockConverterService),
          bind[DestinationConnector].toInstance(mockDestinationConnector)
        )
        .build()

      val arrivalNotificationService = application.injector.instanceOf[ArrivalNotificationService]

      val response = arrivalNotificationService.submit(emptyUserAnswers).futureValue.get
      response.status mustBe OK
    }

    "must return None on submission of invalid data" in {
      when(mockConverterService.convertToArrivalNotification(any()))
        .thenReturn(None)

      val application = applicationBuilder(Some(emptyUserAnswers))
        .overrides(bind[ArrivalNotificationConversionService].toInstance(mockConverterService))
        .build()

      val arrivalNotificationService = application.injector.instanceOf[ArrivalNotificationService]

      arrivalNotificationService.submit(emptyUserAnswers).futureValue mustBe None
    }

    "generateXml" - {

      "must create an xml on a future success" in {

        when(mockInterchangeControllerReference.nextInterchangeControlReferenceId())
          .thenReturn(Future.successful(InterchangeControlReference("date", 0)))

        when(mockConverterService.convertToArrivalNotification(any()))
          .thenReturn(Some(normalNotification))

        val application = applicationBuilder(Some(emptyUserAnswers))
          .overrides(bind[InterchangeControlReferenceIdRepository].toInstance(mockInterchangeControllerReference))
          .overrides(bind[ArrivalNotificationConversionService].toInstance(mockConverterService))
          .build()

        val arrivalNotificationService = application.injector.instanceOf[ArrivalNotificationService]

        arrivalNotificationService.generateXml(normalNotification).futureValue mustBe an[Node]
      }

      "must return a future failed if interchangeControlReferenceIdRepository fails" in {

        when(mockInterchangeControllerReference.nextInterchangeControlReferenceId())
          .thenReturn(Future.failed(new Exception))

        when(mockConverterService.convertToArrivalNotification(any()))
          .thenReturn(Some(normalNotification))

        val application = applicationBuilder(Some(emptyUserAnswers))
          .overrides(bind[InterchangeControlReferenceIdRepository].toInstance(mockInterchangeControllerReference))
          .overrides(bind[ArrivalNotificationConversionService].toInstance(mockConverterService))
          .build()

        val arrivalNotificationService = application.injector.instanceOf[ArrivalNotificationService]

        val result = arrivalNotificationService.generateXml(normalNotification)

        whenReady(result.failed) {
          _ mustBe an[Exception]
        }

      }
    }
  }
}
