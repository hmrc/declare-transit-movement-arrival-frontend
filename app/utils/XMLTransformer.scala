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

import scala.xml.{Elem, Node, NodeSeq, Text}
import scala.xml.transform.{RewriteRule, RuleTransformer}

object XMLTransformer {

  private def createRuleTransformer(key: String, value: String): RuleTransformer =
    new RuleTransformer(new RewriteRule {
      override def transform(n: Node): Seq[Node] = n match {
        case elem: Elem if elem.label.equalsIgnoreCase(key) =>
          elem.copy(child = Text(value))
        case other => other
      }
    })

  def updateXmlNode(key: String, value: String, inputXml: NodeSeq): NodeSeq =
    createRuleTransformer(key, value).transform(inputXml.head)
}