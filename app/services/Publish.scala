package services

import akka.actor.{ ActorSystem }
import javax.inject._

@Singleton
class Publish @Inject() (system: ActorSystem) {
  Seq(actors.MessagesActor.props, actors.TablesActor.props).foreach { props =>
    system.eventStream.subscribe(system.actorOf(props), classOf[actors.GlobalMessages.GlobalMessage])
  }

  def publish_global(m: actors.GlobalMessages.GlobalMessage) {
    system.eventStream.publish(m)
  }
}
