package models

import base.SpecBase
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.MustMatchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsValue, Json}


class CustomOfficeSpec extends SpecBase with MustMatchers with ScalaCheckPropertyChecks  {

  "CustomeOffice" - {

    "deserialise" in {

      forAll(arbitrary[String], arbitrary[String], arbitrary[Seq[String]]) {
        (id, name, roles) => {

          val json: JsValue = Json.parse(s"""
                                   |{
                                   |  "id": $id
                                   |  "name": $name
                                   |  "roles": $roles
                                   |}
                                   |""".stripMargin)

          json.as[CustomsOffice] mustBe CustomsOffice(id, name, roles)
        }
      }
    }
  }

}
