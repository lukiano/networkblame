package models

import play.api.libs.json._


case class Position(latitude: Double, longitude: Double)

object Position {

  implicit object PositionFormat extends Format[Position] {

    override def reads(json: JsValue): JsResult[Position] = JsSuccess(Position(
      (json \ "latitude").as[Double],
      (json \ "longitude").as[Double]
    ))

    override def writes(pos: Position) = Json.obj( "latitude" -> pos.latitude, "longitude" -> pos.longitude)
  }

}



