package services

import org.scalamock.scalatest.MockFactory
import org.scalatest._

class TimeZonesSpec extends FlatSpec with Matchers with MockFactory {
  "TimeZones" should "produce a valid timezone based on country, region and city" in {
    val cases = Seq(
      Map(
        "city" -> "Toronto",
        "region" -> Tuple2("Ontario", "CA-ON"),
        "country" -> Tuple2("Canada", "CA", "CAN"),
        "geocode" -> Tuple2("43.653963", "-79.387207"),
        "ex" -> "America/Toronto"
      ),
      Map(
        "city" -> "Halifax",
        "region" -> Tuple2("Nova Scotia", "CA-NS"),
        "country" -> Tuple2("Canada", "CA", "CAN"),
        "geocode" -> Tuple2("44.6486237", "-63.5859487"),
        "ex" -> "America/Halifax"
      ),
      Map(
        "city" -> "Newark",
        "region" -> Tuple2("New Jersey", "NJ"),
        "country" -> Tuple2("United States of America", "US", "USA"),
        "geocode" -> Tuple2("40.735657", "-74.1723667"),
        "ex" -> "America/Halifax"
      ),
      Map(
        "city" -> "Liverpool",
        "region" -> Tuple2("Liverpool", "GB-LIV"),
        "country" -> Tuple2("United Kingdom of Great Britain and Northern Ireland", "GB", "GBR"),
        "geocode" -> Tuple2("53.4054719", "-2.9805392"),
        "ex" -> "Europe/London"
      )
    )

    cases.foreach { c =>
      val args = Seq("city", "region", "country").map { k => Tuple2(k, c(k)) }.toMap
      TimeZone(args) shouldEqual(c("ex"))
    }
  }
}
