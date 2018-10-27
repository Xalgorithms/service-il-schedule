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
import javax.inject._
import java.io.ByteArrayOutputStream
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import play.api.libs.json._
import scala.util.Properties

object MessagesActor {
  def props = Props[MessagesActor]
}

class MessagesActor extends Actor with ActorLogging {
  implicit val materializer = ActorMaterializer()

  private val broker = Properties.envOrElse("KAFKA_BROKER", "kafka:9092")
  log.info(s"creating kafka settings (broker=${broker})")

  private val settings = ProducerSettings(
    context.system, new StringSerializer, new StringSerializer
  ).withBootstrapServers(broker)

  private val _source = Source.queue[(String, JsValue)](5, OverflowStrategy.backpressure)
  private val _flow_record = Flow[(String, JsValue)].map { case (topic, payload) =>
    new ProducerRecord[String, String](topic, payload.toString)
  }

  log.info("setting up stream")
  val _triggers = _source.via(_flow_record).to(Producer.plainSink(settings)).run()

  def receive = {
    case GlobalMessages.Execute(rule_id, req_id, opt_doc) => {
      log.debug(s"execute (rule_id=${rule_id}; req_id=${req_id})")
      val args = Json.obj(
        "rule_id"    -> rule_id,
        "request_id" -> req_id
      ) ++ (opt_doc match {
        case Some(doc) => Json.obj("context" -> doc)
        case None => Json.obj()
      })

      val o = Json.obj(
        "context" -> Map("task" -> "triggers", "action" -> "execute_rule"),
        "args"    -> args
      )
      _triggers.offer(("il.compute.execute", o))
    }

    case GlobalMessages.Submit(req_id, effective_props, doc) => {
      val o = Json.obj(
        "context" -> Map("task" -> "triggers", "action" -> "submit_document"),
        "args"    -> Json.obj(
          "request_id"           -> req_id,
          "effective_properties" -> effective_props,
          "document"             -> doc
        )
      )
      _triggers.offer(("il.compute.documents", o))
    }

    case GlobalMessages.VerifyEffective(req_id, effective_props, doc) => {
      val o = Json.obj(
        "context" -> Map("task" -> "triggers", "action" -> "verify_effective"),
        "args"    -> Json.obj(
          "request_id"           -> req_id,
          "effective_properties" -> effective_props,
          "document"             -> doc
        )
      )
      _triggers.offer(("il.verify.effective", o))
    }

    case GlobalMessages.VerifyApplicable(req_id, rule_id, doc) => {
      val o = Json.obj(
        "context" -> Map("task" -> "triggers", "action" -> "verify_applicable"),
        "args"    -> Json.obj(
          "request_id" -> req_id,
          "rule_id"    -> rule_id,
          "document"   -> doc
        )
      )
      log.debug("here")
      _triggers.offer(("il.verify.applicable", o))
    }
  }
}
