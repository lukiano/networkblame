package controllers

import models.Ping
import models.PingBSON._
import akka.actor.Actor
import reactivemongo.bson.BSONDocument
import controllers.PingActor._
import akka.pattern.pipe
import reactivemongo.core.commands.LastError
import controllers.PingActor.Save
import reactivemongo.api.collections.default.BSONCollection
import controllers.PingActor.All

object PingActor {
  sealed trait PingMessage
  case class Save(ping: Ping) extends PingMessage
  case class All() extends PingMessage
  case class Query(query: BSONDocument) extends PingMessage

  case class ReturnMessage(success: Boolean, message: String)
  object ReturnMessage {
    def apply(t: Throwable):ReturnMessage = ReturnMessage(success = false, t.getMessage)
    def apply(le: LastError):ReturnMessage = ReturnMessage(!le.inError, (if (le.inError) le.message else ""))
  }
}

class PingActor(collection: BSONCollection) extends Actor {

  import context.dispatcher

  private val all = BSONDocument()
  private def execute(query: BSONDocument) = collection.find(all).cursor[Ping].toList() recover {case _ => List()}

  def receive = {
    case pm: PingMessage => pm match {
      case Save(ping) => collection.insert(ping) map(ReturnMessage.apply) recover {case t => ReturnMessage(t)} pipeTo sender
      case All() => execute(all) pipeTo sender
      case Query(query) => execute(query) pipeTo sender
    }
  }

}
