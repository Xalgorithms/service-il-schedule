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

import org.joda.time.DateTime
import org.mongodb.scala.bson.{ BsonDocument, BsonString }

import services.{ City, Country, Subdivision, NormalizeCountry, NormalizeSubdivision, TimeZones }

class DocumentEnvelope(id: String, doc: BsonDocument, timezones: TimeZones = new TimeZones() ) {
  private val _party_keys = Seq("supplier", "customer", "payee", "buyer", "seller", "tax")
  private val _address_keys = Seq("address", "location.address")

  def rows: Seq[Envelope] = {
    // find the envelope in the document
    Document.maybe_find_document(doc, "content.envelope") match {
      case Some(env) => {
        val issued = Document.maybe_find_date_time(env, "issued").getOrElse(new DateTime())
        val parties = _party_keys.foldLeft(Map[String, Jurisdiction]()) { (m, k) =>
          maybe_extract_jurisdiction(Document.maybe_find_document(env, k)) match {
            case Some(jurisdiction) => {
              m ++ Map(k -> jurisdiction)
            }
            case None => m
          }
        }

        parties.map { case (name, jurisdiction) =>
          val full_code = s"${jurisdiction.country.code2}-${jurisdiction.region.code}"
          Envelope(id, name, jurisdiction.country.code2, full_code, jurisdiction.timezone, issued)
        }.toSeq
      }
      case None => Seq()
    }
  }

  private def maybe_extract_jurisdiction(
    opt_party_doc: Option[BsonDocument]
  ): Option[Jurisdiction] = {
    opt_party_doc.map(
      Document.maybe_find_first_document(_, _address_keys).map(
        extract_jurisdiction_from_address(_)
      )
    ).flatten
  }

  private def extract_jurisdiction_from_address(doc: BsonDocument): Jurisdiction = {
    val country = NormalizeCountry(Country(
      Document.maybe_find_text(doc, "country.name").getOrElse(null),
      Document.maybe_find_text(doc, "country.code.value").getOrElse(null)
    ))
    val region = NormalizeSubdivision(country.code2, Subdivision(
      Document.maybe_find_text(doc, "subentity.name").getOrElse(null),
      Document.maybe_find_text(doc, "subentity.code.value").getOrElse(null)
    ))

    val city = Document.maybe_find_text(doc, "city").map(City(_))
    val tz = timezones.lookup(Option(country), Option(region), city).getOrElse(null)

    Jurisdiction(country, region, tz)
  }

}

case class Jurisdiction(country: Country, region: Subdivision, timezone: String)
