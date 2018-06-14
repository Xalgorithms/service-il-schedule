package services

import java.util.UUID.randomUUID

import org.mongodb.scala.bson.{ BsonDocument }
import org.mongodb.scala._
import org.mongodb.scala.model.Aggregates._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Projections._
import org.mongodb.scala.model.Sorts._
import org.mongodb.scala.model.Updates._
import org.mongodb.scala.model._
import play.api.libs.json._
import scala.concurrent.{ Future, Promise }

class Mongo(logger: Logger = new LocalLogger()) {
  val url = sys.env.get("MONGO_URL").getOrElse("mongodb://127.0.0.1:27017/")
  val cl = MongoClient(url)
  val db = cl.getDatabase("xadf")

  private def documents(): MongoCollection[Document] = {
    db.getCollection("documents")
  }

  def find_one(public_id: String): Future[BsonDocument] = {
    val pr = Promise[BsonDocument]()
    documents().find(equal("public_id", public_id)).first().subscribe(
      (doc: Document) => pr.success(doc.toBsonDocument)
    )
    pr.future
  }

  def store(doc: JsObject): Future[String] = {
    val pr = Promise[String]()

    val public_id = randomUUID.toString()
    documents().insertOne(
      Document(
        "public_id" -> public_id,
        "content"   -> BsonDocument(doc.toString())
      )
    ).subscribe(new Observer[Completed]() {
      override def onComplete(): Unit = {
        logger.debug(s"insert completed (public_id=${public_id})")
      }

      override def onNext(res: Completed): Unit = {
        logger.debug(s"insert next (public_id=${public_id})")
        pr.success(public_id)
      }

      override def onError(th: Throwable): Unit = {
        logger.error(s"failed insert, trigging promise (public_id=${public_id})")
        pr.failure(th)
      }
    })

    pr.future
  }
}
