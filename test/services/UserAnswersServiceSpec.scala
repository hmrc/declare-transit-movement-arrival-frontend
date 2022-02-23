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

import base.{AppWithDefaultMockFixtures, SpecBase}
import connectors.ReferenceDataConnector
import generators.MessagesModelGenerators
import models.messages.ArrivalMovementRequest
import models.reference.CustomsOffice
import models.{ArrivalId, EoriNumber, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalacheck.Arbitrary.arbitrary
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class UserAnswersServiceSpec extends SpecBase with AppWithDefaultMockFixtures with MessagesModelGenerators {

  val mockArrivalNotificationMessageService = mock[ArrivalNotificationMessageService]
  val mockReferenceDataConnector            = mock[ReferenceDataConnector]

  implicit private val hc: HeaderCarrier = HeaderCarrier()

  override def beforeEach: Unit = {
    super.beforeEach()
    reset(mockArrivalNotificationMessageService)
    reset(mockReferenceDataConnector)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[ArrivalNotificationMessageService].toInstance(mockArrivalNotificationMessageService),
        bind[ReferenceDataConnector].toInstance(mockReferenceDataConnector)
      )

  "UserAnswers" - {
    "must return user answers for valid input" in {

      val arrivalMovementRequest = arbitrary[ArrivalMovementRequest].sample.value
      val customsOffice          = arbitrary[CustomsOffice].sample.value.copy(id = arrivalMovementRequest.customsOfficeOfPresentation.office)

      when(mockArrivalNotificationMessageService.getArrivalNotificationMessage(any())(any(), any()))
        .thenReturn(Future.successful(Some(arrivalMovementRequest)))

      when(mockReferenceDataConnector.getCustomsOffices()(any(), any()))
        .thenReturn(Future.successful(Seq(customsOffice)))

      setExistingUserAnswers(emptyUserAnswers)

      val userAnswersService = app.injector.instanceOf[UserAnswersService]
      userAnswersService.getUserAnswers(ArrivalId(1), eoriNumber).futureValue.value mustBe a[UserAnswers]
    }

    "must return None when getArrivalNotificationMessage cannot get a ArrivalMovementRequest" in {
      val customsOffice = arbitrary[CustomsOffice].sample.value

      when(mockArrivalNotificationMessageService.getArrivalNotificationMessage(any())(any(), any()))
        .thenReturn(Future.successful(None))

      when(mockReferenceDataConnector.getCustomsOffices()(any(), any()))
        .thenReturn(Future.successful(Seq(customsOffice)))

      setExistingUserAnswers(emptyUserAnswers)

      val userAnswersService = app.injector.instanceOf[UserAnswersService]

      userAnswersService.getUserAnswers(ArrivalId(1), eoriNumber).futureValue mustBe None
    }

    "must return None when the CustomsOffice cannot be found" in {
      val arrivalMovementRequest = arbitrary[ArrivalMovementRequest].sample.value

      val customsOffice = arbitrary[CustomsOffice]
        .suchThat(
          _.id != arrivalMovementRequest.customsOfficeOfPresentation.office
        )
        .sample
        .value

      when(mockArrivalNotificationMessageService.getArrivalNotificationMessage(any())(any(), any()))
        .thenReturn(Future.successful(Some(arrivalMovementRequest)))

      when(mockReferenceDataConnector.getCustomsOffices()(any(), any()))
        .thenReturn(Future.successful(Seq(customsOffice)))

      setExistingUserAnswers(emptyUserAnswers)

      val userAnswersService = app.injector.instanceOf[UserAnswersService]
      userAnswersService.getUserAnswers(ArrivalId(1), eoriNumber).futureValue mustBe None
    }

    "must return existing UserAnswers from repository if condition matches" in {

      setExistingUserAnswers(emptyUserAnswers)

      val eoriNumber          = EoriNumber("123")
      val existingUserAnswers = emptyUserAnswers.copy(eoriNumber = eoriNumber)
      when(mockSessionRepository.get(any(), any())) thenReturn Future.successful(Some(existingUserAnswers))

      val userAnswersService = app.injector.instanceOf[UserAnswersService]
      userAnswersService.getOrCreateUserAnswers(eoriNumber, mrn).futureValue mustBe existingUserAnswers
    }

    "must return basic UserAnswers with eori numbar and mrn if there is no record exist" in {

      setExistingUserAnswers(emptyUserAnswers)

      when(mockSessionRepository.get(any(), any())) thenReturn Future.successful(None)

      val userAnswersService  = app.injector.instanceOf[UserAnswersService]
      val result: UserAnswers = userAnswersService.getOrCreateUserAnswers(eoriNumber, mrn).futureValue

      result.eoriNumber mustBe emptyUserAnswers.eoriNumber
      result.movementReferenceNumber mustBe mrn
    }
  }
}
