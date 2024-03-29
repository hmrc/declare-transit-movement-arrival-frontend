/*
 * Copyright 2023 HM Revenue & Customs
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

import models.messages.InterchangeControlReference
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import services.mocks.MockDateTimeService
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import scala.concurrent.ExecutionContext.Implicits.global

class InterchangeControlReferenceIdRepositorySpec
    extends AnyFreeSpec
    with Matchers
    with ScalaFutures
    with BeforeAndAfterEach
    with GuiceOneAppPerSuite
    with IntegrationPatience
    with DefaultPlayMongoRepositorySupport[InterchangeControlReference]
    with MockDateTimeService {

  override protected val repository: InterchangeControlReferenceIdRepository =
    new InterchangeControlReferenceIdRepository(mongoComponent, mockTimeService)

  "InterchangeControlReferenceIdRepository" - {

    "must generate correct InterchangeControlReference when no record exists within the database" in {

      mockDateFormatted("20190101")

      val first = repository.nextInterchangeControlReferenceId().futureValue

      first mustBe InterchangeControlReference("20190101", 1)

      val second = repository.nextInterchangeControlReferenceId().futureValue

      second mustBe InterchangeControlReference("20190101", 2)
    }

    "must generate correct InterchangeControlReference when the collection already has a document in the database" in {

      mockDateFormatted("20190101")

      insert(InterchangeControlReference(mockTimeService.dateFormatted, 1)).futureValue

      val first  = repository.nextInterchangeControlReferenceId().futureValue
      val second = repository.nextInterchangeControlReferenceId().futureValue

      first.index mustEqual 2
      second.index mustEqual 3
    }
  }
}
