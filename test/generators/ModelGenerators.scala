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

import models._
import models.reference._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}

trait ModelGenerators {
  self: Generators =>

  implicit lazy val arbitraryTranshipmentType: Arbitrary[TranshipmentType] =
    Arbitrary {
      Gen.oneOf(TranshipmentType.values)
    }

  implicit lazy val arbitraryTraderAddress: Arbitrary[Address] =
    Arbitrary {
      for {
        buildingAndStreet <- stringsWithMaxLength(Address.Constants.buildingAndStreetLength)
        city              <- stringsWithMaxLength(Address.Constants.cityLength)
        postcode          <- stringsWithMaxLength(Address.Constants.postcodeLength)
      } yield Address(buildingAndStreet, city, postcode)
    }

  implicit lazy val arbitraryGoodsLocation: Arbitrary[GoodsLocation] =
    Arbitrary {
      Gen.oneOf(GoodsLocation.values)
    }

  implicit lazy val arbitraryMovementReferenceNumber: Arbitrary[MovementReferenceNumber] =
    Arbitrary {
      for {
        year <- Gen
          .choose(0, 99)
          .map(
            y => f"$y%02d"
          )
        country <- Gen.pick(2, 'A' to 'Z')
        serial  <- Gen.pick(13, ('A' to 'Z') ++ ('0' to '9'))
      } yield MovementReferenceNumber(year, country.mkString, serial.mkString)
    }

  implicit lazy val arbitraryEoriNumber: Arbitrary[EoriNumber] =
    Arbitrary {
      for {
        number <- stringsWithMaxLength(domain.TraderDomain.Constants.eoriLength)
      } yield EoriNumber(number)
    }

  implicit lazy val arbitraryCustomsOffice: Arbitrary[CustomsOffice] = {

    val genRoles = Gen.someOf(Seq("TRA", "DEP", "DES"))

    Arbitrary {
      for {
        id          <- arbitrary[String]
        name        <- arbitrary[Option[String]]
        roles       <- genRoles
        phoneNumber <- Gen.option(arbitrary[String])
      } yield CustomsOffice(id, name, phoneNumber)
    }
  }

  implicit lazy val arbitraryCountryCode: Arbitrary[CountryCode] =
    Arbitrary {
      Gen
        .pick(CountryCode.Constants.countryCodeLength, 'A' to 'Z')
        .map(
          code => CountryCode(code.mkString)
        )
    }

  implicit lazy val arbitraryCountry: Arbitrary[Country] =
    Arbitrary {
      for {
        code <- arbitrary[CountryCode]
        name <- arbitrary[String]
      } yield Country(code, name)
    }
}
