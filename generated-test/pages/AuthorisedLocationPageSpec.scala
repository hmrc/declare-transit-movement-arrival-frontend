package pages

import pages.behaviours.PageBehaviours


class AuthorisedLocationPageSpec extends PageBehaviours {

  "AuthorisedLocationPage" - {

    beRetrievable[String](AuthorisedLocationPage)

    beSettable[String](AuthorisedLocationPage)

    beRemovable[String](AuthorisedLocationPage)
  }
}
