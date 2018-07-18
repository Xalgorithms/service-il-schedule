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
import javax.inject._
import org.joda.time.DateTime
import scala.concurrent.{Future => ScalaFuture}
import scala.util.{ Success, Failure }

// ours
import org.xalgorithms.storage.data.{ MongoActions }

import scala.concurrent.ExecutionContext.Implicits.global

// local
import models.{ DocumentEnvelope, Envelope, InterlibrDatabase, ConnectedInterlibrDatabase }
import services.{ InjectableMongo }

class TablesActor @Inject() (mongo: InjectableMongo) extends Actor with ActorLogging {
  val db: InterlibrDatabase = ConnectedInterlibrDatabase

  def receive = {
    case GlobalMessages.DocumentAdded(id) => {
      log.info(s"document added (id=${id})")
      mongo.find_one_bson(MongoActions.FindDocumentById(id)).onComplete {
        case Success(opt_doc) => {
          opt_doc match {
            case Some(doc) => {
              log.info(s"found document (public_id=${id})")
              val de = new DocumentEnvelope(id, doc)
              de.rows.foreach { e =>
                log.info(s"storing envelope (public_id=${id}; party=${e.party})")
                db.storeEnvelope(e)
              }
            }

            case None => log.info("no document found")
          }
        }

        case Failure(th) => {
          log.error("failed to find document")
        }
      }
    }
  }
}
