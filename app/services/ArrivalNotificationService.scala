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

import java.time.LocalTime

import config.FrontendAppConfig
import connectors.DestinationConnector
import javax.inject.Inject
import models.UserAnswers
import models.messages.{MessageSender, NormalNotification}
import repositories.InterchangeControlReferenceIdRepository
import services.conversion.{ArrivalNotificationConversionService, SubmissionModelService}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.{ExecutionContext, Future}
import scala.xml.Node

class ArrivalNotificationService @Inject()(
                                            converterService: ArrivalNotificationConversionService,
                                            connector: DestinationConnector,
                                            appConfig: FrontendAppConfig,
                                            databaseService: DatabaseService,
                                            submissionModelService: SubmissionModelService,
                                            interchangeControlReferenceIdRepository: InterchangeControlReferenceIdRepository
                                          )(
  implicit ec: ExecutionContext) {

  def submit(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[Option[HttpResponse]] =
    converterService.convertToArrivalNotification(userAnswers) match {
      case Some(notification) => connector.submitArrivalNotification(notification).map(Some(_))
      case None => Future.successful(None)
    }

  def generateXml(arrivalNotification: NormalNotification): Future[Node] = {

    val messageSender = MessageSender(appConfig.env, "eori")

    interchangeControlReferenceIdRepository.nextInterchangeControlReferenceId().map {
      referenceId =>
        submissionModelService.convertToSubmissionModel1(
          arrivalNotification = arrivalNotification,
          messageSender = messageSender,
          interchangeControlReference = referenceId,
          timeOfPresentation = LocalTime.now()
        )
        .toXml
    }
  }
}
