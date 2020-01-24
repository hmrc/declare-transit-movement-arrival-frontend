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

package pages.events.transhipments

import generators.MessagesModelGenerators
import models.messages.Container
import models.{Index, TranshipmentType, UserAnswers}
import models.TranshipmentType._
import models.reference.Country
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours
import queries.ContainersQuery

class TranshipmentTypePageSpec extends PageBehaviours with MessagesModelGenerators {

  val index = 0

  "TranshipmentTypePage" - {

    beRetrievable[TranshipmentType](TranshipmentTypePage(index))

    beSettable[TranshipmentType](TranshipmentTypePage(index))

    beRemovable[TranshipmentType](TranshipmentTypePage(index))

    "cleanup" - {
      "must remove transport identity and nationality when the answer change to Different Container" in {

        forAll(arbitrary[UserAnswers], arbitrary[String], arbitrary[Country]) {
          (userAnswers, transportIdentity, transportNationality) =>
            val result = userAnswers
              .set(TranshipmentTypePage(index), DifferentVehicle)
              .success
              .value
              .set(TransportIdentityPage(index), transportIdentity)
              .success
              .value
              .set(TransportNationalityPage(index), transportNationality)
              .success
              .value
              .set(TranshipmentTypePage(index), DifferentContainer)
              .success
              .value

            result.get(TransportIdentityPage(index)) must not be defined
            result.get(TransportNationalityPage(index)) must not be defined
        }
      }

      "must remove container numbers when the answer changes to Different Vehicle" in {

        forAll(arbitrary[UserAnswers], arbitrary[Container]) {
          (userAnswers, containerNumber) =>
            val result = userAnswers
              .set(TranshipmentTypePage(index), DifferentContainer)
              .success
              .value
              .set(ContainerNumberPage(index, Index(0)), containerNumber)
              .success
              .value
              .set(ContainerNumberPage(index, Index(1)), containerNumber)
              .success
              .value
              .set(TranshipmentTypePage(index), TranshipmentType.DifferentVehicle)
              .success
              .value

            result.get(ContainersQuery(index)) must not be defined
        }
      }

      "must remove all transhipment data when there is no answer" in {

        forAll(arbitrary[UserAnswers], arbitrary[String], arbitrary[Container], arbitrary[Country]) {
          (userAnswers, stringAnswer, container, country) =>
            val result = userAnswers
              .set(TransportIdentityPage(index), stringAnswer)
              .success
              .value
              .set(TransportNationalityPage(index), country)
              .success
              .value
              .set(ContainerNumberPage(index, Index(0)), container)
              .success
              .value
              .set(ContainerNumberPage(index, Index(1)), container)
              .success
              .value
              .remove(TranshipmentTypePage(index))
              .success
              .value

            result.get(TransportIdentityPage(index)) must not be defined
            result.get(TransportNationalityPage(index)) must not be defined
            result.get(ContainerNumberPage(index, Index(0))) must not be defined
            result.get(ContainerNumberPage(index, Index(1))) must not be defined
        }
      }
    }
  }
}
