package services

import base.SpecBase

class ArrivalNotificationServiceSpec extends SpecBase {

  val arrivalNotificationService = app.injector.instanceOf[ArrivalNotificationService]

  "ArrivalNotificationService" - {
    "must submit data for valid input " in {
      arrivalNotificationService.submit(emptyUserAnswers)
    }

    "must return None on submission of invalid data" in  {

    }
  }

}
