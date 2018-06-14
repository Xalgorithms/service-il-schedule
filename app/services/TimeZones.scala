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
package services

import scala.concurrent.{ Await }
import scala.concurrent.duration._

class TimeZones(
  geocoder: Geocoder = new NominatimGeocoder,
  api: GoogleTimeZoneApi = new GoogleTimeZoneApi()
) {
  def lookup(
    country: Option[Country] = None, subdivision: Option[Subdivision] = None, city: Option[City] = None
  ): Option[String] = {
    normalize_country(country) match {
      case Some(fc) => {
        lookup_timezone_by_location(
          lookup_location(fc, normalize_subdivision(fc.code2, subdivision), city)
        )
      }
      case None => None
    }
  }

  def lookup_location(
    country: Country, opt_subdivision: Option[Subdivision], opt_city: Option[City]
  ): Option[LatLon] = opt_city match {
    case None => lookup_location_by_subdivision(opt_subdivision) match {
      case None => Option(country.geo)
      case opt => opt
    }
    case Some(city) => lookup_location_by_city(country, city)
  }

  def lookup_location_by_subdivision(
    opt_subdivision: Option[Subdivision]
  ): Option[LatLon] = opt_subdivision match {
    case Some(subdivision) => Option(subdivision.geo)
    case None => None
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

  def normalize_country(opt_country: Option[Country]): Option[Country] = opt_country match {
    case None => None
    case Some(country) => Some(NormalizeCountry(country))
  }

  def normalize_subdivision(
    country_code2: String, opt_subdivision: Option[Subdivision]
  ): Option[Subdivision] = opt_subdivision match {
    case None => None
    case Some(subdivision) => Some(NormalizeSubdivision(country_code2, subdivision))
  }
}
