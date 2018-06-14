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

  it should "find the subdivision by country code and subdivision code" in {
    val subdivisions = Map(
      "LIV" -> Tuple2("GB", Subdivision("Liverpool", "LIV", LatLon("53.4083714", "-2.9915726"))),
      "SK"  -> Tuple2("CA", Subdivision("Saskatchewan", "SK", LatLon("52.9399159", "-106.4508639"))),
      "NS"  -> Tuple2("CA", Subdivision("Nova Scotia", "NS", LatLon("44.68198659999999", "-63.744311"))),
      "AG"  -> Tuple2("IT", Subdivision("Agrigento", "AG", LatLon("37.3110897", "13.5765475"))),
      "VE"  -> Tuple2("IT", Subdivision("Venezia", "VE", LatLon("45.4408474", "12.3155151")))
    )

    FindSubdivision.by_code(null, null) shouldEqual(None)
    subdivisions.foreach { case (code, tup) =>
      FindSubdivision.by_code(tup._1, code) shouldEqual(Some(tup._2))
      FindSubdivision.by_code(null, code) shouldEqual(None)
    }
  }

  "NormalizeCountry" should "produce a fully populated Country based on sparse data" in {
    val ca = FindCountry.by_code2("CA").getOrElse(null)

    ca should not be null

    val expects = Seq(
      // just code2
      Tuple2(Country(null, "CA"), ca),
      // code2 in the name
      Tuple2(Country("CA"), ca),
      // just name
      Tuple2(Country("Canada"), ca),
      // just code3
      Tuple2(Country(null, null, "CAN"), ca),
      // code3 in the name
      Tuple2(Country("CAN"), ca),
      // a country we won't find
      Tuple2(Country("Foo"), Country("Foo"))
    )

    expects.foreach { case (c, ex) =>
      NormalizeCountry(c) shouldEqual(ex)
    }
  }


  "NormalizeSubdivision" should "produce a fully populated Subdivision based on sparse data" in {
    val ca_on = FindSubdivision.by_full_code("CA-ON").getOrElse(null)
    val ca_ns = FindSubdivision.by_full_code("CA-NS").getOrElse(null)

    ca_on should not be null
    ca_ns should not be null

    val expects = Seq(
      // name
      Tuple2(Subdivision("Ontario"), ca_on),
      Tuple2(Subdivision("Nova Scotia"), ca_ns),
      // full code in name
      Tuple2(Subdivision("CA-ON"), ca_on),
      Tuple2(Subdivision("CA-NS"), ca_ns),
      // full code
      Tuple2(Subdivision(null, "CA-ON"), ca_on),
      Tuple2(Subdivision(null, "CA-NS"), ca_ns),
      // code in name
      Tuple2(Subdivision("ON"), ca_on),
      Tuple2(Subdivision("NS"), ca_ns),
      // code
      Tuple2(Subdivision(null, "ON"), ca_on),
      Tuple2(Subdivision(null, "NS"), ca_ns),
      // something we won't find in Canada
      Tuple2(Subdivision("Delaware"), Subdivision("Delaware"))
    )

    expects.foreach { case (st, ex) =>
      NormalizeSubdivision("CA", st) shouldEqual(ex)
    }
  }
}
