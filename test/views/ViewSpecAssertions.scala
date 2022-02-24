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

package views

import org.jsoup.nodes.{Document, Element}
import org.scalatest.Assertion
import org.scalatest.matchers.must.Matchers
import play.api.i18n.Messages

import scala.collection.JavaConverters._

trait ViewSpecAssertions {
  self: Matchers =>

  def messages: Messages

  def getByElementId(doc: Document, id: String): Element = {
    val elem: Element = doc.getElementById(id)
    elem must not equal null
    elem
  }

  def getByElementTestIdSelector(doc: Document, id: String): Seq[Element] =
    (doc.select(s"[data-testid=$id]")).asScala

  def findByElementId(doc: Document, id: String): Option[Element] =
    Option(doc.getElementById(id))

  def assertContainsClass(doc: Document, expectedClass: String) =
    assert(doc.getElementsByClass(expectedClass).isEmpty == false, s"\n\nPage should have class $expectedClass")

  def assertContainsNoClass(doc: Document, expectedClass: String) =
    assert(doc.getElementsByClass(expectedClass).isEmpty, s"\n\nPage should not have class $expectedClass")

  def assertPageHasSignOutLink(doc: Document, expectedText: String, expectedHref: String): Assertion = {
    val link = doc.getElementsByClass("hmrc-sign-out-nav__link").first()
    link.text() mustBe expectedText
    link.attr("href") mustBe expectedHref
  }

  def assertPageHasNoSignOutLink(doc: Document): Assertion =
    doc.getElementsByClass("hmrc-sign-out-nav__link").isEmpty mustBe true

}
