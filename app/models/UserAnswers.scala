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

package models

import derivable.Derivable
import pages._
import play.api.libs.json._
import queries.Gettable

import java.time.{Instant, LocalDateTime, ZoneOffset}
import scala.util.{Failure, Success, Try}

final case class UserAnswers(
  movementReferenceNumber: MovementReferenceNumber,
  eoriNumber: EoriNumber,
  data: JsObject = Json.obj(),
  lastUpdated: LocalDateTime = LocalDateTime.now,
  arrivalId: Option[ArrivalId] = None,
  id: Id = Id()
) {

  def get[A](gettable: Gettable[A])(implicit rds: Reads[A]): Option[A] =
    Reads.optionNoError(Reads.at(gettable.path)).reads(data).getOrElse(None)

  def get[A, B](derivable: Derivable[A, B])(implicit rds: Reads[A]): Option[B] =
    get(derivable: Gettable[A]).map(derivable.derive)

  def set[A](page: QuestionPage[A], value: A)(implicit writes: Writes[A]): Try[UserAnswers] =
    data.setObject(page.path, Json.toJson(value)) match {
      case JsSuccess(jsObject, _) =>
        val updatedAnswers = copy(data = jsObject)
        if (jsObject == data) {
          Success(updatedAnswers)
        } else {
          page.cleanup(Some(value), updatedAnswers)
        }
      case JsError(errors) =>
        Failure(JsResultException(errors))
    }

  def remove[A](page: QuestionPage[A]): Try[UserAnswers] = {

    val updatedData = data.removeObject(page.path) match {
      case JsSuccess(jsValue, _) =>
        Success(jsValue)
      case JsError(_) =>
        Success(data)
    }

    updatedData.flatMap {
      d =>
        val updatedAnswers = copy(data = d)
        page.cleanup(None, updatedAnswers)
    }
  }
}

object UserAnswers {

  import play.api.libs.functional.syntax._

  implicit lazy val reads: Reads[UserAnswers] = {
    implicit val localDateTimeReader: Reads[LocalDateTime] = {
      val reactiveMongoReads = (__ \ "$date").read[Long].map {
        millis =>
          LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneOffset.UTC)
      }
      val hmrcMongoReads = localDateTimeReads
      hmrcMongoReads orElse reactiveMongoReads
    }

    (
      (__ \ "movementReferenceNumber").read[MovementReferenceNumber] and
        (__ \ "eoriNumber").read[EoriNumber] and
        (__ \ "data").read[JsObject] and
        (__ \ "lastUpdated").read[LocalDateTime] and
        (__ \ "arrivalId").readNullable[ArrivalId] and
        (__ \ "_id").read[Id]
    )(UserAnswers.apply _)
  }

  implicit lazy val writes: OWrites[UserAnswers] =
    (
      (__ \ "movementReferenceNumber").write[MovementReferenceNumber] and
        (__ \ "eoriNumber").write[EoriNumber] and
        (__ \ "data").write[JsObject] and
        (__ \ "lastUpdated").write(localDateTimeWrites) and
        (__ \ "arrivalId").writeNullable[ArrivalId] and
        (__ \ "_id").write[Id]
    )(unlift(UserAnswers.unapply))

  implicit lazy val format: Format[UserAnswers] = Format(reads, writes)

  //TODO: Change LocalDateTime to Instant and remove below methods
  private val localDateTimeReads: Reads[LocalDateTime] =
    Reads
      .at[String](__ \ "$date" \ "$numberLong")
      .map(
        dateTime => Instant.ofEpochMilli(dateTime.toLong).atZone(ZoneOffset.UTC).toLocalDateTime
      )

  private val localDateTimeWrites: Writes[LocalDateTime] =
    Writes
      .at[String](__ \ "$date" \ "$numberLong")
      .contramap(_.toInstant(ZoneOffset.UTC).toEpochMilli.toString)
}
