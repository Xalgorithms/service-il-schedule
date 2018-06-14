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
package models

import com.outworkers.phantom.dsl._
import org.joda.time.DateTime
import scala.concurrent.Future

abstract class EnvelopesTable extends Table[EnvelopesTable, Envelope] {
  // note to self: you MUST have PartitionKey or this fails to compile
  object document_id extends StringColumn with PartitionKey
  object party       extends StringColumn
  object country     extends StringColumn
  object region      extends StringColumn
  object timezone    extends StringColumn
  object issued      extends DateTimeColumn

  override lazy val tableName = "envelopes"

  def find(document_id: String): Future[Option[Envelope]] = {
    select.where(_.document_id eqs document_id).one()
  }
}
