package actors

object GlobalMessages {
  abstract class GlobalMessage
  case class DocumentAdded(id: String) extends GlobalMessage
}
