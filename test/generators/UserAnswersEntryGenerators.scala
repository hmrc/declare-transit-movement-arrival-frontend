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

package generators

import models._
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import pages._
import pages.events._
import pages.events.transhipments._
import pages.events.seals._
import play.api.libs.json.JsValue
import play.api.libs.json.Json

trait UserAnswersEntryGenerators extends PageGenerators {
  self: Generators =>

  implicit lazy val arbitraryEoriNumberUserAnswersEntry: Arbitrary[(EoriNumberPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[EoriNumberPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryEoriConfirmationUserAnswersEntry: Arbitrary[(EoriConfirmationPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[EoriConfirmationPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryConsigneeNameUserAnswersEntry: Arbitrary[(ConsigneeNamePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ConsigneeNamePage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryHaveSealsChangedUserAnswersEntry: Arbitrary[(HaveSealsChangedPage, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[HaveSealsChangedPage]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryAddSealUserAnswersEntry: Arbitrary[(AddSealPage, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[AddSealPage]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitrarySealIdentityUserAnswersEntry: Arbitrary[(SealIdentityPage, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[SealIdentityPage]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryAddContainerUserAnswersEntry: Arbitrary[(AddContainerPage, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[AddContainerPage]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryContainerNumberUserAnswersEntry: Arbitrary[(ContainerNumberPage, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ContainerNumberPage]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryTransportNationalityUserAnswersEntry: Arbitrary[(TransportNationalityPage, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[TransportNationalityPage]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryTransportIdentityUserAnswersEntry: Arbitrary[(TransportIdentityPage, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[TransportIdentityPage]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryTranshipmentTypeUserAnswersEntry: Arbitrary[(TranshipmentTypePage, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[TranshipmentTypePage]
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
