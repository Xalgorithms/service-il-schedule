package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import scala.concurrent._
import scala.util.{ Success, Failure }

import ExecutionContext.Implicits.global

case class AppAction(name: String, payload: JsObject)

@Singleton
class ActionsController @Inject()(
  cc: ControllerComponents,
  docs: services.Documents,
  tables: services.Tables,
  messages: services.Messages
) extends AbstractController(cc) {
  implicit val app_action_read: Reads[AppAction] = (
    (JsPath \ "name").read[String] and
    (JsPath \ "payload").read[JsObject]
  )(AppAction.apply _)

  def validate_json[A : Reads] = parse.json.validate(
     _.validate[A].asEither.left.map(e => BadRequest(JsError.toJson(e)))
  )

  def create() = Action.async(validate_json) { request =>
    val pr = Promise[Result]()
    val act = request.body
    Logger.debug(s"received: create (action=${act.name})")
    docs.store(act.payload).onComplete {
      case Success(public_id) => {
        Logger.debug(s"stored document (public_id=${public_id})")
        Future.sequence(Seq(
          tables.store_envelope(public_id, (act.payload \ "envelope").getOrElse(null)),
          messages.deliver(public_id))
        ).onComplete {
          case Success(_) => {
            Logger.debug("sending success to caller")
            pr.success(Ok(Json.obj("status" -> "ok", "public_id" -> public_id)))
          }
          case Failure(e) => {
            Logger.error(e.toString)
            pr.success(InternalServerError("oops"))
          }
        }
      }
      case Failure(e) => {
        pr.success(InternalServerError(Json.obj("status" -> "failed")))
      }
    }

    pr.future
  }
}
