package actors

import akka.actor._
import org.joda.time.DateTime
import play.api.Logger
import scala.concurrent.{Future => ScalaFuture}
import scala.util.{ Success, Failure }

import scala.concurrent.ExecutionContext.Implicits.global

import models.{ Envelope, InterlibrDatabase, ConnectedInterlibrDatabase }

object TablesActor {
  def props = Props[TablesActor]
}

class TablesActor extends Actor {
  val db: InterlibrDatabase = ConnectedInterlibrDatabase

  def receive = {
    case GlobalMessages.DocumentAdded(id) => {
      Logger.info(s"document added (${id})")
      db.storeEnvelope(Envelope("4", "buyer", "CA", "CA-ON", "America/Toronto", new DateTime())).onComplete {
        case Success(o) => println(o)
        case Failure(e) => println(e)
      }
    }
  }
}
