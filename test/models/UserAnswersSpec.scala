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

package models

import base.SpecBase
import pages.QuestionPage
import play.api.libs.json.{JsPath, Json}

import scala.util.Try

class UserAnswersSpec extends SpecBase {

  private val testPageAnswer = "1"
  private val testPagePath   = "testPath"

  private val testCleanupPagePath   = "testCleanupPagePath"
  private val testCleanupPageAnswer = "testCleanupPageAnswer"

  final case object TestPage extends QuestionPage[String] {
    override def path: JsPath = JsPath \ testPagePath

    override def cleanup(value: Option[String], userAnswers: UserAnswers): Try[UserAnswers] =
      value match {
        case Some("1") => userAnswers.remove(TestCleanupPage)
        case _         => super.cleanup(value, userAnswers)
      }
  }

  final case object TestCleanupPage extends QuestionPage[String] {
    override def path: JsPath = JsPath \ testCleanupPagePath
  }

  "UserAnswers" - {

    s"must run cleanup when the new answer is not equal to the existing answer" in {

      val userAnswers = emptyUserAnswers.set(TestCleanupPage, testCleanupPageAnswer).success.value
      val result      = userAnswers.set(TestPage, testPageAnswer).success.value
      val data =
        Json.obj(
          testPagePath -> testPageAnswer
        )

      result mustBe UserAnswers(mrn, eoriNumber, data, result.lastUpdated, id = emptyUserAnswers.id)
    }

    s"must not run cleanup when the new answer is equal to the existing answer" in {

      val userAnswers = emptyUserAnswers
        .set(TestPage, testPageAnswer)
        .success
        .value
        .set(TestCleanupPage, testCleanupPageAnswer)
        .success
        .value

      val result = userAnswers.set(TestPage, testPageAnswer).success.value
      val data =
        Json.obj(
          testPagePath        -> testPageAnswer,
          testCleanupPagePath -> testCleanupPageAnswer
        )

      result mustBe UserAnswers(mrn, eoriNumber, data, result.lastUpdated, id = emptyUserAnswers.id)
    }
  }

}
