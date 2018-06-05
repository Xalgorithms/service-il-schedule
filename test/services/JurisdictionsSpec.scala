package services

import org.scalamock.scalatest.MockFactory
import org.scalatest._

class JurisdictionsSpec extends FlatSpec with Matchers with MockFactory {
  val countries = Seq("AM", "CA", "GB", "IT").map { code => Countries.ALL(code) }

  "FindCountry" should "find the country by code2" in {
    FindCountry.by_code2(null) shouldEqual(None)
    countries.foreach { country =>
      FindCountry.by_code2(country.code2) shouldEqual(Some(country))
    }
  }

  it should "find the country by code3" in {
    FindCountry.by_code3(null) shouldEqual(None)
    countries.foreach { country =>
      FindCountry.by_code3(country.code3) shouldEqual(Some(country))
    }
  }

  it should "find the country by name" in {
    FindCountry.by_name(null) shouldEqual(None)
    countries.foreach { country =>
      FindCountry.by_name(country.name) shouldEqual(Some(country))
    }
  }

  "FindSubdivision" should "find the subdivision by full code" in {
    val subdivisions = Map(
      "GB-LIV" -> Subdivision("Liverpool", "LIV", LatLon("53.4083714", "-2.9915726")),
      "CA-SK"  -> Subdivision("Saskatchewan", "SK", LatLon("52.9399159", "-106.4508639")),
      "CA-NS"  -> Subdivision("Nova Scotia", "NS", LatLon("44.68198659999999", "-63.744311")),
      "IT-AG"  -> Subdivision("Agrigento", "AG", LatLon("37.3110897", "13.5765475")),
      "IT-VE"  -> Subdivision("Venezia", "VE", LatLon("45.4408474", "12.3155151"))
    )

    FindSubdivision.by_full_code(null) shouldEqual(None)
    FindSubdivision.by_full_code("LIV") shouldEqual(None)
    subdivisions.foreach { case (code, ex) =>
      FindSubdivision.by_full_code(code) shouldEqual(Some(ex))
    }
  }

  it should "find the subdivision by country code and subdivision name" in {
    val subdivisions = Map(
      "Liverpool"    -> Tuple2("GB", Subdivision("Liverpool", "LIV", LatLon("53.4083714", "-2.9915726"))),
      "Saskatchewan" -> Tuple2("CA", Subdivision("Saskatchewan", "SK", LatLon("52.9399159", "-106.4508639"))),
      "Nova Scotia"  -> Tuple2("CA", Subdivision("Nova Scotia", "NS", LatLon("44.68198659999999", "-63.744311"))),
      "Agrigento"    -> Tuple2("IT", Subdivision("Agrigento", "AG", LatLon("37.3110897", "13.5765475"))),
      "Venezia"      -> Tuple2("IT", Subdivision("Venezia", "VE", LatLon("45.4408474", "12.3155151")))
    )

    FindSubdivision.by_name(null, null) shouldEqual(None)
    subdivisions.foreach { case (name, tup) =>
      FindSubdivision.by_name(tup._1, name) shouldEqual(Some(tup._2))
      FindSubdivision.by_name(null, name) shouldEqual(None)
    }
  }
}
