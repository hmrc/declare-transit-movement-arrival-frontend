/*
 * Copyright 2021 HM Revenue & Customs
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

package repositories

import models.{EoriNumber, MovementReferenceNumber, UserAnswers}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.Json
import reactivemongo.play.json.collection.JSONCollection
import services.mocks.MockDateTimeService

import scala.concurrent.ExecutionContext.Implicits.global

class SessionRepositorySpec extends AnyFreeSpec
  with Matchers
  with MongoSuite
  with ScalaFutures
  with BeforeAndAfterEach
  with GuiceOneAppPerSuite
  with IntegrationPatience
  with MockDateTimeService
  with OptionValues {

  private val service = app.injector.instanceOf[SessionRepository]

  private val userAnswer1 = UserAnswers(MovementReferenceNumber("99IT9876AB88901209").get, EoriNumber("EoriNumber1"), Json.obj("foo" -> "bar"))
  private val userAnswer2 = UserAnswers(MovementReferenceNumber("18GB0000601001EB15").get, EoriNumber("EoriNumber2"), Json.obj("bar" -> "foo"))

  override def beforeEach(): Unit = {
    super.beforeEach()
    database.flatMap {
      db =>
        val jsonCollection = db.collection[JSONCollection]("user-answers")

        jsonCollection
          .insert(ordered = false)
          .many(Seq(userAnswer1, userAnswer2))

    }.futureValue
  }

  override def afterEach(): Unit = {
    super.afterEach()
    database.flatMap(_.drop())
  }

  "SessionRepository" - {

    "get" - {

      "must return UserAnswers when given an MovementReferenceNumber and EoriNumber" in {

        val result = service.get("99IT9876AB88901209", EoriNumber("EoriNumber1")).futureValue

        result.value.id         mustBe userAnswer1.id
        result.value.eoriNumber mustBe userAnswer1.eoriNumber
        result.value.data       mustBe userAnswer1.data
      }

      "must return None when no UserAnswers match MovementReferenceNumber" in {

        val result = service.get("18GB0000601001EBD1", EoriNumber("EoriNumber1")).futureValue

        result mustBe None
      }

      "must return None when no UserAnswers match EoriNumber" in {

        val result = service.get("99IT9876AB88901209", EoriNumber("InvalidEori")).futureValue

        result mustBe None
      }
    }

    "set" - {

      "must create new document when given valid UserAnswers" in {

        val userAnswer = UserAnswers(MovementReferenceNumber("18GB0000601001EBD1").get, EoriNumber("EoriNumber3"), Json.obj("foo" -> "bar"))

        val setResult = service.set(userAnswer).futureValue

        val getResult = service.get("18GB0000601001EBD1", EoriNumber("EoriNumber3")).futureValue.value


        setResult            mustBe true
        getResult.id         mustBe userAnswer.id
        getResult.eoriNumber mustBe userAnswer.eoriNumber
        getResult.data       mustBe userAnswer.data
      }
    }

    "remove" - {

      "must remove document when given a valid MovementReferenceNumber and EoriNumber" in {

        service.get("99IT9876AB88901209", EoriNumber("EoriNumber1")).futureValue mustBe defined

        service.remove("99IT9876AB88901209", EoriNumber("EoriNumber1")).futureValue

        service.get("99IT9876AB88901209", EoriNumber("EoriNumber1")).futureValue must not be defined
      }
    }
  }

}