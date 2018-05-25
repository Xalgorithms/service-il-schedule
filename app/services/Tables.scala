package services

import javax.inject._
import play.api.Logger
import play.api.libs.json._
import scala.concurrent._

import ExecutionContext.Implicits.global

@Singleton
class Tables @Inject()(
) {
  def store_envelope(doc_id: String, envelope: JsValue): Future[Unit] = {
    Logger.debug(s"scheduling envelope store (doc_id=${doc_id})")
    Future {
      Logger.debug(s"storing envelope (doc_id=${doc_id})")
    }
  }
}
