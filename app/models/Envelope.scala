package models

import org.joda.time.DateTime

case class Envelope(
  document_id: String,
  party: String,
  country: String,
  region: String,
  timezone: String,
  issued: DateTime
)

