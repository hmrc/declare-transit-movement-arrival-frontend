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

import connectors.DestinationConnector
import javax.inject.Inject
import models.UserAnswers
import services.conversion.ArrivalNotificationConversionService
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.{ExecutionContext, Future}

class ArrivalNotificationService @Inject()(converterService: ArrivalNotificationConversionService, connector: DestinationConnector)(
  implicit ec: ExecutionContext) {

  def submit(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[Option[HttpResponse]] =
    converterService.convertToArrivalNotification(userAnswers) match {
      case Some(notification) => connector.submitArrivalNotification(notification).map(Some(_))
      case None               => Future.successful(None)
    }
}
