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

package services

import base.SpecBase
import connectors.ReferenceDataConnector
import models.reference.CustomsOffice
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.libs.json.Json

import scala.concurrent.Future

class ReferenceDataServiceSpec extends SpecBase with MockitoSugar {
  private val mockReferenceDataConnector = mock[ReferenceDataConnector]

  val application = applicationBuilder(Some(emptyUserAnswers))
    .overrides(
      bind[ReferenceDataConnector].toInstance(mockReferenceDataConnector)
    )
    .build()

  val referenceDataService: ReferenceDataService = application.injector.instanceOf[ReferenceDataService]
  val customsOffice: CustomsOffice               = CustomsOffice("id", "name", Seq.empty)
  val customsOffice1: CustomsOffice              = CustomsOffice("someId", "someName", Seq.empty)

  "ReferenceDataService" - {

    "getCustomsOffice" - {

      "must return valid customs office for the given 'custom office id'" in {
        when(mockReferenceDataConnector.getCustomsOffices()(any(), any())).thenReturn(Future.successful(Seq(customsOffice, customsOffice1)))
        referenceDataService.getCustomsOffice("id").futureValue mustBe Some(customsOffice)
      }

      "must return none if the given 'custom office id' is does not exists in the customsOffice data list" in {
        when(mockReferenceDataConnector.getCustomsOffices()(any(), any())).thenReturn(Future.successful(Seq(customsOffice1)))
        referenceDataService.getCustomsOffice("id").futureValue mustBe None
      }
    }

    "getCustomsOfficesAsJson" - {
      "must return valid customs offices json objects for the given customs office data list" in {

        val expectedJson = Seq(
          Json.obj("value" -> "", "text"       -> ""),
          Json.obj("value" -> "id", "text"     -> "name (id)", "selected" -> true),
          Json.obj("value" -> "someId", "text" -> "someName (someId)", "selected" -> false)
        )

        when(mockReferenceDataConnector.getCustomsOffices()(any(), any())).thenReturn(Future.successful(Seq(customsOffice, customsOffice1)))
        referenceDataService.getCustomsOfficesAsJson(Some("id")).futureValue mustBe expectedJson
      }

      "must return valid empty customs office json object for the empty customs office data list" in {

        val expectedJson = Seq(Json.obj("value" -> "", "text" -> ""))

        when(mockReferenceDataConnector.getCustomsOffices()(any(), any())).thenReturn(Future.successful(Seq.empty))
        referenceDataService.getCustomsOfficesAsJson(Some("id")).futureValue mustBe expectedJson
      }
    }
  }

}
