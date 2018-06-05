package services

import scala.concurrent.{ Await }
import scala.concurrent.duration._

class TimeZones(
  geocoder: Geocoder = new NominatimGeocoder,
  api: GoogleTimeZoneApi = new GoogleTimeZoneApi()
) {
  val country_fns = Seq[(Country) => Option[Country]](
    (c: Country) => FindCountry.by_code2(c.code2),
    (c: Country) => FindCountry.by_code2(c.name),
    (c: Country) => FindCountry.by_name(c.name),
    (c: Country) => FindCountry.by_code3(c.code3),
    (c: Country) => FindCountry.by_code3(c.name)
  )

  def lookup(country: Country, subdivision: Subdivision, city: City): Option[String] = {
    lookup_timezone_by_location(
      lookup_location(normalize_country(country), normalize_subdivision(subdivision), city)
    )
  }

  def lookup_location(country: Country, subdivision: Subdivision, city: City): Option[LatLon] = {
    city match {
      case null => lookup_location_by_subdivision(subdivision) match {
        case None => {
          if (null != country.geo) {
            Some(country.geo)
          } else {
            None
          }
        }
        case opt => opt
      }
      case _ => lookup_location_by_city(country, city)
    }
  }

  def lookup_location_by_subdivision(subdivision: Subdivision): Option[LatLon] = subdivision match {
    case null => None
    case _ => {
      if (null != subdivision.geo) {
        Some(subdivision.geo)
      } else {
        None
      }
    }
  }

  def lookup_location_by_city(country: Country, city: City): Option[LatLon] = {
    val fut = geocoder.lookup(Query(country, city))
    Await.result(fut, 5.seconds) match {
      case (loc: LatLon) => Some(loc)
      case _ => None
    }
  }

  def lookup_timezone_by_location(opt_loc: Option[LatLon]): Option[String] = opt_loc match {
    case Some(loc) => {
      val fut = api.lookup(loc)
      Await.result(fut, 5.seconds) match {
        case (tzi: TimeZoneInfo) => Some(tzi.id)
        case _ => None
      }
    }
    case None => None
  }

  def normalize_country(country: Country): Country = {
    country_fns.map { fn => fn(country) }.filterNot { o => o == None } match {
      case (oc: Option[Country]) :: tail => {
        oc match {
          case Some(full_country) => full_country
          case None => country
        }
      }

      case _ => country
    }
  }

  def normalize_subdivision(subdivision: Subdivision): Subdivision = {
    subdivision
  }
}
