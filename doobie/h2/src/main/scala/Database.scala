import cats.effect.IO

import doobie._
import doobie.implicits._

import scala.concurrent.ExecutionContext

object Database {

  object implicits {
    implicit val EC = ExecutionContext.global
    implicit val CS = IO.contextShift(EC)
  }
  import implicits._

  val xa = Transactor.fromDriverManager[IO](
    "org.h2.Driver",
    "jdbc:h2:~/.h2/library",
    "",
    ""
  )

  def makeUserTable = {
    sql"CREATE TABLE IF NOT EXISTS USER (id INT NOT NULL, username VARCHAR(50) NOT NULL, password VARCHAR(50) NOT NULL, ACTIVE INT)"
      .update.run.transact(Database.xa).unsafeRunSync()
  }

  def init = {
    makeUserTable
  }

}
