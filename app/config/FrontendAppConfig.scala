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

package config

import com.google.inject.{Inject, Singleton}
import play.api.Configuration

@Singleton
class FrontendAppConfig @Inject()(configuration: Configuration) {

  private val contactHost                  = configuration.get[String]("urls.contactFrontend")
  private val contactFormServiceIdentifier = "play26frontend"
  val analyticsToken: String               = configuration.get[String](s"google-analytics.token")

  val trackingConsentUrl: String = configuration.get[String]("microservice.services.tracking-consent-frontend.url")
  val gtmContainer: String       = configuration.get[String]("microservice.services.tracking-consent-frontend.gtm.container")

  val analyticsHost: String          = configuration.get[String](s"google-analytics.host")
  val reportAProblemPartialUrl       = s"$contactHost/problem_reports_ajax?service=$contactFormServiceIdentifier"
  val reportAProblemNonJSUrl         = s"$contactHost/problem_reports_nonjs?service=$contactFormServiceIdentifier"
  val betaFeedbackUrl                = s"$contactHost/beta-feedback"
  val betaFeedbackUnauthenticatedUrl = s"$contactHost/beta-feedback-unauthenticated"
  val signOutUrl: String             = configuration.get[String]("urls.logout")
  val nctsEnquiriesUrl: String       = configuration.get[String]("urls.nctsEnquiries")
  val loginHmrcServiceUrl: String    = configuration.get[String]("urls.loginHmrcService")

  lazy val manageTransitMovementsUrl: String             = configuration.get[String]("urls.manageTransitMovementsFrontend")
  lazy val manageTransitMovementsViewArrivalsUrl: String = s"$manageTransitMovementsUrl/view-arrivals"

  lazy val authUrl: String            = configuration.get[Service]("auth").fullServiceUrl
  lazy val loginUrl: String           = configuration.get[String]("urls.login")
  lazy val loginContinueUrl: String   = configuration.get[String]("urls.loginContinue")
  lazy val enrolmentKey: String       = configuration.get[String]("urls.enrolmentKey")
  lazy val destinationUrl: String     = configuration.get[Service]("microservice.services.destination").fullServiceUrl
  lazy val baseDestinationUrl: String = configuration.get[Service]("microservice.services.destination").baseUrl
  lazy val referenceDataUrl: String   = configuration.get[Service]("microservice.services.referenceData").fullServiceUrl
  lazy val timeoutSeconds: String     = configuration.get[String]("session.timeoutSeconds")
  lazy val countdownSeconds: String   = configuration.get[String]("session.countdownSeconds")

  val env: String = configuration.get[String]("env")

  lazy val languageTranslationEnabled: Boolean =
    configuration.get[Boolean]("microservice.services.features.welsh-translation")

}
