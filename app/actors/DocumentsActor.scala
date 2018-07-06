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

// ours
import org.xalgorithms.storage.bson.Find

// local
import services.{ Mongo, MongoActions }

import scala.concurrent.ExecutionContext.Implicits.global

object DocumentsActor {
  case class StoreDocument(doc: JsObject)
  case class StoreAdhocExecution(rule_id: String, ctx: JsObject)
  case class StoreExecution(rule_ref: String, ctx: JsObject)
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

    case StoreAdhocExecution(rule_id, ctx) => {
      val them = sender()
      mongo.store(new MongoActions.StoreExecution(rule_id, ctx)).onComplete {
        case Success(request_id) => {
          log.debug(s"stored execution (request_id=${request_id})")
          publish.publish_global(GlobalMessages.TestRunAdded(request_id))
          them ! request_id
        }

        case Failure(th) => {
          log.error("failed store")
        }
      }
    }

    case StoreExecution(rule_ref, ctx) => {
      val them = sender()
      log.info("locating rule by reference")
      mongo.find_one(MongoActions.FindRuleByReference(rule_ref)).onComplete {
        case Success(rule_doc) => {
          log.info("found corresponding rule document")
          Find.maybe_find_text(rule_doc, "public_id") match {
            case Some(public_id) => {
              log.info(s"storing execution of rule (id=#{public_id})")
              mongo.store(new MongoActions.StoreExecution(public_id, ctx)).onComplete {
                case Success(request_id) => {
                  log.debug(s"stored execution (request_id=${request_id})")
                  publish.publish_global(GlobalMessages.TestRunAdded(request_id))
                  them ! request_id
                }
                case Failure(th) => {
                  log.error("failed store")
                }
              }
            }
            case None => {
              log.error(s"failed to find id")
            }
          }
        }
        case Failure(th) => {
          log.error(s"failed to find document using ref (ref=#{rule_ref})")
        }
      }
    }
  }

  def store(doc: JsObject, sender: ActorRef): Unit = {
  }
}
