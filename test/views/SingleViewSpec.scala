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

package views

import base.SpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Messages, MessagesApi}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.RequestHeader
import renderer.Renderer
import uk.gov.hmrc.viewmodels.NunjucksSupport

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

abstract class SingleViewSpec(protected val viewUnderTest: String, hasSignOutLink: Boolean = true)
    extends SpecBase
    with ViewSpecAssertions
    with NunjucksSupport
    with GuiceOneAppPerSuite {

  require(viewUnderTest.endsWith(".njk"), "Expected view with file extension of `.njk`")

  implicit override val messages: Messages = app.injector.instanceOf[MessagesApi].preferred(fakeRequest)

  def renderDocument(json: JsObject = Json.obj()): Future[Document] = {
    import play.api.test.CSRFTokenHelper._

    implicit val fr: RequestHeader = fakeRequest.withCSRFToken

    app.injector
      .instanceOf[Renderer]
      .render(viewUnderTest, json)
      .map(
        html => Jsoup.parse(html.toString())
      )
  }

  if (hasSignOutLink) {
    "must render sign out link in header" in {
      val doc: Document = renderDocument().futureValue

      assertPageHasSignOutLink(
        doc = doc,
        expectedText = "Sign out",
        expectedHref = "http://localhost:9553/bas-gateway/sign-out-without-state?continue=http://localhost:9514/feedback/manage-transit-movements"
      )
    }
  } else {
    "must not render sign out link in header" in {
      val doc: Document = renderDocument(
        Json.obj("signInUrl" -> "/manage-transit-movements/what-do-you-want-to-do")
      ).futureValue

      assertPageHasNoSignOutLink(doc)
    }
  }

  "must append service to feedback link" in {
    val doc: Document = renderDocument().futureValue
    val link          = doc.getElementsByClass("govuk-phase-banner__text").first().getElementsByClass("govuk-link").first()
    link.attr("href") must include("?service=CTCTraders")
  }

}
