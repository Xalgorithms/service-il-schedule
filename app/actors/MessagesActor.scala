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
