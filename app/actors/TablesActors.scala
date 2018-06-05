package actors

import akka.actor._
import play.api.Logger

object TablesActor {
  def props = Props[TablesActor]
}

class TablesActor extends Actor {
  def receive = {
    case GlobalMessages.DocumentAdded(id) => {
      Logger.info(s"document added (${id})")
    }
  }
}
