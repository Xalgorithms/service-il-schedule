package services

import scala.concurrent.Future

import org.scalamock.scalatest.MockFactory
import org.scalatest._

class TimeZonesSpec extends FlatSpec with Matchers with MockFactory {
  case class Case(city: City, subdivision: Subdivision, country: Country, geo: LatLon, ex: String)

  val city_cases = Seq(
    Case(
      City("Toronto"),
      Subdivision("Ontario", "CA-ON"),
      Country("Canada", "CA", "CAN"),
      LatLon("43.653963", "-79.387207"),
      "America/Toronto"
    ),
    Case(
      City("Halifax"),
      Subdivision("Nova Scotia", "CA-NS"),
      Country("Canada", "CA", "CAN"),
      LatLon("44.6486237", "-63.5859487"),
      "America/Halifax"
    ),
    Case(
      City("Newark"),
      Subdivision("New Jersey", "US-NJ"),
      Country("United States of America", "US", "USA"),
      LatLon("40.735657", "-74.1723667"),
      "America/New_York"
    ),
    Case(
      City("Liverpool"),
      Subdivision("Liverpool", "GB-LIV"),
      Country("United Kingdom of Great Britain and Northern Ireland", "GB", "GBR"),
      LatLon("53.4054719", "-2.9805392"),
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
    (geocoder.lookup _).expects(Query(query_country, tc.city)).returning(Future.successful(tc.geo))
    (api.lookup _).expects(tc.geo).returning(Future.successful(tzi))
    tzs.lookup(tc.country, tc.subdivision, tc.city) shouldEqual(Some(tc.ex))
  }

  "TimeZones" should "produce a valid timezone based on country, subdivision and city" in {
    city_cases.foreach { c =>
      // ALL information
      validate_city_timezone(c, c.country.code2)

      // just the codes
      validate_city_timezone(
        Case(
          c.city,
          Subdivision(null, c.subdivision.code),
          Country(null, c.country.code2, null),
          c.geo,
          c.ex),
        c.country.code2)

      // // allow just the codes, in the name
      validate_city_timezone(
        Case(
          c.city,
          Subdivision(c.subdivision.code, null),
          Country(c.country.code2, null, null),
          c.geo,
          c.ex),
        c.country.code2)

      // // just with the names
      validate_city_timezone(
        Case(
          c.city,
          Subdivision(c.subdivision.name, null),
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
}
