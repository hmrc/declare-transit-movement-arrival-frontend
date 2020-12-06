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

import base.{AppWithDefaultMockFixtures, SpecBase}
import config.FrontendAppConfig
import matchers.JsonMatchers
import models.ArrivalId
import models.messages.ErrorType.{DuplicateMrn, IncorrectValue, InvalidMrn, UnknownMrn}
import models.messages.{ArrivalNotificationRejectionMessage, ErrorPointer, ErrorType, FunctionalError}
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.Configuration
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import services.ArrivalRejectionService

import scala.concurrent.Future

class ArrivalRejectionControllerSpec extends SpecBase with AppWithDefaultMockFixtures with JsonMatchers with BeforeAndAfterEach {

  private val mockArrivalRejectionService = mock[ArrivalRejectionService]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockArrivalRejectionService)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[ArrivalRejectionService].toInstance(mockArrivalRejectionService))

  private val arrivalId = ArrivalId(1)

  "ArrivalRejection Controller" - {

    Seq(
      (UnknownMrn, "Unknown MRN", "movementReferenceNumberRejection.error.unknown"),
      (DuplicateMrn, "duplicate MRN", "movementReferenceNumberRejection.error.duplicate"),
      (InvalidMrn, "Invalid MRN", "movementReferenceNumberRejection.error.invalid")
    ) foreach {
      case (errorType, errorPointer, errorKey) =>
        s"return OK and the correct $errorPointer Rejection view for a GET" in {

          setExistingUserAnswers(emptyUserAnswers)

          when(mockRenderer.render(any(), any())(any()))
            .thenReturn(Future.successful(Html("")))

          val errors = Seq(FunctionalError(errorType, ErrorPointer(errorPointer), None, None))

          when(mockArrivalRejectionService.arrivalRejectionMessage((any()))(any(), any()))
            .thenReturn(Future.successful(Some(ArrivalNotificationRejectionMessage(mrn.toString, LocalDate.now, None, None, errors))))

          val frontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
          val request           = FakeRequest(GET, routes.ArrivalRejectionController.onPageLoad(arrivalId).url)
          val templateCaptor    = ArgumentCaptor.forClass(classOf[String])
          val jsonCaptor        = ArgumentCaptor.forClass(classOf[JsObject])

          val result = route(app, request).value

          status(result) mustEqual OK

          verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())
          verify(mockArrivalRejectionService, times(1)).arrivalRejectionMessage(eqTo(arrivalId))(any(), any())

          val expectedJson = Json.obj(
            "mrn"                        -> mrn,
            "errorKey"                   -> errorKey,
            "contactUrl"                 -> frontendAppConfig.nctsEnquiriesUrl,
            "movementReferenceNumberUrl" -> routes.UpdateRejectedMRNController.onPageLoad(arrivalId).url
          )

          templateCaptor.getValue mustEqual "movementReferenceNumberRejection.njk"
          jsonCaptor.getValue must containJson(expectedJson)
        }
    }

    "return OK and the correct arrivalGeneralRejection view for a GET" in {

      setExistingUserAnswers(emptyUserAnswers)

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val errors = Seq(FunctionalError(IncorrectValue, ErrorPointer("TRD.TIN"), None, None))

      when(mockArrivalRejectionService.arrivalRejectionMessage(any())(any(), any()))
        .thenReturn(Future.successful(Some(ArrivalNotificationRejectionMessage(mrn.toString, LocalDate.now, None, None, errors))))

      val frontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
      val request           = FakeRequest(GET, routes.ArrivalRejectionController.onPageLoad(arrivalId).url)
      val templateCaptor    = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor        = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())
      verify(mockArrivalRejectionService, times(1)).arrivalRejectionMessage(eqTo(arrivalId))(any(), any())

      val expectedJson = Json.obj(
        "mrn"              -> mrn,
        "errors"           -> errors,
        "contactUrl"       -> frontendAppConfig.nctsEnquiriesUrl,
        "createArrivalUrl" -> routes.MovementReferenceNumberController.onPageLoad().url
      )

      templateCaptor.getValue mustEqual "arrivalGeneralRejection.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "redirect to 'Technical difficulties' page when arrival rejection message is malformed" in {

      setExistingUserAnswers(emptyUserAnswers)

      when(mockArrivalRejectionService.arrivalRejectionMessage(any())(any(), any()))
        .thenReturn(Future.successful(None))

      val request = FakeRequest(GET, routes.ArrivalRejectionController.onPageLoad(arrivalId).url)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      verify(mockArrivalRejectionService, times(1)).arrivalRejectionMessage(eqTo(arrivalId))(any(), any())
    }

  }

}
