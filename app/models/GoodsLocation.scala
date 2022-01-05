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

sealed trait GoodsLocation

object GoodsLocation extends Enumerable.Implicits {

  case object BorderForceOffice extends WithName("borderForceOffice") with GoodsLocation

  case object AuthorisedConsigneesLocation extends WithName("authorisedConsigneesLocation") with GoodsLocation

  val values: Seq[GoodsLocation] = Seq(
    BorderForceOffice,
    AuthorisedConsigneesLocation
  )

  def radios(form: Form[_]): Seq[Radios.Item] = {

    val field = form("value")
    val items = Seq(
      Radios.Radio(msg"goodsLocation.authorisedConsigneesLocation", AuthorisedConsigneesLocation.toString),
      Radios.Radio(msg"goodsLocation.borderForceOffice", BorderForceOffice.toString)
    )

    Radios(field, items)
  }

  implicit val enumerable: Enumerable[GoodsLocation] =
    Enumerable(
      values.map(
        v => v.toString -> v
      ): _*
    )
}
