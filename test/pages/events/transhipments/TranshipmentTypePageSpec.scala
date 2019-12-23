/*
 * Copyright 2019 HM Revenue & Customs
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

package pages.events.transhipments

import models.TranshipmentType
import pages.behaviours.PageBehaviours

class TranshipmentTypePageSpec extends PageBehaviours {

  val index = 0

  "TranshipmentTypePage" - {

    beRetrievable[TranshipmentType](TranshipmentTypePage(index))

    beSettable[TranshipmentType](TranshipmentTypePage(index))

    beRemovable[TranshipmentType](TranshipmentTypePage(index))
  }
}
