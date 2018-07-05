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
package actors

import akka.actor._
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.{ Producer }
import akka.stream.{ ActorMaterializer, OverflowStrategy }
import akka.stream.scaladsl.{ Flow, Source }
import com.sksamuel.avro4s.{ AvroOutputStream }
import javax.inject._
import java.io.ByteArrayOutputStream
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import scala.util.Properties

object MessagesActor {
  def props = Props[MessagesActor]
}

object Triggers {
  abstract class Trigger
  case class TriggerById(id: String) extends Trigger

  case class InvokeTrigger(topic: String, trigger: Trigger)
}

class MessagesActor extends Actor with ActorLogging {
  implicit val materializer = ActorMaterializer()

  import Triggers._

  private val settings = ProducerSettings(
    context.system, new StringSerializer, new StringSerializer
  ).withBootstrapServers(Properties.envOrElse("KAFKA_BROKER", "localhost:9092"))

  private val _source = Source.queue[InvokeTrigger](5, OverflowStrategy.backpressure)
  private val _flow_avro = Flow[InvokeTrigger].map { o =>
    val os = new ByteArrayOutputStream()

    o.trigger match {
      case (tr: TriggerById) => {
        val avo = AvroOutputStream.json[TriggerById](os)
        avo.write(tr)
        avo.close()
      }
    }

    val rv = os.toString
    os.close

    (o.topic, rv)
  }
  private val _flow_record = Flow[(String, String)].map { case (topic, payload) =>
    new ProducerRecord[String, String](topic, payload)
  }

  val _triggers = _source.via(_flow_avro).via(_flow_record).to(Producer.plainSink(settings)).run()

  def receive = {
    case GlobalMessages.DocumentAdded(id) => {
      send(id, "il.compute.execute")
    }

    case GlobalMessages.TestRunAdded(id) => {
      send(id, "il.verify.rule_execution")
    }
  }

  private def send(id: String, topic: String) = {
    log.debug(s"# sending message (topic=${topic}; id=${id})")
    _triggers.offer(InvokeTrigger(topic, TriggerById(id)))
    log.debug(s"> sent message (topic=${topic}; id=${id})")
  }
}
