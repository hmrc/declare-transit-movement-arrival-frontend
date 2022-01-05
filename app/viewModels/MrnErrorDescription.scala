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

package viewModels

import models.messages.ErrorType.{DuplicateMrn, InvalidMrn, MRNError, UnknownMrn}

private[viewModels] object MrnErrorDescription extends (MRNError => String) {

  override def apply(v1: MRNError): String = v1 match {
    case UnknownMrn   => "movementReferenceNumberRejection.error.unknown"
    case DuplicateMrn => "movementReferenceNumberRejection.error.duplicate"
    case InvalidMrn   => "movementReferenceNumberRejection.error.invalid"
  }
}
