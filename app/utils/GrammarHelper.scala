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

package utils

import uk.gov.hmrc.viewmodels.{NunjucksSupport, Text}

abstract class GrammarHelper {
  def plural(x: String): Text.Message
  def singular(y: String): Text.Message
}

object ImplicitGrammarConversion extends NunjucksSupport {

  implicit val grammar: GrammarHelper = new GrammarHelper {
    override def plural(x: String): Text.Message =
      msg"$x.plural"

    override def singular(y: String): Text.Message =
      msg"$y.singular"
  }

  def singularOrPlural(key: String, condition: Int)(implicit g: GrammarHelper): Text.Message =
    if (condition == 1) g.singular(key) else g.plural(key)

  def singularOrPluralWithArgs(key: String, condition: Int, args: Any): Text.Message =
    singularOrPlural(key, condition).withArgs(args)
}
