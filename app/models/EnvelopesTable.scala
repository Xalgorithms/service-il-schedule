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
