package actors

import play.api.libs.json._
import play.api.libs.functional.syntax._

object Triggers {
  abstract class Trigger
  case class TriggerById(id: String) extends Trigger
  case class TriggerDocument(doc_id: String, effective_ctx: Map[String, String]) extends Trigger
}

object Implicits {
  import Triggers._

  implicit val trigger_writes = new Writes[Trigger] {
    def writes(tr: Trigger) = tr match {
      case TriggerById(id) => Json.obj(
        "context" -> Map("task" -> "triggers", "action" -> "trigger_by_id"),
        "args"    -> Map("id" -> id)
      )
      case TriggerDocument(doc_id, effective_ctx) => Json.obj(
        "context" -> Map("task" -> "triggers", "action" -> "trigger_document"),
        "args"    -> Json.obj(
          "document_id" -> doc_id,
          "effective_context" -> effective_ctx
        )
      )
    }
  }
}

object Actions {
  case class InvokeTrigger(topic: String, trigger: Triggers.Trigger)
}
