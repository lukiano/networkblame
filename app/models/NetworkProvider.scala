package models

import play.api.libs.json._
import play.api.libs.json.JsString


sealed abstract case class NetworkProvider(country: Country.Country) {
  val name: String
}

object personal extends NetworkProvider(Country.argentina) {
  val name = "personal"
}

object movistar extends NetworkProvider(Country.argentina) {
  val name = "movistar"
}

object NetworkProvider {

  def apply(provider: String): NetworkProvider = provider match {
      case "personal" => personal
      case "movistar" => movistar
  }

  implicit object NetworkProviderFormat extends Format[NetworkProvider] {

    override def reads(json: JsValue): JsResult[NetworkProvider] = json match {
      case JsString(provider)                 => JsSuccess(NetworkProvider(provider))
      case _                                  => JsError("Invalid JsValue type for NetworkProvider conversion: JsString")
    }

    override def writes(np: NetworkProvider) = JsString(np.name)
  }

}

