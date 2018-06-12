package models

import org.joda.time.DateTime
import org.mongodb.scala.bson.{ BsonDocument, BsonString }

object Envelope {
  val party_keys = Seq("supplier", "customer", "payee", "buyer", "seller", "tax")

  def extract_location_from_address(doc: BsonDocument): Tuple2[String, String] = {
    Tuple2("", "")
  }

  def apply_timezone(loc: Tuple2[String, String]): Tuple3[String, String, String] = {
    Tuple3(loc._1, loc._2, null)
  }

  def maybe_extract_location(
    opt_party_doc: Option[BsonDocument]
  ): Option[Tuple3[String, String, String]] = opt_party_doc match {
    case Some(party_doc) => {
      val keys = Seq("address", "location.address")
      val addresses = keys.map { k => Document.maybe_find_document(party_doc, k) }.flatten
      addresses.size match {
        case 0 => None
        case _ => Some(apply_timezone(extract_location_from_address(addresses.head)))
      }
    }
    case None => None
  }

  def from_document(id: String, doc: BsonDocument): Seq[Envelope] = {
    // find the envelope in the document
    Document.maybe_find_document(doc, "content.envelope") match {
      case Some(env) => {
        val issued = Document.maybe_find_date_time(env, "issued").getOrElse(new DateTime())
        val parties = party_keys.foldLeft(Map[String, Tuple3[String, String, String]]()) { (m, k) =>
          maybe_extract_location(Document.maybe_find_document(env, k)) match {
            case Some(location) => {
              m ++ Map(k -> location)
            }
            case None => m
          }
        }

        parties.map { case (name, location) =>
          Envelope(id, name, location._1, location._2, location._3, issued)
        }.toSeq
      }
      case None => Seq()
    }
  }
}

case class Location(country: String, region: String, timezone: String)

case class Envelope(
  document_id: String,
  party: String,
  country: String,
  region: String,
  timezone: String,
  issued: DateTime
)

