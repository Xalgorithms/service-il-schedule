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
package actors

import play.api.libs.json._

object GlobalMessages {
  abstract class GlobalMessage
  case class Execute(rule_id: String, req_id: String, opt_doc: Option[JsObject]) extends GlobalMessage
  case class Submit(req_id: String, effective_props: Map[String, String], doc: JsObject) extends GlobalMessage
  case class VerifyEffective(req_id: String, effective_props: Map[String, String], doc: JsObject) extends GlobalMessage
  case class VerifyApplicable(req_id: String, rule_id: String, doc: JsObject) extends GlobalMessage
}
