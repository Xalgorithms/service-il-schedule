package actors

import akka.actor._
import org.joda.time.DateTime
import play.api.Logger
import scala.concurrent.{Future => ScalaFuture}
import scala.util.{ Success, Failure }

import scala.concurrent.ExecutionContext.Implicits.global

import models.{ DocumentEnvelope, Envelope, InterlibrDatabase, ConnectedInterlibrDatabase }
import services.{ Mongo }

object TablesActor {
  def props = Props[TablesActor]
}

class TablesActor extends Actor {
  val db: InterlibrDatabase = ConnectedInterlibrDatabase
  val mongo = new Mongo()

  def receive = {
    case GlobalMessages.DocumentAdded(id) => {
      Logger.info(s"document added (${id})")
      mongo.find_one(id).onComplete {
        case Success(doc) => {
          Logger.info(s"found document (public_id=${id})")
          val de = new DocumentEnvelope(id, doc)
          de.rows.foreach { e =>
            Logger.info(s"storing envelope (public_id=${id}; party=${e.party}")
            db.storeEnvelope(e)
          }
        }

        case Failure(th) => {
          Logger.error("failed to find document")
        }
      }
    }
  }
}
