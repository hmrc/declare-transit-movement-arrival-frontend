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

package forms

import forms.behaviours.BooleanFieldBehaviours
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.FormError
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest

class IsTraderAddressPlaceOfNotificationFormProviderSpec extends BooleanFieldBehaviours with GuiceOneAppPerSuite {

  private val messagesApi = app.injector.instanceOf[MessagesApi]
  private def fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("", "")
  private implicit def messages: Messages = messagesApi.preferred(fakeRequest)

  private val postcode = "NE99 1AX"
  private val requiredKey = messages("isTraderAddressPlaceOfNotification.error.required", postcode)
  private val invalidKey = "error.boolean"

  private val formProvider = new IsTraderAddressPlaceOfNotificationFormProvider()
  private val form = formProvider(postcode)

  ".value" - {

    val fieldName = "value"

    behave like booleanField(
      form,
      fieldName,
      invalidError = FormError(fieldName, invalidKey)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}