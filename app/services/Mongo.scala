// Copyright (C) 2018 Don Kelly <karfai@gmail.com>

// This file is part of Interlibr, a functional component of an
// Internet of Rules (IoR).

// ACKNOWLEDGEMENTS
// Funds: Xalgorithms Foundation
// Collaborators: Don Kelly, Joseph Potvin and Bill Olders.

// This program is free software: you can redistribute it and/or
// modify it under the terms of the GNU Affero General Public License
// as published by the Free Software Foundation, either version 3 of
// the License, or (at your option) any later version.

// This program is distributed in the hope that it will be useful, but
// WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Affero General Public License for more details.

// You should have received a copy of the GNU Affero General Public
// License along with this program. If not, see
// <http://www.gnu.org/licenses/>.
package services

import java.util.UUID.randomUUID
import javax.inject._
import org.mongodb.scala.bson.{ BsonDocument }
import org.mongodb.scala._
import org.mongodb.scala.model.Aggregates._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Projections._
import org.mongodb.scala.model.Sorts._
import org.mongodb.scala.model.Updates._
import org.mongodb.scala.model._
import play.api.{ Logger => PlayLogger }
import play.api.libs.json._
import scala.concurrent.{ Future, Promise }

import scala.concurrent.ExecutionContext.Implicits.global

object MongoActions {
  abstract class Store() {
    def document : Document
    def id: String
  }

  case class StoreDocument(doc: BsonDocument) extends Store {
    private val public_id = randomUUID.toString()

    def this(doc: JsObject) = this(BsonDocument(doc.toString()))

    def document: Document = {
      Document("public_id" -> public_id, "content" -> doc)
    }

    def id = public_id
  }

  case class StoreTestRun(rule_id: String, ctx: BsonDocument) extends Store {
    private val request_id = randomUUID.toString()

    def this(rule_id: String, ctx: JsObject) = this(rule_id, BsonDocument(ctx.toString()))

    def document: Document = {
      Document("rule_id" -> rule_id, "request_id" -> request_id, "context" -> ctx)
    }

    def id = request_id
  }

  class FindOne()
  case class FindByKey(cn: String, key: String, value: String) extends FindOne

  object FindDocumentById {
    def apply(id: String) = FindByKey("documents", "public_id", id)
  }
}

class Mongo @Inject() {
  val url = sys.env.get("MONGO_URL").getOrElse("mongodb://127.0.0.1:27017/")
  val cl = MongoClient(url)
  val db = cl.getDatabase("xadf")

  def find_one(op: MongoActions.FindOne): Future[BsonDocument] = {
    val pr = Promise[BsonDocument]()

    op match {
      case MongoActions.FindByKey(cn, key, value) => {
        db.getCollection(cn).find(equal(key, value)).first().subscribe(
          (doc: Document) => pr.success(doc.toBsonDocument)
        )
      }
    }

    pr.future
  }

  def store(op: MongoActions.Store): Future[String] = {
    val pr = Promise[String]()
    val cn = op match {
      case MongoActions.StoreDocument(_) => "documents"
      case MongoActions.StoreTestRun(_, _)  => "test-runs"
    }

    db.getCollection(cn).insertOne(op.document).subscribe(new Observer[Completed] {
      override def onComplete(): Unit = {
        PlayLogger.debug(s"insert completed")
      }

      override def onNext(res: Completed): Unit = {
        PlayLogger.debug(s"insert next")
        pr.success(op.id)
      }

      override def onError(th: Throwable): Unit = {
        PlayLogger.error(s"failed insert, trigging promise")
        pr.failure(th)
      }
    })

    pr.future
  }
}
