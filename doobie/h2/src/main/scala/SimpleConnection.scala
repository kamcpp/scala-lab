import doobie._
import doobie.implicits._

import cats._
import cats.effect._
import cats.implicits._

import scala.concurrent.ExecutionContext

object SimpleConnection extends App {

  implicit val EC = IO.contextShift(ExecutionContext.global)

  val xa = Transactor.fromDriverManager[IO](
    "org.h2.Driver",
    "jdbc:h2:~/.h2/library",
    "",
    ""
  )

  val program = for {
    a <- sql"select 45".query[Int].unique
    b <- sql"select random()".query[Double].unique
  } yield (a, b)

  println(program.transact(xa).unsafeRunSync())
}
