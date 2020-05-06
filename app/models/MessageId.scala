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

import play.api.mvc.PathBindable

case class MessageId(value: Int)

object MessageId {
  implicit def pathBindable(implicit intBinder: PathBindable[Int]): PathBindable[MessageId] = new PathBindable[MessageId] {
    override def bind(key: String, value: String): Either[String, MessageId] =
      intBinder.bind(key, value) match {
        case Right(id) if id > 0 => Right(MessageId(id))
        case _                   => Left("invalid Message Id")
      }

    override def unbind(key: String, value: MessageId): String =
      intBinder.unbind(key, value.value)
  }
}
