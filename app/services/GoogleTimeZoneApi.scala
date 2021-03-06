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

import com.softwaremill.sttp._
import com.softwaremill.sttp.akkahttp._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import scala.concurrent.{ Future, Promise }
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{ Success, Failure }

case class TimeZoneInfo(val id: String, val name: String, val dst: Int, val offset: Int)

class FailureResponseNotObject extends Throwable {
}

class FailureParseTimeZoneInfo extends Throwable {
}

class GoogleTimeZoneApi {
  implicit val time_zone_info_reads: Reads[TimeZoneInfo] = (
    (JsPath \ "timeZoneId").read[String] and
    (JsPath \ "timeZoneName").read[String] and
    (JsPath \ "dstOffset").read[Int] and
    (JsPath \ "rawOffset").read[Int]
  )(TimeZoneInfo.apply _)

  def extract_info(v: JsValue): Future[TimeZoneInfo] = v match {
    case (o: JsObject) => {
      o.validate[TimeZoneInfo] match {
        case (s: JsSuccess[TimeZoneInfo]) => Future.successful(s.get)
        case _ => Future.failed(new FailureParseTimeZoneInfo())
      }
    }
    case _ => Future.failed(new FailureResponseNotObject())
  }

  def lookup(location: LatLon): Future[TimeZoneInfo] = {
    val pr = Promise[TimeZoneInfo]
    val ts = System.currentTimeMillis() / 1000
    val key = sys.env.getOrElse("TIMEZONE_API_KEY", "")
    val uri = uri"https://maps.googleapis.com/maps/api/timezone/json?location=${location.lat},${location.lon}&timestamp=${ts}&key=${key}"

    implicit val backend = AkkaHttpBackend()

    val req = sttp.get(uri)
    req.send().onComplete {
      case Success(res) => {
        res.body match {
          case Right(body) => pr.completeWith(extract_info(Json.parse(body)))
          case Left(message) => pr.failure(new FailureRemoteError(message))
        }
      }
      case Failure(e) => pr.failure(e)
    }
    
    pr.future
  }
}
