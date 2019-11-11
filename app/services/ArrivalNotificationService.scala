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

package services

import java.time.LocalDate

import connectors.DestinationConnector
import javax.inject.Inject
import models.UserAnswers
import models.domain.messages.{ArrivalNotification, NormalNotification}
import models.domain.{Trader, TraderWithEori}
import pages._
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.{ExecutionContext, Future}

class ArrivalNotificationService @Inject()(connector: DestinationConnector) {

  def submit(userAnswers: UserAnswers)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    connector.submitArrivalNotification(convertToArrivalNotification(userAnswers))
  }

  def convertToArrivalNotification(userAnswers: UserAnswers): ArrivalNotification = {
    NormalNotification(userAnswers.id.value,
      "", //TODO notificationPlace
      LocalDate.now(),
      userAnswers.get(CustomsSubPlacePage),
      traderAddress(userAnswers),
      userAnswers.get(PresentationOfficePage).getOrElse(""),
      Seq.empty
    )
  }

  private def traderAddress(userAnswers: UserAnswers): Trader = {
    val traderAddress = userAnswers.get(TraderAddressPage)

    TraderWithEori(userAnswers.get(TraderEoriPage).getOrElse(""),
      userAnswers.get(TraderNamePage),
      traderAddress.map(_.buildingAndStreet),
      traderAddress.map(_.postcode),
      traderAddress.map(_.city),
      Some("GB")
    )
  }
}
