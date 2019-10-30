package forms

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class AuthorisedLocationFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "authorisedLocation.error.required"
  val lengthKey = "authorisedLocation.error.length"
  val maxLength = 8

  val form = new AuthorisedLocationFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
