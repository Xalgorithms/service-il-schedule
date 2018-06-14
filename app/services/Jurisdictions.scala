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

object NormalizeCountry {
  private val country_fns = Seq[(Country) => Option[Country]](
    (c: Country) => FindCountry.by_code2(c.code2),
    (c: Country) => FindCountry.by_code2(c.name),
    (c: Country) => FindCountry.by_name(c.name),
    (c: Country) => FindCountry.by_code3(c.code3),
    (c: Country) => FindCountry.by_code3(c.name)
  )

  def apply(country: Country): Country = {
    country_fns.map { fn => fn(country) }.flatten match {
      case (c: Country) :: tail => c
      case _ => country
    }
  }
}

case class Subdivision(
  val name: String = null,
  val code: String = null,
  val geo: LatLon = null)

object FindSubdivision {
  def by_full_code(code: String): Option[Subdivision] = code match {
    case null => None
    case _ => {
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

  def by_code(country_code: String, code: String): Option[Subdivision] = country_code match {
    case null => None
    case _ => {
      FindCountry.by_code2(country_code) match {
        case Some(country) => country.subdivisions.find(code == _.code)
        case None => None
      }
    }
  }

  def by_name(country_code: String, name: String): Option[Subdivision] = country_code match {
    case null => None
    case _ => {
      FindCountry.by_code2(country_code) match {
        case Some(country) => country.subdivisions.find(name == _.name)
        case None => None
      }
    }
  }
}

object NormalizeSubdivision {
  private val subdivision_fns = Seq[(String, Subdivision) => Option[Subdivision]](
    (cc: String, s: Subdivision) => FindSubdivision.by_full_code(s.code),
    (cc: String, s: Subdivision) => FindSubdivision.by_full_code(s.name),
    (cc: String, s: Subdivision) => FindSubdivision.by_code(cc, s.code),
    (cc: String, s: Subdivision) => FindSubdivision.by_code(cc, s.name),
    (cc: String, s: Subdivision) => FindSubdivision.by_name(cc, s.name)
  )

  def apply(country_code2: String, subdivision: Subdivision): Subdivision = {
    subdivision_fns.map { fn => fn(country_code2, subdivision) }.flatten match {
      case (s: Subdivision) :: tail => s
      case _ => subdivision
    }
  }
}

case class City(val name: String)

