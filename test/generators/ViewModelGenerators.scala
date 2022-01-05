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

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import uk.gov.hmrc.viewmodels.SummaryList.{Action, Key, Row, Value}
import uk.gov.hmrc.viewmodels.Text.{Literal, Message}
import uk.gov.hmrc.viewmodels.{Content, Text}
import viewModels.sections.Section

// TODO: Upstream to uk.gov.hmrc.viewmodels
trait ViewModelGenerators {
  self: Generators =>

  private val maxSeqLength = 10

  implicit lazy val arbitraryMessage: Arbitrary[Message] = Arbitrary {
    for {
      key    <- arbitrary[String]
      length <- Gen.choose(1, maxSeqLength)
      args   <- Gen.containerOfN[Seq, String](length, arbitrary[String])
    } yield Message(key, args)
  }

  implicit lazy val arbitraryLiteral: Arbitrary[Literal] = Arbitrary {
    arbitrary[String].map(Literal)
  }

  implicit lazy val arbitraryText: Arbitrary[Text] = Arbitrary {
    Gen.oneOf(arbitrary[Message], arbitrary[Literal])
  }

  implicit lazy val arbitraryContent: Arbitrary[Content] = Arbitrary {
    // Gen.oneOf(arbitrary[Text], arbitrary[Html]) // TODO: This is the ideal generator here, but is it worth the effort to generate HTML here?
    arbitrary[Text]
  }

  implicit lazy val arbitraryKey: Arbitrary[Key] = Arbitrary {
    for {
      content <- arbitrary[Content]
      length  <- Gen.choose(1, maxSeqLength)
      classes <- Gen.containerOfN[Seq, String](length, arbitrary[String])
    } yield Key(content, classes)
  }

  implicit lazy val arbitraryValue: Arbitrary[Value] = Arbitrary {
    for {
      content <- arbitrary[Content]
      length  <- Gen.choose(1, maxSeqLength)
      classes <- Gen.containerOfN[Seq, String](length, arbitrary[String])
    } yield Value(content, classes)
  }

  implicit lazy val arbitraryAction: Arbitrary[Action] = Arbitrary {
    for {
      content            <- arbitrary[Content]
      href               <- arbitrary[String]
      visuallyHiddenText <- arbitrary[Option[Text]]
      length             <- Gen.choose(1, maxSeqLength)
      classes            <- Gen.containerOfN[Seq, String](length, arbitrary[String])
      attributes         <- Gen.const(Map.empty[String, String]) // TODO: Do we need to have valid attributes generated here? Use case?
    } yield Action(content, href, visuallyHiddenText, classes, attributes)
  }

  implicit lazy val arbitraryRow: Arbitrary[Row] = Arbitrary {
    for {
      key     <- arbitrary[Key]
      value   <- arbitrary[Value]
      length  <- Gen.choose(1, maxSeqLength)
      actions <- Gen.containerOfN[Seq, Action](length, arbitrary[Action])
    } yield Row(key, value, actions)
  }

  implicit lazy val arbitrarySection: Arbitrary[Section] = Arbitrary {
    for {
      sectionTitle <- arbitrary[Option[Text]]
      length       <- Gen.choose(1, maxSeqLength)
      row          <- Gen.containerOfN[Seq, Row](length, arbitrary[Row])
    } yield Section(sectionTitle, row)
  }
}
