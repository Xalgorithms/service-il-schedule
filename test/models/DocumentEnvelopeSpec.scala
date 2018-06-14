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
package models

import scala.io.Source
import org.bson.BsonDocument
import org.joda.time.DateTime

import services.{ City, FindCountry, FindSubdivision, TimeZones }

import org.scalamock.scalatest.MockFactory
import org.scalatest._

class DocumentEnvelopeSpec extends FlatSpec with Matchers with MockFactory {
  val document_id = "1234"

  val expects = Map(
    "0" -> Seq(
      Envelope(document_id, "supplier", "CA", "CA-ON", "America/Toronto", new DateTime("2016-11-15T01:23:04-04:00")),
      Envelope(document_id, "customer", "CA", "CA-ON", "America/Toronto", new DateTime("2016-11-15T01:23:04-04:00"))
    )
  )

  "DocumentEnvelope" should "extract all parties from a stored Document" in {
    expects.foreach { case (k, ex) =>
      val s = Source.fromFile(s"files/envelope/${k}.json").mkString
      val doc = BsonDocument.parse(s)
      val timezones = mock[TimeZones]
      val de = new DocumentEnvelope(document_id, doc, timezones)

      val country = FindCountry.by_code2("CA")
      val subdivision = FindSubdivision.by_full_code("CA-ON")
      val city = Some(City("Ottawa"))

      (timezones.lookup _)
        .expects(country, subdivision, city)
        .repeat(ex.size)
        .returning(Some("America/Toronto"))

      de.rows shouldEqual(ex)
    }
  }
}
