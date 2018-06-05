package actors

import java.util.UUID.randomUUID
import javax.inject._
import akka.actor._
import org.mongodb.scala.bson.{ BsonDocument }
import org.mongodb.scala._
import play.api.libs.json._
import play.api.Logger
import scala.collection.immutable

object DocumentsActor {
  case class StoreDocument(doc: JsObject)
}

class DocumentsActor @Inject() (publish: services.Publish) extends Actor {
  import DocumentsActor._

  private var _documents: MongoCollection[Document] = null

  def receive = {
    case StoreDocument(doc) => {
      store(doc, sender())
    }
  }

  def documents(): MongoCollection[Document] = {
    if (null == _documents) {
      val url = sys.env.get("MONGO_URL").getOrElse("mongodb://127.0.0.1:27017/")
      val cl = MongoClient(url)
      val db = cl.getDatabase("xadf")
      _documents = db.getCollection("documents")
    }

    _documents
  }

  def store(doc: JsObject, sender: ActorRef): Unit = {
    val public_id = randomUUID.toString()

    Logger.debug(s"inserting document (public_id=${public_id})")
    documents().insertOne(
      Document(
        "public_id" -> public_id,
        "content" -> BsonDocument(doc.toString())
      )
    ).subscribe(new Observer[Completed]() {
      override def onComplete(): Unit = {
        Logger.debug(s"insert completed, publishing notification (public_id=${public_id})")
        publish.publish_global(GlobalMessages.DocumentAdded(public_id))
      }

      override def onNext(res: Completed): Unit = {
        Logger.debug(s"inserted, informing sender (public_id=${public_id})")
        sender ! public_id
      }

      override def onError(e: Throwable): Unit = {
        Logger.error(s"failed insert, trigging promise (public_id=${public_id})")
      }
    })
  }
}
