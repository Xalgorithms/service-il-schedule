package services

import org.scalamock.scalatest.MockFactory
import org.scalatest._

class JurisdictionsSpec extends FlatSpec with Matchers with MockFactory {
  val countries = Seq("AM", "CA", "GB", "IT").map { code => Countries.ALL(code) }

  "FindCountry" should "find the country by code2" in {
    countries.foreach { country =>
      FindCountry.by_code2(country.code2) match {
        case Some(found) => found shouldEqual(country)
        case None => true shouldBe false
      }
    }
  }

  it should "find the country by code3" in {
    countries.foreach { country =>
      FindCountry.by_code3(country.code3) match {
        case Some(found) => found shouldEqual(country)
        case None => true shouldBe false
      }
    }
  }

  it should "find the country by name" in {
    countries.foreach { country =>
      FindCountry.by_name(country.name) match {
        case Some(found) => found shouldEqual(country)
        case None => true shouldBe false
      }
    }
  }

  it should "find the subdivision by full code" in {
    val subdivisions = Map(
      "GB-LIV" -> Subdivision("Liverpool", "LIV", LatLon("53.4083714", "-2.9915726")),
      "CA-SK"  -> Subdivision("Saskatchewan", "SK", LatLon("52.9399159", "-106.4508639")),
      "CA-NS"  -> Subdivision("Nova Scotia", "NS", LatLon("44.68198659999999", "-63.744311")),
      "IT-AG"  -> Subdivision("Agrigento", "AG", LatLon("37.3110897", "13.5765475")),
      "IT-VE"  -> Subdivision("Venezia", "VE", LatLon("45.4408474", "12.3155151"))
    )

    subdivisions.foreach { case (code, ex) =>
      FindSubdivision.by_full_code(code) match {
        case Some(ac) => ac shouldEqual(ex)
        case None => true shouldBe false
      }
    }
  }
}
