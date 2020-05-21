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

package controllers

import java.time.LocalDate

import base.SpecBase
import connectors.ArrivalMovementConnector
import matchers.JsonMatchers
import models.messages.{ArrivalNotificationRejectionMessage, ErrorPointer, ErrorType, FunctionalError}
import models.{ArrivalId, MessagesLocation, MessagesSummary}
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.Configuration
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import play.api.inject.bind

import scala.concurrent.Future

class ArrivalRejectionControllerSpec extends SpecBase with MockitoSugar with JsonMatchers with BeforeAndAfterEach {

  private val mockArrivalMovementConnector = mock[ArrivalMovementConnector]

  override def beforeEach: Unit = {
    super.beforeEach
    reset(mockArrivalMovementConnector)
  }

  private val arrivalId = ArrivalId(1)

  "ArrivalRejection Controller" - {

    "return OK and the correct view for a GET when 'arrivalRejection' feature toggle set to true" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val errors = Seq(FunctionalError(ErrorType(91), ErrorPointer("Duplicate MRN"), None, None))
      val messageActions =
        MessagesSummary(arrivalId,
                        MessagesLocation(s"/movements/arrivals/${arrivalId.value}/messages/3", Some(s"/movements/arrivals/${arrivalId.value}/messages/5")))

      when(mockArrivalMovementConnector.getSummary(any())(any()))
        .thenReturn(Future.successful(messageActions))

      when(mockArrivalMovementConnector.getRejectionMessage(any(), any())(any()))
        .thenReturn(Future.successful(ArrivalNotificationRejectionMessage(mrn.toString, LocalDate.now, None, None, errors)))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .configure(Configuration("feature-toggles.arrivalRejection" -> true))
        .overrides(
          bind[ArrivalMovementConnector].toInstance(mockArrivalMovementConnector)
        )
        .build()
      val request        = FakeRequest(GET, routes.ArrivalRejectionController.onPageLoad(arrivalId).url)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(application, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())
      verify(mockArrivalMovementConnector, times(1)).getSummary(eqTo(arrivalId))(any())
      verify(mockArrivalMovementConnector, times(1)).getRejectionMessage(eqTo(arrivalId), eqTo(s"/movements/arrivals/${arrivalId.value}/messages/5"))(any())

      val expectedJson = Json.obj("mrn" -> mrn, "errors" -> errors)

      templateCaptor.getValue mustEqual "arrivalRejection.njk"
      jsonCaptor.getValue must containJson(expectedJson)

      application.stop()
    }

    "redirect to Unauthorised page when 'arrivalRejection' feature toggle set to false" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .configure(Configuration("feature-toggles.arrivalRejection" -> false))
        .build()
      val request = FakeRequest(GET, routes.ArrivalRejectionController.onPageLoad(arrivalId).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      application.stop()
    }
  }
}
