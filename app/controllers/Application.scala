package controllers

import play.api._
import play.api.mvc._
import play.api.Play.current

import play.api.libs.concurrent.Execution.Implicits._

// Play Json imports
import play.api.libs.json._

object Application extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

}