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
import pages.events.AddEventPage
import pages.events.EventCountryPage
import pages.events.EventPlacePage
import pages.events.EventReportedPage
import pages.events.IncidentInformationPage
import pages.events.IsTranshipmentPage
import pages.events.transhipments.{TranshipmentTypePage, TransportIdentityPage}
import play.api.libs.json.JsValue
import play.api.libs.json.Json

trait UserAnswersEntryGenerators extends PageGenerators {
  self: Generators =>

  implicit lazy val arbitraryAddContainerUserAnswersEntry: Arbitrary[(AddContainerPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[AddContainerPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryContainerNumberUserAnswersEntry: Arbitrary[(ContainerNumberPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ContainerNumberPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryTransportNationalityUserAnswersEntry: Arbitrary[(TransportNationalityPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[TransportNationalityPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryTransportIdentityUserAnswersEntry: Arbitrary[(TransportIdentityPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[TransportIdentityPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryTranshipmentTypeUserAnswersEntry: Arbitrary[(TranshipmentTypePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[TranshipmentTypePage.type]
        value <- arbitrary[TranshipmentType].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryAddEventUserAnswersEntry: Arbitrary[(AddEventPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[AddEventPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryPlaceOfNotificationUserAnswersEntry: Arbitrary[(PlaceOfNotificationPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[PlaceOfNotificationPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryIsTraderAddressPlaceOfNotificationUserAnswersEntry: Arbitrary[(IsTraderAddressPlaceOfNotificationPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[IsTraderAddressPlaceOfNotificationPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryIsTranshipmentUserAnswersEntry: Arbitrary[(IsTranshipmentPage, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[IsTranshipmentPage]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryIncidentInformationUserAnswersEntry: Arbitrary[(IncidentInformationPage, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[IncidentInformationPage]
        value <- stringsWithMaxLength(350).suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryEventReportedUserAnswersEntry: Arbitrary[(EventReportedPage, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[EventReportedPage]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryEventPlaceUserAnswersEntry: Arbitrary[(EventPlacePage, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[EventPlacePage]
        value <- stringsWithMaxLength(35).suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryEventCountryUserAnswersEntry: Arbitrary[(EventCountryPage, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[EventCountryPage]
        value <- stringsWithMaxLength(2).suchThat(_.nonEmpty).map(Json.toJson(_))
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
        value <- stringsWithMaxLength(35).suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryTraderEoriUserAnswersEntry: Arbitrary[(TraderEoriPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[TraderEoriPage.type]
        value <- stringsWithMaxLength(17).suchThat(_.nonEmpty).map(Json.toJson(_))
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
        value <- stringsWithMaxLength(17).suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryCustomsSubPlaceUserAnswersEntry: Arbitrary[(CustomsSubPlacePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[CustomsSubPlacePage.type]
        value <- stringsWithMaxLength(17).suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryPresentationOfficeUserAnswersEntry: Arbitrary[(PresentationOfficePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[PresentationOfficePage.type]
        value <- stringsWithMaxLength(8).suchThat(_.nonEmpty).map(Json.toJson(_))
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
