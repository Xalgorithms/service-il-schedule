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

import org.scalamock.scalatest.MockFactory
import org.scalatest._

class GeneratedSpec extends FlatSpec with Matchers with MockFactory {
  "Code generation" should "have generated some known countries" in {
    val expects = Map(
      "AM" -> Country(
        "Armenia", "AM", "ARM", "Asia", "Asia", "Western Asia", services.subdivisions.AM.ALL, LatLon("40.069099", "45.038189")
      ),
      "CA" -> Country(
        "Canada", "CA", "CAN", "North America", "Americas", "Northern America", services.subdivisions.CA.ALL, LatLon("56.130366", "-106.346771")
      ),
      "GB" -> Country(
        "United Kingdom of Great Britain and Northern Ireland", "GB", "GBR", "Europe", "Europe", "Northern Europe", services.subdivisions.GB.ALL, LatLon("55.378051", "-3.435973")
      ),
      "IT" -> Country(
        "Italy", "IT", "ITA", "Europe", "Europe", "Southern Europe", services.subdivisions.IT.ALL, LatLon("41.87194", "12.56738")
      )
    )

    expects.foreach { case (code, country) =>
      Countries.ALL(code) shouldEqual(country)
    }
  }

  it should "have generated some known subdivisions" in {
    val ca_subs = Seq(
      Subdivision("Alberta", "AB", LatLon("53.9332706", "-116.5765035")),
      Subdivision("British Columbia", "BC", LatLon("53.7266683", "-127.6476206")),
      Subdivision("Manitoba", "MB", LatLon("53.7608608", "-98.81387629999999")),
      Subdivision("New Brunswick", "NB", LatLon("46.5653163", "-66.46191639999999")),
      Subdivision("Newfoundland and Labrador", "NL", LatLon("53.1355091", "-57.6604364")),
      Subdivision("Nova Scotia", "NS", LatLon("44.68198659999999", "-63.744311")),
      Subdivision("Northwest Territories", "NT", LatLon("64.8255441", "-124.8457334")),
      Subdivision("Nunavut", "NU", LatLon("70.2997711", "-83.1075769")),
      Subdivision("Ontario", "ON", LatLon("51.253775", "-85.3232139")),
      Subdivision("Prince Edward Island", "PE", LatLon("46.510712", "-63.41681359999999")),
      Subdivision("Quebec", "QC", LatLon("52.9399159", "-73.5491361")),
      Subdivision("Saskatchewan", "SK", LatLon("52.9399159", "-106.4508639")),
      Subdivision("Yukon", "YT", LatLon("64.2823274", "-135"))
    )

    val am_subs = Seq(
      Subdivision("Aragacotn", "AG", LatLon("", "")),
      Subdivision("Ararat", "AR", LatLon("39.9753273", "44.8338058")),
      Subdivision("Armavir", "AV", LatLon("40.1315615", "43.8325355")),
      Subdivision("Erevan", "ER", LatLon("40.183333", "44.516667")),
      Subdivision("Gegark'unik'", "GR", LatLon("", "")),
      Subdivision("Kotayk'", "KT", LatLon("40.4277896", "44.6641741")),
      Subdivision("Lory", "LO", LatLon("40.969845", "44.490014")),
      Subdivision("Širak", "SH", LatLon("40.9630814", "43.8102461")),
      Subdivision("Syunik'", "SU", LatLon("39.3194392", "46.14609189999999")),
      Subdivision("Tavuš", "TV", LatLon("40.8866296", "45.339349")),
      Subdivision("Vayoc Jor", "VD", LatLon("39.8107912", "45.4967174"))
    )

    services.subdivisions.CA.ALL shouldEqual(ca_subs)
    services.subdivisions.AM.ALL shouldEqual(am_subs)
  }
}
