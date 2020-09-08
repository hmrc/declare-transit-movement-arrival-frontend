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

import java.util.UUID

import play.api.libs.json.{Format, JsError, JsObject, JsResult, JsString, JsValue, Json, OFormat}
import play.api.mvc.PathBindable

import scala.util.{Failure, Success, Try}
final case class ArrivalUniqueRef(uuid: UUID) {
  def value: UUID = uuid

  override def toString: String = value.toString
}

object ArrivalUniqueRef {

  def instance: ArrivalUniqueRef = ArrivalUniqueRef(UUID.randomUUID())

  implicit val jsonFormat: Format[ArrivalUniqueRef] = new Format[ArrivalUniqueRef] {
    override def writes(o: ArrivalUniqueRef): JsValue = JsString(o.uuid.toString)

    override def reads(json: JsValue): JsResult[ArrivalUniqueRef] = {
      json match {
        case xs @ JsString(_) => xs.validate[UUID].map(ArrivalUniqueRef.apply)
        case json => JsError(s"Expected type JsString containing single UUID toString value, got ${json.toString()} instead.")
      }
    }
  }

  implicit def pathBindable: PathBindable[ArrivalUniqueRef] = new PathBindable[ArrivalUniqueRef] {

    override def bind(key: String, value: String): Either[String, ArrivalUniqueRef] =
      Try(UUID.fromString(value)) match {
        case Failure(exception) => Left(s"Invalid UUID format for ArrivalUniqueRef: ${exception.getMessage}")
        case Success(value)     => Right(ArrivalUniqueRef(value))
      }

    override def unbind(key: String, value: ArrivalUniqueRef): String =
      value.uuid.toString
  }
}
