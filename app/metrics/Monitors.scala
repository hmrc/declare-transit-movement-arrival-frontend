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

package metrics

import models.CountryList
import models.reference.CustomsOffice

trait RequestMonitor[A] {
  val name: String

  val path: String         = s"$name.request"
  val timer: Timer         = Timer(path)
  val callCounter: Counter = Counter(path)

  def completionCounter(result: A): Option[Counter] = None
  val failureCounter: Counter                       = Counter(s"$path.failed")
}

case class DefaultRequestMonitor[A](name: String) extends RequestMonitor[A]

object Monitors {
  val getCustomsOfficesMonitor: DefaultRequestMonitor[Seq[CustomsOffice]] = DefaultRequestMonitor[Seq[CustomsOffice]]("get-customs-offices")
  val getCountryListMonitor: DefaultRequestMonitor[CountryList]           = DefaultRequestMonitor[CountryList]("get-country-list")
}