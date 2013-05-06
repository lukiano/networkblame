package models

import org.joda.time.DateTime
import reactivemongo.bson._
import play.api.libs.json._
import reactivemongo.bson.BSONLong
import reactivemongo.bson.BSONInteger
import reactivemongo.bson.BSONDateTime
import reactivemongo.bson.BSONDouble
import play.api.libs.json.JsString
import reactivemongo.bson.BSONString
import play.api.libs.json.JsNumber

case class Ping(networkProvider: NetworkProvider, position: Position,
                 dateTime: DateTime, packetsSent: Int, packetsLost: Int,
                 longestInMillis: Long, shortestInMillis: Long)

object PingBSON {

  implicit object PingBSONWriter extends BSONDocumentWriter[Ping] {

    def write(ping: Ping) = BSONDocument(
        "networkProvider" -> BSONString(ping.networkProvider.name),
        "position" -> BSONDocument(
          "latitude" -> ping.position.latitude,
          "longitude" -> ping.position.longitude
        ),
        "dateTime" -> BSONDateTime(ping.dateTime.getMillis),
        "packetsSent" -> BSONInteger(ping.packetsSent),
        "packetsLost" -> BSONInteger(ping.packetsLost),
          "shortestInMillis" -> BSONLong(ping.shortestInMillis),
        "longestInMillis" -> BSONLong(ping.longestInMillis)
    )

  }

  implicit object PingBSONReader extends BSONDocumentReader[Ping] {

    private def pos(doc: BSONDocument): Position = Position(
      doc.getAs[BSONDouble]("latitude").get.value,
      doc.getAs[BSONDouble]("longitude").get.value
    )

    def read(bson: BSONDocument): Ping = Ping(
      NetworkProvider(bson.getAs[BSONString]("networkProvider").get.value),
      pos(bson.getAs[BSONDocument]("position").get),
      new DateTime(bson.getAs[BSONDateTime]("dateTime").get.value),
      bson.getAs[BSONInteger]("packetsSent").get.value,
      bson.getAs[BSONInteger]("packetsLost").get.value,
      bson.getAs[BSONLong]("longestInMillis").get.value,
      bson.getAs[BSONLong]("shortestInMillis").get.value
    )

  }
}

object Ping {

  implicit object DateTimeFormat extends Format[DateTime] {

    override def reads(json: JsValue): JsResult[DateTime] = json match {
      case JsNumber(value) if value.isValidLong => JsSuccess(new DateTime(value.toLong))
      case JsString(dateString)                 => JsSuccess(new DateTime(dateString))
      case _                                    => JsError("Invalid JsValue type for DateTime conversion: must be JsNumber or JsString")
    }

    override def writes(dt: DateTime) = JsNumber(dt.getMillis)
  }

  /*
  implicit val pingReads = Json.reads[Ping]

  implicit val pingWrites = Json.writes[Ping]
  */

  implicit val pingFormat = Json.format[Ping]
}

