package services

import java.util.UUID.randomUUID
import javax.inject._
import org.mongodb.scala.bson.{ BsonDocument }
import org.mongodb.scala._
import play.api.libs.json._
import play.api.Logger
import scala.concurrent._

import ExecutionContext.Implicits.global

@Singleton
class Documents @Inject()(
) {
  private var _documents: MongoCollection[Document] = null

  def store(doc: JsObject): Future[String] = {
    val pr = Promise[String]()
    val public_id = randomUUID.toString()

    Logger.debug(s"inserting document (public_id=${public_id})")
    documents().insertOne(
      Document(
        "public_id" -> public_id,
        "content" -> BsonDocument(doc.toString())
      )
    ).subscribe(new Observer[Completed]() {
      override def onComplete(): Unit = {
        Logger.debug(s"insert completed (public_id=${public_id})")
      }

      override def onNext(res: Completed): Unit = {
        Logger.debug(s"inserted, trigging promise (public_id=${public_id})")
        pr success public_id
      }

      override def onError(e: Throwable): Unit = {
        Logger.error(s"failed insert, trigging promise (public_id=${public_id})")
        pr failure (e)
      }
    })

    pr.future
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
}
