package services

case class Country(
  val name: String = null,
  val code2: String = null,
  val code3: String = null,
  val continent: String = null,
  val region: String = null,
  val subregion: String = null,
  val subdivisions: Seq[Subdivision] = Seq(),
  val geo: LatLon = null,
)

object FindCountry {
  def by_code2(code2: String): Option[Country] = {
    Countries.ALL.get(code2)
  }

  def by_code3(code3: String): Option[Country] = {
    Countries.ALL.values.find(code3 == _.code3)
  }

  def by_name(name: String): Option[Country] = {
    Countries.ALL.values.find(name == _.name)
  }
}

object FindSubdivision {
  def by_full_code(code: String): Option[Subdivision] = {
    code.split("-") match {
      case Array(country_code, subdivision_code) => {
        FindCountry.by_code2(country_code) match {
          case Some(country) => country.subdivisions.find(subdivision_code == _.code)
          case None => None
        }
      }
      case _ => None
    }
  }
}

case class Subdivision(
  val name: String = null,
  val code: String = null,
  val geo: LatLon = null)

case class City(val name: String)
