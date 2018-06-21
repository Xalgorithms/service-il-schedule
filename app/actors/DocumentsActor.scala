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
package actors

import akka.actor._
import java.util.UUID.randomUUID
import javax.inject._
import play.api.libs.json._
import scala.collection.immutable
import scala.util.{ Success, Failure }

import services.{ Mongo, MongoActions }

import scala.concurrent.ExecutionContext.Implicits.global

object DocumentsActor {
  case class StoreDocument(doc: JsObject)
  case class StoreTestRun(rule_id: String, ctx: JsObject)
}

class DocumentsActor @Inject() (mongo: Mongo, publish: services.Publish) extends Actor with ActorLogging {
  import DocumentsActor._

  def receive = {
    case StoreDocument(doc) => {
      val them = sender()
      mongo.store(new MongoActions.StoreDocument(doc)).onComplete {
        case Success(public_id) => {
          log.debug(s"stored (public_id=${public_id})")
          publish.publish_global(GlobalMessages.DocumentAdded(public_id))
          them ! public_id
        }
        case Failure(th) => {
          log.error(s"failed store")
        }
      }
    }

    case StoreTestRun(rule_id, doc) => {
      val them = sender()
      mongo.store(new MongoActions.StoreTestRun(rule_id, doc)).onComplete {
        case Success(request_id) => {
          log.debug(s"stored test run (request_id=${request_id})")
          publish.publish_global(GlobalMessages.TestRunRequested(request_id))
          them ! request_id
        }

        case Failure(th) => {
          log.error("failed store")
        }
      }
    }
  }

  def store(doc: JsObject, sender: ActorRef): Unit = {
  }
}
