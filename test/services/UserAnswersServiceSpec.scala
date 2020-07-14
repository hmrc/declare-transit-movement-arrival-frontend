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

import base.SpecBase
import connectors.ReferenceDataConnector
import generators.MessagesModelGenerators
import models.messages.ArrivalMovementRequest
import models.reference.CustomsOffice
import models.{ArrivalId, EoriNumber, UserAnswers}
import org.mockito.Matchers.any
import org.mockito.Mockito._
import org.scalacheck.Arbitrary.arbitrary
import play.api.inject.bind
import repositories.SessionRepository

import scala.concurrent.Future

class UserAnswersServiceSpec extends SpecBase with MessagesModelGenerators {

  val mockArrivalNotificationMessageService = mock[ArrivalNotificationMessageService]
  val mockReferenceDataConnector            = mock[ReferenceDataConnector]
  val mockSessionRepository                 = mock[SessionRepository]

  override def beforeEach: Unit = {
    super.beforeEach()
    reset(mockArrivalNotificationMessageService)
  }

  "UserAnswers" - {
    "must return user answers for valid input" in {

      val arrivalMovementRequest = arbitrary[ArrivalMovementRequest].sample.value
      val customsOffice          = arbitrary[CustomsOffice].sample.value.copy(id = arrivalMovementRequest.customsOfficeOfPresentation.presentationOffice)

      when(mockArrivalNotificationMessageService.getArrivalNotificationMessage(any())(any(), any()))
        .thenReturn(Future.successful(Some(arrivalMovementRequest)))

      when(mockReferenceDataConnector.getCustomsOffices()(any(), any()))
        .thenReturn(Future.successful(Seq(customsOffice)))

      val application = applicationBuilder(Some(emptyUserAnswers))
        .overrides(bind[ArrivalNotificationMessageService].toInstance(mockArrivalNotificationMessageService))
        .overrides(bind[ReferenceDataConnector].toInstance(mockReferenceDataConnector))
        .build()

      val userAnswersService = application.injector.instanceOf[UserAnswersService]
      userAnswersService.getUserAnswers(ArrivalId(1), eoriNumber).futureValue.value mustBe a[UserAnswers]
    }

    "must return None when getArrivalNotificationMessage cannot get a ArrivalMovementRequest" in {
      when(mockArrivalNotificationMessageService.getArrivalNotificationMessage(any())(any(), any()))
        .thenReturn(Future.successful(None))

      val application = applicationBuilder(Some(emptyUserAnswers))
        .overrides(bind[ArrivalNotificationMessageService].toInstance(mockArrivalNotificationMessageService))
        .build()
      val userAnswersService = application.injector.instanceOf[UserAnswersService]
      userAnswersService.getUserAnswers(ArrivalId(1), eoriNumber).futureValue mustBe None
    }

    "must return None when the PresentationOffice cannot be found" in {
      val arrivalMovementRequest = arbitrary[ArrivalMovementRequest].sample.value

      val customsOffice = arbitrary[CustomsOffice]
        .suchThat(
          _.id != arrivalMovementRequest.customsOfficeOfPresentation.presentationOffice
        )
        .sample
        .value

      when(mockArrivalNotificationMessageService.getArrivalNotificationMessage(any())(any(), any()))
        .thenReturn(Future.successful(Some(arrivalMovementRequest)))

      when(mockReferenceDataConnector.getCustomsOffices()(any(), any()))
        .thenReturn(Future.successful(Seq(customsOffice)))

      val application = applicationBuilder(Some(emptyUserAnswers))
        .overrides(bind[ArrivalNotificationMessageService].toInstance(mockArrivalNotificationMessageService))
        .overrides(bind[ReferenceDataConnector].toInstance(mockReferenceDataConnector))
        .build()

      val userAnswersService = application.injector.instanceOf[UserAnswersService]
      userAnswersService.getUserAnswers(ArrivalId(1), eoriNumber).futureValue mustBe None
    }

    "must return existing UserAnswers from repository if condition matches" in {

      val application = applicationBuilder()
        .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .build()
      val eoriNumber          = EoriNumber("123")
      val existingUserAnswers = emptyUserAnswers.copy(eoriNumber = eoriNumber)
      when(mockSessionRepository.get(any(), any())) thenReturn Future.successful(Some(existingUserAnswers))

      val userAnswersService = application.injector.instanceOf[UserAnswersService]
      userAnswersService.getOrCreateUserAnswers(eoriNumber, mrn).futureValue mustBe existingUserAnswers
    }

    "must return basic UserAnswers with eori numbar and mrn if there is no record exist" in {

      val application = applicationBuilder()
        .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .build()

      when(mockSessionRepository.get(any(), any())) thenReturn Future.successful(None)

      val userAnswersService  = application.injector.instanceOf[UserAnswersService]
      val result: UserAnswers = userAnswersService.getOrCreateUserAnswers(eoriNumber, mrn).futureValue

      result.eoriNumber mustBe emptyUserAnswers.eoriNumber
      result.id mustBe mrn

    }
  }
}
