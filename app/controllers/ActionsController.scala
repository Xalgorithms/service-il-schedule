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

case class AppAction(name: String, args: Map[String, String], document: Option[JsObject])

@Singleton
class ActionsController @Inject()(
  @Named("actors-documents") actor_docs: ActorRef,
  publish: services.Publish,
  cc: ControllerComponents
) extends AbstractController(cc) {
  implicit val app_action_read: Reads[AppAction] = (
    (JsPath \ "name").read[String] and
    (JsPath \ "args").read[Map[String, String]] and
    (JsPath \ "document").readNullable[JsObject]
  )(AppAction.apply _)

  // for ask
  implicit val timeout: Timeout = 5.seconds

  val effective_ctx_keys = Seq(
    "key",
    "country",
    "region",
    "timezone",
    "issued"
  )

  // NOTE: a version of this exists in
  // storage/src/main/scala/org/xalgorithms/storage/data/Mongo.scala
  // the storage lib should provide a factored make_rule_id
  def make_rule_id(ns: String, name: String, version: String): String = {
    play.api.libs.Codecs.sha1(s"R(${ns}:${name}:${version})")
  }

  def validate_json[A : Reads] = parse.json.validate(
     _.validate[A].asEither.left.map(e => BadRequest(JsError.toJson(e)))
  )

  def apply_execute(args: Map[String, String], opt_doc: Option[JsObject]): Future[Result] = {
    val ns = args.getOrElse("namespace", null)
    val name = args.getOrElse("name", null)
    val ver = args.getOrElse("version", null)
    val rule_id = make_rule_id(ns, name, ver)

    Logger.debug(s"executing (ns=${ns}; name=${name}; ver=${ver}; rule_id=${rule_id})")

    (actor_docs ? DocumentsActor.StoreExecution(rule_id, opt_doc)).mapTo[String].map { req_id =>
      Ok(Json.obj("status" -> "ok", "request_id" -> req_id))
    }
  }

  def apply_submit(args: Map[String, String], opt_doc: Option[JsObject]): Future[Result] = {
    val opt_content = opt_doc.flatMap { doc =>
      (doc \ "content").asOpt[JsObject]
    }

    val opt_effective_ctxs = opt_doc.flatMap { doc =>
      (doc \ "effective_contexts").asOpt[JsArray].map { os =>
        os.value.map { o =>
          effective_ctx_keys.foldLeft(Map[String, String]()) { (m, k) =>
            (o \ k).asOpt[String] match {
              case Some(v) => m ++ Map(k -> v)
              case None => m
            }
          }
        }
      }
    }

    opt_content match {
      case Some(content) => {
        (actor_docs ? DocumentsActor.StoreSubmission(content, opt_effective_ctxs)).mapTo[String].map { req_id =>
          Ok(Json.obj("status" -> "ok", "request_id" -> req_id))
        }
      }
      case None => Future.successful(Ok(Json.obj("status" -> "fail", "reason" -> "document_required")))
    }
  }

  def apply_verify(args: Map[String, String], opt_doc: Option[JsObject]): Future[Result] = {
    val opt_content = opt_doc.flatMap { doc =>
      (doc \ "content").asOpt[JsObject]
    }

    val opt_effective_ctxs = opt_doc.flatMap { doc =>
      (doc \ "effective_contexts").asOpt[JsArray].map { os =>
        os.value.map { o =>
          effective_ctx_keys.foldLeft(Map[String, String]()) { (m, k) =>
            (o \ k).asOpt[String] match {
              case Some(v) => m ++ Map(k -> v)
              case None => m
            }
          }
        }
      }
    }

    args("what") match {
      case "effective" => {
        opt_content match {
          case Some(content) => {
            val m = DocumentsActor.StoreEffectiveVerification(content, opt_effective_ctxs)
              (actor_docs ? m).mapTo[String].map { req_id =>
                Ok(Json.obj("status" -> "ok", "request_id" -> req_id))
              }
          }

          case None => Future.successful(Ok(Json.obj("status" -> "fail", "reason" -> "no_content")))
        }
      }

      case "applicable" => {
        opt_content match {
          case Some(content) => {
            val rule_id = args("rule_id")
            val m = DocumentsActor.StoreApplicableVerification(content, rule_id)
              (actor_docs ? m).mapTo[String].map { req_id =>
                Ok(Json.obj("status" -> "ok", "request_id" -> req_id))
              }
          }

          case None => Future.successful(Ok(Json.obj("status" -> "fail", "reason" -> "no_content")))
        }
      }

      case _ => Future.successful(Ok(Json.obj("status" -> "fail", "reason" -> "unknown_verify_what")))
    }
  }

  private val actions = Map(
    "execute" -> (apply_execute _),
    "submit"  -> (apply_submit _),
    "verify"  -> (apply_verify _)
  )

  def create() = Action.async(validate_json) { request =>
    val act = request.body

    Logger.debug(s"received: create (action=${act.name})")
    actions.get(act.name) match {
      case Some(fn) => fn(act.args, act.document)
      case None => Future.successful(NotFound(Json.obj("status" -> "failure_unknown_action")))
    }
  }
}
