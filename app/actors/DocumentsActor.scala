package actors

import javax.inject._
import akka.actor._
import play.api.libs.json._
import play.api.Logger
import scala.collection.immutable
import scala.util.{ Success, Failure }

import services.{ Mongo }

import scala.concurrent.ExecutionContext.Implicits.global

object DocumentsActor {
  case class StoreDocument(doc: JsObject)
}

class DocumentsActor @Inject() (publish: services.Publish) extends Actor {
  import DocumentsActor._

  private var _mongo = new Mongo()

  def receive = {
    case StoreDocument(doc) => store(doc, sender())
  }

  def store(doc: JsObject, sender: ActorRef): Unit = {
    _mongo.store(doc).onComplete {
      case Success(public_id) => {
        Logger.debug(s"stored (public_id=${public_id})")
        publish.publish_global(GlobalMessages.DocumentAdded(public_id))
        sender ! public_id
      }
      case Failure(th) => {
        Logger.error(s"failed store")
      }
    }
  }
}
