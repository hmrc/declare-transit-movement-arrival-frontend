/*
 * Copyright 2023 HM Revenue & Customs
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
import play.api.mvc.RequestHeader
import uk.gov.hmrc.http.StringContextOps
import java.net.URL

@Singleton
class FrontendAppConfig @Inject() (configuration: Configuration) {

  val contactHost: String                  = configuration.get[String]("contact-frontend.host")
  val contactFormServiceIdentifier: String = "CTCTraders"

  val analyticsToken: String = configuration.get[String](s"google-analytics.token")

  val trackingConsentUrl: String = configuration.get[String]("microservice.services.tracking-consent-frontend.url")
  val gtmContainer: String       = configuration.get[String]("microservice.services.tracking-consent-frontend.gtm.container")

  val showPhaseBanner: Boolean        = configuration.get[Boolean]("banners.showPhase")
  val userResearchUrl: String         = configuration.get[String]("urls.userResearch")
  val showUserResearchBanner: Boolean = configuration.get[Boolean]("banners.showUserResearch")

  val analyticsHost: String = configuration.get[String](s"google-analytics.host")

  private val host: String = configuration.get[String]("host")

  def feedbackUrl(implicit request: RequestHeader): URL =
    url"$contactHost/contact/beta-feedback?service=$contactFormServiceIdentifier&backUrl=${host + request.uri}"

  val signOutUrl: String          = configuration.get[String]("urls.logoutContinue") + configuration.get[String]("urls.feedback")
  val loginHmrcServiceUrl: String = configuration.get[String]("urls.loginHmrcService")

  lazy val manageTransitMovementsUrl: String             = configuration.get[String]("urls.manageTransitMovementsFrontend")
  lazy val serviceUrl: String                            = s"$manageTransitMovementsUrl/what-do-you-want-to-do"
  lazy val manageTransitMovementsViewArrivalsUrl: String = s"$manageTransitMovementsUrl/view-arrivals"

  lazy val authUrl: String            = configuration.get[Service]("auth").fullServiceUrl
  lazy val loginUrl: String           = configuration.get[String]("urls.login")
  lazy val loginContinueUrl: String   = configuration.get[String]("urls.loginContinue")
  lazy val destinationUrl: String     = configuration.get[Service]("microservice.services.destination").fullServiceUrl
  lazy val baseDestinationUrl: String = configuration.get[Service]("microservice.services.destination").baseUrl
  lazy val referenceDataUrl: String   = configuration.get[Service]("microservice.services.referenceData").fullServiceUrl
  lazy val timeoutSeconds: String     = configuration.get[String]("session.timeoutSeconds")
  lazy val countdownSeconds: String   = configuration.get[String]("session.countdownSeconds")
  lazy val enrolmentProxyUrl: String  = configuration.get[Service]("microservice.services.enrolment-store-proxy").fullServiceUrl

  lazy val legacyEnrolmentKey: String           = configuration.get[String]("keys.legacy.enrolmentKey")
  lazy val legacyEnrolmentIdentifierKey: String = configuration.get[String]("keys.legacy.enrolmentIdentifierKey")
  lazy val newEnrolmentKey: String              = configuration.get[String]("keys.enrolmentKey")
  lazy val newEnrolmentIdentifierKey: String    = configuration.get[String]("keys.enrolmentIdentifierKey")

  lazy val eccEnrolmentSplashPage: String = configuration.get[String]("urls.eccEnrolmentSplashPage")

  lazy val languageTranslationEnabled: Boolean =
    configuration.get[Boolean]("microservice.services.features.welsh-translation")

  lazy val maxSeals: Int      = configuration.get[Int]("limits.maxSeals")
  lazy val maxContainers: Int = configuration.get[Int]("limits.maxContainers")
  lazy val maxEvents: Int     = configuration.get[Int]("limits.maxEvents")

  lazy val cacheTtl: Int           = configuration.get[Int]("mongodb.timeToLiveInSeconds")
  lazy val replaceIndexes: Boolean = configuration.get[Boolean]("mongodb.replaceIndexes")

  lazy val countriesOfDestination: Seq[String] = configuration.get[Seq[String]]("countriesOfDestination")
}
