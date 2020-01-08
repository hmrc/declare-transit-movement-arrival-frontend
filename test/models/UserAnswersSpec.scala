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

package models

import base.SpecBase
import pages.QuestionPage
import play.api.libs.json.{JsPath, Json}

import scala.util.Try

class UserAnswersSpec extends SpecBase {

  case object TestPage extends QuestionPage[String] {
    override def path: JsPath = JsPath \ "testPath"
    override def cleanup(value: Option[String], userAnswers: UserAnswers): Try[UserAnswers] =
      value match {
        case Some("1") => userAnswers.remove(CleanupPage)
        case _         => super.cleanup(value, userAnswers)
      }
  }

  case object CleanupPage extends QuestionPage[String] {
    override def path: JsPath = JsPath \ "testCleanupPath"
  }

  "UserAnswers" - {

    "must update UserAnswers when set" in {

      val userAnswers = UserAnswers(mrn)

      val data = {
        Json.obj(
          "testPath" -> "test"
        )
      }

      val result: UserAnswers = userAnswers.set(TestPage, "test").success.value

      result mustBe UserAnswers(mrn, data, result.lastUpdated)
    }

    "must remove CleanupPath when testPage is set to 1" in {

      val userAnswers = UserAnswers(mrn).set(CleanupPage, "testCleanupResult").success.value

      val data = {
        Json.obj(
          "testPath" -> "1"
        )
      }

      val result: UserAnswers = userAnswers.set(TestPage, "1").success.value

      result mustBe UserAnswers(mrn, data, result.lastUpdated)
    }

    "must not remove CleanupPath when testPage is the same answer as before" in {

      val userAnswers = UserAnswers(mrn)
        .set(TestPage, "1")
        .success
        .value
        .set(CleanupPage, "testCleanupResult")
        .success
        .value

      val data = {
        Json.obj(
          "testPath"        -> "1",
          "testCleanupPath" -> "testCleanupResult"
        )
      }

      val result: UserAnswers = userAnswers.set(TestPage, "1").success.value

      result mustBe UserAnswers(mrn, data, result.lastUpdated)
    }

  }

}
