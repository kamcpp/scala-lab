import java.util.UUID

object Domain {

  case class User(id: UUID, username: String, password: String, active: Boolean) extends Traits.Entity

  case class Token(id: UUID, userId: UUID, value: String, active: Boolean, createTs: Long) extends Traits.Entity

  case class ActivityLog(id: UUID, userId: UUID, ts: Long, desc: String) extends Traits.Entity

}
