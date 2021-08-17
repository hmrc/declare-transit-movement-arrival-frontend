/*
 * Copyright 2021 HM Revenue & Customs
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

package config

import com.google.inject.{Inject, Singleton}
import play.api.Configuration

@Singleton
class FrontendAppConfig @Inject()(configuration: Configuration) {

  lazy val contactHost: String             = configuration.get[Service]("microservice.services.contact-frontend").baseUrl
  lazy val contactFrontendUrl: String      = configuration.get[Service]("microservice.services.contact-frontend").fullServiceUrl
  val contactFormServiceIdentifier: String = "CTCTraders"
  val analyticsToken: String               = configuration.get[String](s"google-analytics.token")

  val trackingConsentUrl: String = configuration.get[String]("microservice.services.tracking-consent-frontend.url")
  val gtmContainer: String       = configuration.get[String]("microservice.services.tracking-consent-frontend.gtm.container")

  val analyticsHost: String          = configuration.get[String](s"google-analytics.host")
  val betaFeedbackUrl                = s"$contactFrontendUrl/beta-feedback"
  val betaFeedbackUnauthenticatedUrl = s"$contactFrontendUrl/beta-feedback-unauthenticated"
  val signOutUrl: String             = configuration.get[String]("urls.logoutContinue") + configuration.get[String]("urls.feedback")
  val loginHmrcServiceUrl: String    = configuration.get[String]("urls.loginHmrcService")
  val isNIJourneyEnabled: Boolean    = configuration.getOptional[Boolean]("microservice.services.features.isNIJourneyEnabled").getOrElse(false)

  lazy val manageTransitMovementsUrl: String             = configuration.get[String]("urls.manageTransitMovementsFrontend")
  lazy val serviceUrl: String                            = s"$manageTransitMovementsUrl/what-do-you-want-to-do"
  lazy val manageTransitMovementsViewArrivalsUrl: String = s"$manageTransitMovementsUrl/view-arrivals"

  lazy val authUrl: String                             = configuration.get[Service]("auth").fullServiceUrl
  lazy val loginUrl: String                            = configuration.get[String]("urls.login")
  lazy val loginContinueUrl: String                    = configuration.get[String]("urls.loginContinue")
  lazy val enrolmentKey: String                        = configuration.get[String]("urls.enrolmentKey")
  lazy val destinationUrl: String                      = configuration.get[Service]("microservice.services.destination").fullServiceUrl
  lazy val baseDestinationUrl: String                  = configuration.get[Service]("microservice.services.destination").baseUrl
  lazy val referenceDataUrl: String                    = configuration.get[Service]("microservice.services.referenceData").fullServiceUrl
  lazy val timeoutSeconds: String                      = configuration.get[String]("session.timeoutSeconds")
  lazy val countdownSeconds: String                    = configuration.get[String]("session.countdownSeconds")
  lazy val enrolmentProxyUrl: String                   = configuration.get[Service]("microservice.services.enrolment-store-proxy").fullServiceUrl
  lazy val enrolmentManagementFrontendEnrolUrl: String = configuration.get[String]("urls.enrolmentManagementFrontendEnrolUrl")
  lazy val enrolmentIdentifierKey: String              = configuration.get[String]("keys.enrolmentIdentifierKey")

  lazy val languageTranslationEnabled: Boolean =
    configuration.get[Boolean]("microservice.services.features.welsh-translation")

}
