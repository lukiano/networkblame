package controllers

import play.api.mvc._
import play.api.Play.current
import reactivemongo.bson.BSONDocument
import reactivemongo.api.collections.default.BSONCollection
import models.Ping
import models.PingBSON._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.Logger
import akka.actor.Props
import akka.pattern.ask
import scala.concurrent.Future
import akka.util.Timeout
import scala.concurrent.duration._

// Reactive Mongo plugin
import play.modules.reactivemongo._

// Play Json imports
import play.api.libs.json._

import play.api.libs.concurrent.Akka

object PingController extends Controller /* with MongoController */ {

  val db = ReactiveMongoPlugin.db
  lazy val collection: BSONCollection = db("ping")

  val pingActor = Akka.system.actorOf(Props(() => new PingActor(db)))

  implicit val timeout = Timeout(90.seconds) // Akka timeout

  def post = Action(parse.json) { request =>
    Async {
      val ping: Ping = Json.fromJson[Ping](request.body).get
      val future: Future[String] = (pingActor ? Save(ping)).mapTo[String]
      future.map( body => Ok(body))
    }
  }

  def get = Action {
    Async {
      val future: Future[List[Ping]] = (pingActor ? All).mapTo[List[Ping]]
      future.map( pings => Ok(pings.foldLeft(JsArray(List()))( (obj, ping) => obj ++ Json.arr(ping) )))
    }
  }

}