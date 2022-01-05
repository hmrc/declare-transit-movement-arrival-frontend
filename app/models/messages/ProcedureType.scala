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

package models.messages

import play.api.libs.json._

sealed trait ProcedureType

object ProcedureType {

  case object Normal extends ProcedureType {
    val typeString: String        = "normal"
    override def toString: String = typeString
  }

  case object Simplified extends ProcedureType {
    val typeString: String        = "simplified"
    override def toString: String = typeString
  }

  implicit lazy val reads: Reads[ProcedureType] = Reads {
    case JsString(Normal.typeString)     => JsSuccess(Normal)
    case JsString(Simplified.typeString) => JsSuccess(Simplified)
    case _                               => JsError("Unknown procedure type")
  }

  implicit def writes[T <: ProcedureType]: Writes[T] = Writes {
    procedureType => JsString(procedureType.toString)
  }
}
