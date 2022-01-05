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

import play.api.data.Form
import uk.gov.hmrc.viewmodels._

sealed trait TranshipmentType

object TranshipmentType extends Enumerable.Implicits {

  case object DifferentContainer extends WithName("differentContainer") with TranshipmentType
  case object DifferentVehicle extends WithName("differentVehicle") with TranshipmentType
  case object DifferentContainerAndVehicle extends WithName("differentContainerAndVehicle") with TranshipmentType

  val values: Seq[TranshipmentType] = Seq(
    DifferentContainer,
    DifferentVehicle,
    DifferentContainerAndVehicle
  )

  def radios(form: Form[_]): Seq[Radios.Item] = {

    val field = form("value")
    val items = values
      .map(_.toString)
      .map(
        optionName => (msg"transhipmentType.$optionName", optionName)
      )
      .map(Radios.Radio.tupled)

    Radios(field, items)
  }

  implicit val enumerable: Enumerable[TranshipmentType] =
    Enumerable(
      values.map(
        v => v.toString -> v
      ): _*
    )
}
