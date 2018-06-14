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
