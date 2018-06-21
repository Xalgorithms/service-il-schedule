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
package controllers

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import javax.inject._
import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import scala.concurrent._
import scala.concurrent.duration._
import scala.util.{ Success, Failure }

import ExecutionContext.Implicits.global

import actors.DocumentsActor

case class AppAction(name: String, args: Map[String, String], payload: JsObject)

@Singleton
class ActionsController @Inject()(
  @Named("actors-documents") actor_docs: ActorRef,
  cc: ControllerComponents
) extends AbstractController(cc) {
  implicit val app_action_read: Reads[AppAction] = (
    (JsPath \ "name").read[String] and
    (JsPath \ "args").read[Map[String, String]] and
    (JsPath \ "payload").read[JsObject]
  )(AppAction.apply _)

  // for ask
  implicit val timeout: Timeout = 5.seconds

  def validate_json[A : Reads] = parse.json.validate(
     _.validate[A].asEither.left.map(e => BadRequest(JsError.toJson(e)))
  )

  def create() = Action.async(validate_json) { request =>
    val act = request.body

    Logger.debug(s"received: create (action=${act.name})")

    act.name match {
      case "execute" => {
        Logger.debug("running execute")
        (actor_docs ? DocumentsActor.StoreDocument(act.payload)).mapTo[String].map { public_id =>
          Logger.debug(s"documents responded (public_id=${public_id})")
          Ok(Json.obj("status" -> "ok", "public_id" -> public_id))
        }
      }

      case "test-rule" => {
        act.args.get("rule_id") match {
          case Some(rule_id) => {
            Logger.debug(s"running test-rule (id=${rule_id})")
            (actor_docs ? DocumentsActor.StoreTestRun(rule_id, act.payload)).mapTo[String].map { request_id =>
              Logger.debug(s"documents responded (request_id=${request_id})")
              Ok(Json.obj("status" -> "ok", "request_id" -> request_id))
            }
          }
          case None => {
            Logger.warn("no rule_id specified")
            Future.successful(Forbidden(Json.obj("status" -> "failure_missing_rule_id")))
          }
        }
      }
    }
  }
}
