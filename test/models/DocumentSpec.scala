package models

import scala.io.Source
import org.bson._
import org.joda.time.DateTime
import play.api.libs.json._

import org.scalamock.scalatest.MockFactory
import org.scalatest._

import models._

class DocumentSpec extends FlatSpec with Matchers with MockFactory {
  val doc = BsonDocument.parse(Source.fromFile(s"files/document/0.json").mkString)

  "Document" should "find text elements" in {
    val expects = Map(
      "a" -> Some("AA"),
      "b" -> Some("BB"),
      "c" -> None,
      "c.x.p" -> Some("PP")
    )

    expects.foreach { case (k, ex) =>
      Document.maybe_find_text(doc, k) shouldEqual(ex)
    }
  }

  it should "find many text elements" in {
    val expects = Seq(
      Tuple2(Seq("a", "b"), Seq("AA", "BB")),
      Tuple2(Seq("a", "c"), Seq("AA")),
      Tuple2(Seq("a", "s", "b", "t", "z"), Seq("AA", "BB")),
      Tuple2(Seq("c.x.p", "c.x.q"), Seq("PP", "QQ"))
    )

    expects.foreach { tup =>
      Document.maybe_find_many_text(doc, tup._1) shouldEqual(tup._2)
    }
  }

  it should "find the first of many text elements" in {
    val expects = Seq(
      Tuple2(Seq("a", "b"), Some("AA")),
      Tuple2(Seq("c", "a"), Some("AA")),
      Tuple2(Seq("b", "s", "a", "t", "z"), Some("BB")),
      Tuple2(Seq("c.x.p", "c.x.q"), Some("PP"))
    )

    expects.foreach { tup =>
      Document.maybe_find_first_text(doc, tup._1) shouldEqual(tup._2)
    }
  }

  it should "find document elements" in {
    val expects = Map(
      "a" -> None,
      "b" -> None,
      "c" -> Option(doc.getDocument("c", null)),
      "c.x" -> Option(doc.getDocument("c", null).getDocument("x", null)),
      "c.z" -> None
    )

    expects.foreach { case (k, ex) =>
      Document.maybe_find_document(doc, k) shouldEqual(ex)
    }
  }

  it should "find many document elements" in {
    val expects = Seq(
      Tuple2(Seq("a", "b"), Seq()),
      Tuple2(Seq("a", "c"), Seq(doc.getDocument("c", null))),
      Tuple2(Seq("a", "s", "c", "f"), Seq(doc.getDocument("c", null), doc.getDocument("f", null))),
      Tuple2(Seq("c.x", "c.y"), Seq(doc.getDocument("c", null).getDocument("x", null)))
    )

    expects.foreach { tup =>
      Document.maybe_find_many_document(doc, tup._1) shouldEqual(tup._2)
    }
  }

  it should "find the first of many document elements" in {
    val expects = Seq(
      Tuple2(Seq("a", "b"), None),
      Tuple2(Seq("a", "c"), Some(doc.getDocument("c", null))),
      Tuple2(Seq("a", "s", "c", "f"), Some(doc.getDocument("c", null))),
      Tuple2(Seq("c.x", "c.y"), Some(doc.getDocument("c", null).getDocument("x", null)))
    )

    expects.foreach { tup =>
      Document.maybe_find_first_document(doc, tup._1) shouldEqual(tup._2)
    }
  }

  it should "find date time elements (as strings)" in {
    val expects = Map(
      "a" -> None,
      "b" -> None,
      "c" -> None,
      "d" -> Some(new DateTime("2018-06-12T11:24:47Z")),
      "e" -> Some(new DateTime("2018-06-12T11:24:58Z"))
    )

    expects.foreach { case (k, ex) =>
      Document.maybe_find_date_time(doc, k) shouldEqual(ex)
    }

    val dt = new DateTime()
    val ndoc = new BsonDocument()
    val secs = dt.getMillis / 1000

    ndoc.append("a", new BsonTimestamp(secs.toInt, 0))
    ndoc.append("b", new BsonDateTime(dt.getMillis))


    Document.maybe_find_date_time(ndoc, "a") shouldEqual(Some(new DateTime(secs * 1000)))
    Document.maybe_find_date_time(ndoc, "b") shouldEqual(Some(dt))
  }
}
