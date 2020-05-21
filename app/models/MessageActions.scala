package models

import play.api.libs.json.Reads
import play.api.libs.json._

case class MessageActions(arrivalId: ArrivalId, actions: Seq[MessageAction])

object MessageActions {


  implicit lazy val reads: Reads[MessageActions] = {

    import play.api.libs.functional.syntax._

    (
      (__ \ "arrivalId").read[ArrivalId] and
       __.read[Seq[MessageAction]]
    )
  }

}
