package controllers

import play.api.mvc._
import play.api.Play.current
import models.Ping
import models.PingBSON._
import play.api.libs.concurrent.Execution.Implicits._
import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import controllers.PingActor.{ReturnMessage, All, Save}

// Reactive Mongo plugin
import play.modules.reactivemongo._

// Play Json imports
import play.api.libs.json._

import play.api.libs.concurrent.Akka

object PingController extends Controller {

  val db = ReactiveMongoPlugin.db

  val pingActor = Akka.system.actorOf(Props(() => new PingActor(db("ping"))))

  implicit val timeout = Timeout(90.seconds) // Akka timeout

  def post = Action(parse.json) { request =>
    Async {
      (pingActor ? Save(Json.fromJson[Ping](request.body).get))
        .mapTo[ReturnMessage]
        .map(rm => Ok(Json.obj( "success" -> rm.success, "message" -> rm.message)))
    }
  }

  def get = Action {
    Async {
      (pingActor ? All())
        .mapTo[List[Ping]]
        .map( pings => Ok(pings.foldLeft(JsArray(List()))( (obj, ping) => obj ++ Json.arr(ping) )))
    }
  }

}