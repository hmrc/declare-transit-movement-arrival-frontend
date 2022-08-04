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

package repositories

import config.FrontendAppConfig
import models.{EoriNumber, UserAnswers}
import org.mongodb.scala.model.Indexes.{ascending, compoundIndex}
import org.mongodb.scala.model.{Filters, FindOneAndUpdateOptions, IndexModel, IndexOptions, Indexes, ReplaceOptions, Updates}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SessionRepository @Inject() (
  mongoComponent: MongoComponent,
  appConfig: FrontendAppConfig
)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[UserAnswers](
      mongoComponent = mongoComponent,
      collectionName = "user-answers",
      domainFormat = UserAnswers.format,
      indexes = SessionRepository.indexes(appConfig)
    ) {

  def get(movementReferenceNumber: String, eoriNumber: EoriNumber): Future[Option[UserAnswers]] = {
    val filter = Filters.and(
      Filters.eq("movementReferenceNumber", movementReferenceNumber),
      Filters.eq("eoriNumber", eoriNumber.value)
    )
    val update = Updates.set("lastUpdated", LocalDateTime.now())

    collection
      .findOneAndUpdate(filter, update, FindOneAndUpdateOptions().upsert(false))
      .toFutureOption()
  }

  def set(userAnswers: UserAnswers): Future[Boolean] = {
    val filter = Filters.and(
      Filters.eq("movementReferenceNumber", userAnswers.movementReferenceNumber.toString),
      Filters.eq("eoriNumber", userAnswers.eoriNumber.value)
    )
    val updatedUserAnswers = userAnswers.copy(lastUpdated = LocalDateTime.now())

    collection
      .replaceOne(filter, updatedUserAnswers, ReplaceOptions().upsert(true))
      .toFuture()
      .map(_.wasAcknowledged())
  }

  def remove(movementReferenceNumber: String, eoriNumber: EoriNumber): Future[Boolean] = {
    val filter = Filters.and(
      Filters.eq("movementReferenceNumber", movementReferenceNumber),
      Filters.eq("eoriNumber", eoriNumber.value)
    )

    collection
      .deleteOne(filter)
      .toFuture()
      .map(_.wasAcknowledged())
  }
}

object SessionRepository {

  def indexes(appConfig: FrontendAppConfig): Seq[IndexModel] = {
    val userAnswersLastUpdatedIndex: IndexModel = IndexModel(
      keys = Indexes.ascending("lastUpdated"),
      indexOptions = IndexOptions().name("user-answers-last-updated-index").expireAfter(appConfig.cacheTtl, TimeUnit.SECONDS)
    )

    val eoriNumberAndMrnCompoundIndex: IndexModel = IndexModel(
      keys = compoundIndex(ascending("eoriNumber"), ascending("movementReferenceNumber")),
      indexOptions = IndexOptions().name("eoriNumber-mrn-index")
    )

    Seq(userAnswersLastUpdatedIndex, eoriNumberAndMrnCompoundIndex)
  }

}
