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
import connectors.ArrivalMovementConnector
import generators.MessagesModelGenerators
import models.XMLWrites._
import models.messages.{ArrivalMovementRequest, InterchangeControlReference, NormalNotification, TraderWithoutEori}
import models.{ArrivalId, MovementReferenceNumber}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.mockito.MockitoSugar
import play.api.Configuration
import play.api.http.Status._
import play.api.inject.bind
import repositories.InterchangeControlReferenceIdRepository
import services.conversion.ArrivalNotificationConversionService
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.Future

class ArrivalNotificationServiceSpec extends SpecBase with MockitoSugar with MessagesModelGenerators {

  private val mockConverterService                  = mock[ArrivalNotificationConversionService]
  private val mockArrivalMovementConnector          = mock[ArrivalMovementConnector]
  private val mockInterchangeControllerReference    = mock[InterchangeControlReferenceIdRepository]
  private val mockArrivalNotificationMessageService = mock[ArrivalNotificationMessageService]

  private val traderWithoutEori  = TraderWithoutEori("", "", "", "", "")
  private val normalNotification = NormalNotification(mrn, "", LocalDate.now(), None, traderWithoutEori, "", "", None)

  private val userEoriNumber = arbitrary[String].sample.value

  "ArrivalNotificationService" - {

    "submit" - {

      "must return None on submission of invalid data" in {
        when(mockConverterService.convertToArrivalNotification(any()))
          .thenReturn(None)

        val application = applicationBuilder(Some(emptyUserAnswers))
          .configure(Configuration("microservice.services.destination.xmlEndpoint" -> false))
          .overrides(bind[ArrivalNotificationConversionService].toInstance(mockConverterService))
          .build()

        val arrivalNotificationService = application.injector.instanceOf[ArrivalNotificationService]

        arrivalNotificationService.submit(emptyUserAnswers, userEoriNumber).futureValue mustBe None
      }

      "must submit data for valid xml input" in {

        when(mockInterchangeControllerReference.nextInterchangeControlReferenceId())
          .thenReturn(Future.successful(InterchangeControlReference("date", 0)))

        when(mockConverterService.convertToArrivalNotification(any()))
          .thenReturn(Some(normalNotification))

        when(mockArrivalMovementConnector.submitArrivalMovement(any())(any()))
          .thenReturn(Future.successful(HttpResponse(ACCEPTED)))

        val application = applicationBuilder(Some(emptyUserAnswers))
          .configure(Configuration("microservice.services.destination.xmlEndpoint" -> true))
          .overrides(bind[InterchangeControlReferenceIdRepository].toInstance(mockInterchangeControllerReference))
          .overrides(bind[ArrivalNotificationConversionService].toInstance(mockConverterService))
          .overrides(bind[ArrivalMovementConnector].toInstance(mockArrivalMovementConnector))
          .build()

        val arrivalNotificationService = application.injector.instanceOf[ArrivalNotificationService]

        val response = arrivalNotificationService.submit(emptyUserAnswers, userEoriNumber).futureValue.get
        response.status mustBe ACCEPTED
      }

      "must return None if interchangeControlReferenceIdRepository fails" in {

        when(mockInterchangeControllerReference.nextInterchangeControlReferenceId())
          .thenReturn(Future.failed(new Exception("failed to get nextInterchange reference id")))

        when(mockConverterService.convertToArrivalNotification(any()))
          .thenReturn(Some(normalNotification))

        val application = applicationBuilder(Some(emptyUserAnswers))
          .configure(Configuration("microservice.services.destination.xmlEndpoint" -> true))
          .overrides(bind[InterchangeControlReferenceIdRepository].toInstance(mockInterchangeControllerReference))
          .overrides(bind[ArrivalNotificationConversionService].toInstance(mockConverterService))
          .overrides(bind[ArrivalMovementConnector].toInstance(mockArrivalMovementConnector))
          .build()

        val arrivalNotificationService = application.injector.instanceOf[ArrivalNotificationService]

        arrivalNotificationService.submit(emptyUserAnswers, userEoriNumber).futureValue mustBe None

      }
    }

    "update" - {
      "must return Accepted status for successful updation of arrivalMovement" in {
        val arrivalId = ArrivalId(1)

        val arrivalMovementRequest: ArrivalMovementRequest = arbitrary[ArrivalMovementRequest].sample.value

        when(mockArrivalNotificationMessageService.getArrivalNotificationMessage(any())(any(), any()))
          .thenReturn(
            Future.successful(Some((arrivalMovementRequest.toXml, MovementReferenceNumber(arrivalMovementRequest.header.movementReferenceNumber).get))))

        val application = applicationBuilder(Some(emptyUserAnswers))
          .overrides(bind[ArrivalNotificationMessageService].toInstance(mockArrivalNotificationMessageService))
          .build()

        val arrivalNotificationService = application.injector.instanceOf[ArrivalNotificationService]
        val response                   = arrivalNotificationService.update(arrivalId).futureValue
        response.status mustBe ACCEPTED
      }

    }
  }
}
