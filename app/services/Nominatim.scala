package services

import com.softwaremill.sttp._
import com.softwaremill.sttp.akkahttp._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import scala.concurrent.{ Future, Promise }
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{ Success, Failure }

class FailureParseLatLon extends Throwable {
}

class FailureResponseNotArray extends Throwable {
}

class FailureRemoteError(val message: String) extends Throwable {
}

class NominatimGeocoder extends Geocoder {
  implicit val lat_lon_reads: Reads[LatLon] = (
    (JsPath \ "lat").read[String] and
    (JsPath \ "lon").read[String]
  )(LatLon.apply _)
  
  def extract_coords(v: JsValue): Future[LatLon] = v match {
    case (a: JsArray) => {
      a.value.head.validate[LatLon] match {
        case (s: JsSuccess[LatLon]) => Future.successful(s.get)
        case _ => Future.failed(new FailureParseLatLon())
      }
    }
    case _ => Future.failed(new FailureResponseNotArray())
  }

  def lookup(q: Query): Future[LatLon] = {
    val countrycodes = Seq(q.country.code2, q.country.code3).mkString(",")
    val req = sttp.get(uri"https://nominatim.openstreetmap.org/search/?city=${q.city.name}&countrycodes=${countrycodes}&format=json")
    val pr = Promise[LatLon]()

    implicit val backend = AkkaHttpBackend()

    req.send().onComplete {
      case Success(res) => {
        res.body match {
          case Right(body) => pr.completeWith(extract_coords(Json.parse(body)))
          case Left(message) => pr.failure(new FailureRemoteError(message))
        }
      }
      case Failure(e) => pr.failure(e)
    }
    pr.future
  }
}
