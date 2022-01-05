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

package views

import generators.{Generators, MessagesModelGenerators}
import models.messages.FunctionalError
import org.jsoup.nodes.Element
import play.api.libs.json.Json

class ArrivalGeneralRejectionViewSpec extends SingleViewSpec("arrivalGeneralRejection.njk") with Generators with MessagesModelGenerators {

  val baseJson =
    Json.obj(
      "mrn"              -> mrn,
      "errors"           -> Seq.empty[FunctionalError],
      "contactUrl"       -> "enquiriesUrl",
      "createArrivalUrl" -> "createArrivalUrl"
    )

  "rows for functional errors" - {
    "must not display a row when there are no errors" in {
      val doc = renderDocument(baseJson).futureValue

      getByElementTestIdSelector(doc, "error-row-error-type") must be(empty)
    }

    "must display a row for each error with the error type and error pointer" in {
      forAll(listWithMaxLength[FunctionalError]()) {
        functionalError =>
          val json = baseJson ++ Json.obj(
            "errors" -> functionalError
          )

          val doc = renderDocument(json).futureValue

          val errorRows: Seq[Element] = getByElementTestIdSelector(doc, "error-row-error-type")
          errorRows.length mustEqual functionalError.length

          val errorTypes: Seq[Element] = getByElementTestIdSelector(doc, "error-row-error-type")
          errorTypes.length mustEqual functionalError.length

          val errorPointer: Seq[Element] = getByElementTestIdSelector(doc, "error-row-error-pointer")
          errorPointer.length mustEqual functionalError.length

          for {
            FunctionalError(errorType, pointer, _, _) <- functionalError
          } yield {
            errorTypes.find(_.text() == errorType.code.toString) must not be empty
            errorPointer.find(_.text() == pointer.value) must not be empty
          }
      }
    }
  }
}
