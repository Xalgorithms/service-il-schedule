package models

import com.outworkers.phantom.connectors
import com.outworkers.phantom.connectors.CassandraConnection
import com.outworkers.phantom.dsl._
import scala.concurrent.Future

class InterlibrDatabase(
  override val connector: CassandraConnection
) extends Database[InterlibrDatabase](connector) {
  object Envelopes extends EnvelopesTable with Connector

  def storeEnvelope(e: Envelope): Future[ResultSet] = {
    Envelopes.storeRecord(e)
  }
}

object ConnectedInterlibrDatabase extends InterlibrDatabase(connectors.ContactPoint.local.keySpace("xadf"))
