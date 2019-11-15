/*
 * Copyright 2019 HM Revenue & Customs
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

package generators

import models._
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import pages._
import play.api.libs.json.{JsValue, Json}

trait UserAnswersEntryGenerators extends PageGenerators with ModelGenerators {

  implicit lazy val arbitrarySealsChangedUserAnswersEntry: Arbitrary[(SealsChangedPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[SealsChangedPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryIsTranshipmentUserAnswersEntry: Arbitrary[(IsTranshipmentPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[IsTranshipmentPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryIncidentInformationUserAnswersEntry: Arbitrary[(IncidentInformationPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[IncidentInformationPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryEventReportedUserAnswersEntry: Arbitrary[(EventReportedPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[EventReportedPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryEventPlaceUserAnswersEntry: Arbitrary[(EventPlacePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[EventPlacePage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryEventCountryUserAnswersEntry: Arbitrary[(EventCountryPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[EventCountryPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryIncidentOnRouteUserAnswersEntry: Arbitrary[(IncidentOnRoutePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[IncidentOnRoutePage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryTraderNameUserAnswersEntry: Arbitrary[(TraderNamePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[TraderNamePage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryTraderEoriUserAnswersEntry: Arbitrary[(TraderEoriPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[TraderEoriPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryTraderAddressUserAnswersEntry: Arbitrary[(TraderAddressPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[TraderAddressPage.type]
        value <- arbitrary[TraderAddress].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryAuthorisedLocationUserAnswersEntry: Arbitrary[(AuthorisedLocationPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[AuthorisedLocationPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryCustomsSubPlaceUserAnswersEntry: Arbitrary[(CustomsSubPlacePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[CustomsSubPlacePage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryPresentationOfficeUserAnswersEntry: Arbitrary[(PresentationOfficePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[PresentationOfficePage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryGoodsLocationUserAnswersEntry: Arbitrary[(GoodsLocationPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[GoodsLocationPage.type]
        value <- arbitrary[GoodsLocation].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryMovementReferenceNumberUserAnswersEntry: Arbitrary[(MovementReferenceNumberPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[MovementReferenceNumberPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }
}
