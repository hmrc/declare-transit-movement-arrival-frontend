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

package generators

import models.Index
import org.scalacheck.Arbitrary
import pages._
import pages.events._
import pages.events.seals._
import pages.events.transhipments._

trait PageGenerators {

  implicit lazy val arbitraryConsigneeAddressPage: Arbitrary[ConsigneeAddressPage.type] =
    Arbitrary(ConsigneeAddressPage)

  implicit lazy val arbitraryEoriNumberPage: Arbitrary[ConsigneeEoriNumberPage.type] =
    Arbitrary(ConsigneeEoriNumberPage)

  implicit lazy val arbitraryConsigneeNamePage: Arbitrary[ConsigneeNamePage.type] =
    Arbitrary(ConsigneeNamePage)

  implicit lazy val arbitraryHaveSealsChangedPage: Arbitrary[HaveSealsChangedPage] =
    Arbitrary(HaveSealsChangedPage(Index(0)))

  implicit lazy val arbitraryAddSealPage: Arbitrary[AddSealPage] =
    Arbitrary(AddSealPage(Index(0)))

  implicit lazy val arbitrarySealIdentityPage: Arbitrary[SealIdentityPage] =
    Arbitrary(SealIdentityPage(Index(0), Index(0)))

  implicit lazy val arbitraryConfirmRemoveContainerPage: Arbitrary[ConfirmRemoveContainerPage.type] =
    Arbitrary(ConfirmRemoveContainerPage)

  implicit lazy val arbitraryAddContainerPage: Arbitrary[AddContainerPage] =
    Arbitrary(AddContainerPage(Index(0)))

  implicit lazy val arbitraryContainerNumberPage: Arbitrary[ContainerNumberPage] =
    Arbitrary(ContainerNumberPage(Index(0), Index(0)))

  implicit lazy val arbitraryTransportNationalityPage: Arbitrary[TransportNationalityPage] =
    Arbitrary(TransportNationalityPage(Index(0)))

  implicit lazy val arbitraryTransportIdentityPage: Arbitrary[TransportIdentityPage] =
    Arbitrary(TransportIdentityPage(Index(0)))

  implicit lazy val arbitraryTranshipmentTypePage: Arbitrary[TranshipmentTypePage] =
    Arbitrary(TranshipmentTypePage(Index(0)))

  implicit lazy val arbitraryAddEventPage: Arbitrary[AddEventPage.type] =
    Arbitrary(AddEventPage)

  implicit lazy val arbitraryPlaceOfNotificationPage: Arbitrary[PlaceOfNotificationPage.type] =
    Arbitrary(PlaceOfNotificationPage)

  implicit lazy val arbitraryIsTraderAddressPlaceOfNotificationPage: Arbitrary[IsTraderAddressPlaceOfNotificationPage.type] =
    Arbitrary(IsTraderAddressPlaceOfNotificationPage)

  implicit lazy val arbitraryIsTranshipmentPage: Arbitrary[IsTranshipmentPage] =
    Arbitrary(IsTranshipmentPage(Index(0)))

  implicit lazy val arbitraryIncidentInformationPage: Arbitrary[IncidentInformationPage] =
    Arbitrary(IncidentInformationPage(Index(0)))

  implicit lazy val arbitraryEventReportedPage: Arbitrary[EventReportedPage] =
    Arbitrary(EventReportedPage(Index(0)))

  implicit lazy val arbitraryEventPlacePage: Arbitrary[EventPlacePage] =
    Arbitrary(EventPlacePage(Index(0)))

  implicit lazy val arbitraryEventCountryPage: Arbitrary[EventCountryPage] =
    Arbitrary(EventCountryPage(Index(0)))

  implicit lazy val arbitraryIncidentOnRoutePage: Arbitrary[IncidentOnRoutePage.type] =
    Arbitrary(IncidentOnRoutePage)

  implicit lazy val arbitraryTraderNamePage: Arbitrary[TraderNamePage.type] =
    Arbitrary(TraderNamePage)

  implicit lazy val arbitraryTraderEoriPage: Arbitrary[TraderEoriPage.type] =
    Arbitrary(TraderEoriPage)

  implicit lazy val arbitraryTraderAddressPage: Arbitrary[TraderAddressPage.type] =
    Arbitrary(TraderAddressPage)

  implicit lazy val arbitraryAuthorisedLocationPage: Arbitrary[AuthorisedLocationPage.type] =
    Arbitrary(AuthorisedLocationPage)

  implicit lazy val arbitraryCustomsSubPlacePage: Arbitrary[CustomsSubPlacePage.type] =
    Arbitrary(CustomsSubPlacePage)

  implicit lazy val arbitraryCustomsOfficePage: Arbitrary[CustomsOfficePage.type] =
    Arbitrary(CustomsOfficePage)

  implicit lazy val arbitrarySimplifiedCustomsOfficePage: Arbitrary[SimplifiedCustomsOfficePage.type] =
    Arbitrary(SimplifiedCustomsOfficePage)

  implicit lazy val arbitraryGoodsLocationPage: Arbitrary[GoodsLocationPage.type] =
    Arbitrary(GoodsLocationPage)

  implicit lazy val arbitraryMovementReferenceNumberPage: Arbitrary[MovementReferenceNumberPage.type] =
    Arbitrary(MovementReferenceNumberPage)
}
