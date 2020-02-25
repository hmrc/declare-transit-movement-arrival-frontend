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
import models.messages.{NormalNotification, TraderWithoutEori}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status._
import play.api.inject.bind
import services.conversion.ArrivalNotificationConversionService
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ArrivalNotificationServiceSpec extends SpecBase with MockitoSugar {

  private val mockConverterService     = mock[ArrivalNotificationConversionService]
  private val mockDestinationConnector = mock[DestinationConnector]

  "ArrivalNotificationService" - {
    "must submit data for valid input " in {
      val traderWithoutEori  = TraderWithoutEori("", "", "", "", "")
      val normalNotification = NormalNotification(mrn.toString, "", LocalDate.now(), None, traderWithoutEori, "", "", None)

      when(mockConverterService.convertToArrivalNotification(any()))
        .thenReturn(Some(normalNotification))
      when(mockDestinationConnector.submitArrivalNotification(any())(any(), any()))
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
  }
}
