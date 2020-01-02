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

package controllers.actions

import generators.Generators
import models.requests.IdentifierRequest
import models.requests.OptionalDataRequest
import models.MovementReferenceNumber
import models.UserAnswers
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.FreeSpec
import org.scalatest.MustMatchers
import org.scalatest.OptionValues
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.AnyContent
import play.api.mvc.Request
import play.api.mvc.Results
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository

import scala.concurrent.Future

class DataRetrievalActionSpec
    extends FreeSpec
    with MustMatchers
    with GuiceOneAppPerSuite
    with ScalaFutures
    with MockitoSugar
    with Generators
    with OptionValues {

  val sessionRepository: SessionRepository = mock[SessionRepository]
  val mrn: MovementReferenceNumber         = arbitrary[MovementReferenceNumber].sample.value

  override lazy val app: Application = {

    import play.api.inject._

    new GuiceApplicationBuilder()
      .overrides(
        bind[SessionRepository].toInstance(sessionRepository)
      )
      .build()
  }

  def harness(mrn: MovementReferenceNumber, f: OptionalDataRequest[AnyContent] => Unit): Unit = {

    lazy val actionProvider = app.injector.instanceOf[DataRetrievalActionProviderImpl]

    actionProvider(mrn)
      .invokeBlock(
        IdentifierRequest(FakeRequest(GET, "/").asInstanceOf[Request[AnyContent]], ""), {
          request: OptionalDataRequest[AnyContent] =>
            f(request)
            Future.successful(Results.Ok)
        }
      )
      .futureValue
  }

  "a data retrieval action" - {

    "must return an OptionalDataRequest with an empty UserAnswers" - {

      "where there are no existing answers for this MRN" in {

        when(sessionRepository.get(any())) thenReturn Future.successful(None)

        harness(mrn, {
          request =>
            request.userAnswers must not be defined
        })
      }
    }

    "must return an OptionalDataRequest with some defined UserAnswers" - {

      "when there are existing answers for this MRN" in {

        when(sessionRepository.get(any())) thenReturn Future.successful(Some(UserAnswers(mrn)))

        harness(mrn, {
          request =>
            request.userAnswers mustBe defined
        })
      }
    }
  }
}
