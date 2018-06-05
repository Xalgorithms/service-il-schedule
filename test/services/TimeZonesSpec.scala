package services

import scala.concurrent.Future

import org.scalamock.scalatest.MockFactory
import org.scalatest._

class TimeZonesSpec extends FlatSpec with Matchers with MockFactory {
  case class Case(
    city: Option[City], subdivision: Option[Subdivision], country: Country, geo: LatLon, ex: String)

  val city_cases = Seq(
    Case(
      Some(City("Toronto")),
      Some(Subdivision("Ontario", "CA-ON")),
      Country("Canada", "CA", "CAN"),
      LatLon("43.653963", "-79.387207"),
      "America/Toronto"
    ),
    Case(
      Some(City("Halifax")),
      Some(Subdivision("Nova Scotia", "CA-NS")),
      Country("Canada", "CA", "CAN"),
      LatLon("44.6486237", "-63.5859487"),
      "America/Halifax"
    ),
    Case(
      Some(City("Newark")),
      Some(Subdivision("New Jersey", "US-NJ")),
      Country("United States of America", "US", "USA"),
      LatLon("40.735657", "-74.1723667"),
      "America/New_York"
    ),
    Case(
      Some(City("Liverpool")),
      Some(Subdivision("Liverpool", "GB-LIV")),
      Country("United Kingdom of Great Britain and Northern Ireland", "GB", "GBR"),
      LatLon("53.4054719", "-2.9805392"),
      "Europe/London"
    )
  )

  val subdivision_cases = Seq(
    Case(
      None,
      Some(Subdivision("Ontario", "CA-ON")),
      Country("Canada", "CA", "CAN"),
      null,
      "America/Toronto"
    ),
    Case(
      None,
      Some(Subdivision("Nova Scotia", "CA-NS")),
      Country("Canada", "CA", "CAN"),
      null,
      "America/Halifax"
    ),
    Case(
      None,
      Some(Subdivision("New Jersey", "US-NJ")),
      Country("United States of America", "US", "USA"),
      null,
      "America/New_York"
    ),
    Case(
      None,
      Some(Subdivision("Liverpool", "GB-LIV")),
      Country("United Kingdom of Great Britain and Northern Ireland", "GB", "GBR"),
      null,
      "Europe/London"
    )
  )

  def validate_city_timezone(tc: Case, country_code: String) {
    val geocoder = mock[Geocoder]
    val api = mock[GoogleTimeZoneApi]
    val tzs = new TimeZones(geocoder, api)
    val tzi = TimeZoneInfo(tc.ex, "", 0, 0)
    val query_country = Countries.ALL(country_code)

    // ALL information
    (geocoder.lookup _).expects(Query(query_country, tc.city.get)).returning(Future.successful(tc.geo))
    (api.lookup _).expects(tc.geo).returning(Future.successful(tzi))
    tzs.lookup(Some(tc.country), tc.subdivision, tc.city) shouldEqual(Some(tc.ex))
  }

  def validate_subdivision_timezone(tc: Case, subdivision_code: String) {
    val geocoder = mock[Geocoder]
    val api = mock[GoogleTimeZoneApi]
    val tzs = new TimeZones(geocoder, api)
    val tzi = TimeZoneInfo(tc.ex, "", 0, 0)

    FindSubdivision.by_full_code(subdivision_code) match {
      case Some(full) => {
        (api.lookup _).expects(full.geo).returning(Future.successful(tzi))
        tzs.lookup(Option(tc.country), tc.subdivision, tc.city) shouldEqual(Some(tc.ex))
      }
      case None => true shouldBe(false)
    }
  }

  def validate_country_timezone(tc: Case, country_code2: String) {
    val geocoder = mock[Geocoder]
    val api = mock[GoogleTimeZoneApi]
    val tzs = new TimeZones(geocoder, api)
    val tzi = TimeZoneInfo(tc.ex, "", 0, 0)

    FindCountry.by_code2(country_code2) match {
      case Some(full) => {
        (api.lookup _).expects(full.geo).returning(Future.successful(tzi))
        tzs.lookup(Some(tc.country), tc.subdivision, tc.city) shouldEqual(Some(tc.ex))
      }
      case None => true shouldBe(false)
    }
  }

  "TimeZones" should "produce a valid timezone based on country, subdivision and city" in {
    city_cases.foreach { c =>
      // ALL information
      validate_city_timezone(c, c.country.code2)

      // just the codes
      validate_city_timezone(
        Case(
          c.city,
          Some(Subdivision(null, c.subdivision.get.code)),
          Country(null, c.country.code2, null),
          c.geo,
          c.ex),
        c.country.code2)

      // // allow just the codes, in the name
      validate_city_timezone(
        Case(
          c.city,
          Some(Subdivision(c.subdivision.get.code, null)),
          Country(c.country.code2, null, null),
          c.geo,
          c.ex),
        c.country.code2)

      // // just with the names
      validate_city_timezone(
        Case(
          c.city,
          Some(Subdivision(c.subdivision.get.name, null)),
          Country(c.country.name, null, null),
          c.geo,
          c.ex),
        c.country.code2)

      // // try code3 for country
      validate_city_timezone(
        Case(
          c.city,
          c.subdivision,
          Country(null, null, c.country.code3),
          c.geo,
          c.ex),
        c.country.code2)

      // // try code3 for country, in the name
      validate_city_timezone(
        Case(
          c.city,
          c.subdivision,
          Country(c.country.code3, null, null),
          c.geo,
          c.ex),
        c.country.code2)
    }
  }

  it should "produce a valid timezone based on country and subdivision" in {
    subdivision_cases.foreach { c =>
      val code = c.subdivision.get.code
      validate_subdivision_timezone(c, code)

      // just the codes
      validate_subdivision_timezone(
        Case(
          c.city,
          Some(Subdivision(null, c.subdivision.get.code)),
          Country(null, c.country.code2, null),
          c.geo,
          c.ex),
        code)

      // just the codes, in the name
      validate_subdivision_timezone(
        Case(
          c.city,
          Some(Subdivision(c.subdivision.get.code)),
          Country(c.country.code2),
          c.geo,
          c.ex),
        code)

      // only a country name
      validate_subdivision_timezone(
        Case(
          c.city,
          c.subdivision,
          Country(c.country.name),
          c.geo,
          c.ex),
        code)

      // only a subdivision name
      validate_subdivision_timezone(
        Case(
          c.city,
          Some(Subdivision(c.subdivision.get.name)),
          c.country,
          c.geo,
          c.ex),
        code)

      // country alpha3
      validate_subdivision_timezone(
        Case(
          c.city,
          c.subdivision,
          Country(c.country.code3),
          c.geo,
          c.ex),
        code)
      validate_subdivision_timezone(
        Case(
          c.city,
          c.subdivision,
          Country(null, null, c.country.code3),
          c.geo,
          c.ex),
        code)
    }
  }

  it should "produce nothing if there is nothing" in {
    val geocoder = mock[Geocoder]
    val api = mock[GoogleTimeZoneApi]
    val tzs = new TimeZones(geocoder, api)

    tzs.lookup(None, None, None) shouldEqual(None)
  }
}
