package models

import play.api.libs.json._
import play.api.libs.json.JsString


object Country extends Enumeration {

  type Country = Value

  val argentina = Value

  implicit object CountryFormat extends Format[Country] {
    override def reads(json: JsValue): JsResult[Country] = json match {
      case JsString("argentina") => JsSuccess(Country.argentina)
      case _ => JsError("Invalid JsValue type for Country conversion: must be JsString")
    }

    override def writes(c: Country) = c match {
      case Country.argentina => JsString("argentina")
    }
  }

  /*
  implicit val countryReads = Json.reads[Country]

  implicit val countryWrites = Json.writes[Country]
  */

  //implicit val countryFormat = Json.format[Country]
}
