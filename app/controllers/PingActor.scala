package controllers

import models.Ping
import models.PingBSON._
import reactivemongo.api.{Cursor, DB}
import scala.concurrent.{Future, ExecutionContext}
import akka.actor.Actor
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.core.commands.LastError
import scala.util.{Failure, Success}
import reactivemongo.bson.BSONDocument
import play.api.Logger

sealed case class Save(ping: Ping)

sealed case class All()

class PingActor(db: DB)(implicit ec: ExecutionContext) extends Actor {

  def receive = {
    case Save(ping) => {
      Logger(this.getClass).info("Save received with Ping: " + ping)

      val collection: BSONCollection = db("ping")
      val future: Future[LastError] = collection.insert(ping)
      val zender = sender
      future.onComplete {
        case Success(le: LastError) => {
          Logger(this.getClass).info("Save success")
          val body = "{'success': " + !le.inError + ", 'message': '" + (if (le.inError) le.message else "") + "'}"
          zender ! body
        }
        case Failure(throwable) => {
          Logger(this.getClass).error("Save failure", throwable)
          val body = "{'success': false, 'message': '" + throwable.getMessage + "'}"
          zender ! body
        }
      }
    }

    case All => {
      Logger(this.getClass).info("All received")

      val collection: BSONCollection = db("ping")

      val cursor: Cursor[Ping] = collection.find(BSONDocument()).cursor[Ping]
      val future: Future[List[Ping]] = cursor.toList()

      val zender = sender

      future.onComplete {
        case Success(list: List[Ping]) => {
          Logger(this.getClass).info("All success. Count: " + list.size)
          zender ! list
        }
        case Failure(throwable) => {
          Logger(this.getClass).error("All failure", throwable)
        }
      }

      //cursor.enumerate()
      //sender ! HttpResponse(entity = HttpBody(ContentType.`application/json`, body))
    }

  }

}
