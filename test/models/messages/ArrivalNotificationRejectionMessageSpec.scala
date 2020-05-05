package models.messages

import base.SpecBase
import com.lucidchart.open.xtract.XmlReader
import generators.{MessagesModelGenerators, ModelGenerators}
import models.{FunctionalError, MovementReferenceNumber}
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen

class ArrivalNotificationRejectionMessageSpec extends SpecBase with ScalaCheckDrivenPropertyChecks with MessagesModelGenerators {

  val rejectionMessageXmlGen: Gen[ArrivalNotificationRejectionMessage] =
    for {
      mrn    <- arbitrary[MovementReferenceNumber].map(_.toString())
      date   <- datesBetween(pastDate, dateNow)
      action <- arbitrary[String]
      reason <- arbitrary[String]
      errors <- arbitrary[FunctionalError]
    } yield ArrivalNotificationRejectionMessage(mrn, date, Some(action), Some(reason), Seq(errors))

  "deserialization from XML" - {
    "do the thing" in {
      forAll(arbitrary[ArrivalNotificationRejectionMessage]) {
        rejectionMessage =>
          val xml = <CC008A>
            <SynIdeMES1>UNOC</SynIdeMES1>
            <SynVerNumMES2>3</SynVerNumMES2>
            <MesSenMES3>NTA.GB</MesSenMES3>
            <MesRecMES6>SYST17B-NCTS_EU_EXIT</MesRecMES6>
            <DatOfPreMES9>20191018</DatOfPreMES9>
            <TimOfPreMES10>1525</TimOfPreMES10>
            <IntConRefMES11>81391018152535</IntConRefMES11>
            <AppRefMES14>NCTS</AppRefMES14>
            <TesIndMES18>0</TesIndMES18>
            <MesIdeMES19>81391018152535</MesIdeMES19>
            <MesTypMES20>GB008A</MesTypMES20>
            <HEAHEA>
              <DocNumHEA5>19IT021300100075E9</DocNumHEA5>
              <ArrRejDatHEA142>20191018</ArrRejDatHEA142>
              <ArrRejReaHEA242>Invalid IE007 Message</ArrRejReaHEA242>
            </HEAHEA>
            <FUNERRER1>
              <ErrTypER11>93</ErrTypER11>
              <ErrPoiER12>Invalid MRN</ErrPoiER12>
              <OriAttValER14>19IT02110010007827</OriAttValER14>
            </FUNERRER1>
          </CC008A>

          val result = XmlReader.of[ArrivalNotificationRejectionMessage].read(xml).toOption.value

          result mustEqual rejectionMessage

      }
    }
  }
}
