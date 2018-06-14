package services

import play.api.{ Logger => PlayLogger }

abstract class Logger {
  def debug(m: String)
  def error(m: String)
}

class LocalLogger extends Logger {
  def debug(m: String) = { PlayLogger.debug(m) }
  def error(m: String) = { PlayLogger.error(m) }
}
