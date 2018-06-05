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

case class AppAction(name: String, payload: JsObject)

@Singleton
class ActionsController @Inject()(
  @Named("actors-documents") actor_docs: ActorRef,
  cc: ControllerComponents
) extends AbstractController(cc) {
  implicit val app_action_read: Reads[AppAction] = (
    (JsPath \ "name").read[String] and
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

    (actor_docs ? DocumentsActor.StoreDocument(act.payload)).mapTo[String].map { public_id =>
      Logger.debug(s"documents responded (public_id=${public_id}")
      Ok(Json.obj("status" -> "ok", "public_id" -> public_id))
    }
  }
}
