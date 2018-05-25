package services

import javax.inject._
import org.apache.kafka.clients.producer.{ KafkaProducer, ProducerRecord }
import play.api.Logger
import scala.collection.JavaConverters._
import scala.concurrent._

import ExecutionContext.Implicits.global

@Singleton
class Messages @Inject()(
) {
  private val _cfg: Map[String, Object] = Map(
    "key.serializer" -> "org.apache.kafka.common.serialization.StringSerializer",
    "value.serializer" -> "org.apache.kafka.common.serialization.StringSerializer",
    "client.id" -> "services-schedule",
    "bootstrap.servers" -> "localhost:9092"
  )
  private val _producer: KafkaProducer[String, String] = new KafkaProducer[String, String](
    _cfg.asJava
  )

  def deliver(doc_id: String): Future[Unit] = {
    Logger.debug("scheduling message")
    Future {
      Logger.debug("sending message")
      _producer.send(new ProducerRecord("xadf.test", doc_id))
    }
  }
}
