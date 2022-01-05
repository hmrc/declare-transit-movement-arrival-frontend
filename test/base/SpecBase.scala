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

package base

import models.domain.{ContainerDomain, SealDomain}
import models.messages.{Container, Seal}
import models.{Address, EoriNumber, Index, MovementReferenceNumber, UserAnswers}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, TryValues}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsEmpty
import play.api.test.{FakeRequest, Helpers}

trait SpecBase
    extends AnyFreeSpec
    with Matchers
    with ScalaCheckPropertyChecks
    with OptionValues
    with TryValues
    with ScalaFutures
    with IntegrationPatience
    with MockitoSugar {

  val eoriNumber: EoriNumber       = EoriNumber("GB123456")
  val mrn: MovementReferenceNumber = MovementReferenceNumber("19", "GB", "1234567890123")

  val emptyUserAnswers: UserAnswers = UserAnswers(mrn, eoriNumber, Json.obj())

  val eventIndex: Index     = Index(0)
  val containerIndex: Index = Index(0)
  val sealIndex: Index      = Index(0)

  val seal: Seal                       = Seal("sealNumber")
  val sealDomain: SealDomain           = SealDomain("sealNumber")
  val container: Container             = Container("containerNumber")
  val domainContainer: ContainerDomain = ContainerDomain("containerNumber")

  val traderName: String    = "traderName"
  val consigneeName: String = "consigneeName"
  val customsOffice: String = "customsOffice"

  val traderAddress: Address    = Address("buildingAndStreet", "city", "NE99 1XN")
  val consigneeAddress: Address = Address("buildingAndStreet", "city", "NE99 1XN")
  val configKey                 = "config"

  def fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("", "")

  implicit def messages: Messages = Helpers.stubMessages()
}
