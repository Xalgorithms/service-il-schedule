package actors

import akka.actor._
import javax.inject._
import org.apache.kafka.clients.producer.{ KafkaProducer, ProducerRecord }
import play.api.Logger
import scala.collection.JavaConverters._

object MessagesActor {
  def props = Props[MessagesActor]
}

class MessagesActor extends Actor {
  private val _cfg: Map[String, Object] = Map(
    "key.serializer" -> "org.apache.kafka.common.serialization.StringSerializer",
    "value.serializer" -> "org.apache.kafka.common.serialization.StringSerializer",
    "client.id" -> "services-schedule",
    "bootstrap.servers" -> "localhost:9092"
  )

  private val _producer: KafkaProducer[String, String] = new KafkaProducer[String, String](
    _cfg.asJava
  )

  val topic = "xadf.test"

  def receive = {
    case GlobalMessages.DocumentAdded(id) => {
      Logger.debug(s"# sending message (topic=${topic}; id=${id})")
      _producer.send(new ProducerRecord(topic, id))
      Logger.debug(s"> sent message (topic=${topic}; id=${id})")
    }
  }
}
