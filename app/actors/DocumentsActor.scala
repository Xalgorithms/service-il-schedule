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
