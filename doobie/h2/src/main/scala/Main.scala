import java.util.UUID

import Database.implicits._
import DAO.UserDAO
import Domain.User
import cats.effect.IO

import scala.concurrent.Future

object Main extends App {

  Database.init

  println(">>>")

  val userDAO = new UserDAO
  userDAO.add(User(UUID.randomUUID(), "admin", "admin", active = true)).flatMap {
    case Left(error) => Future.successful(IO { println(error) })
    case Right(()) => Future.successful(IO { println("Row was inserted.") })
  }.foreach(_.unsafeRunSync)

  userDAO.all().flatMap(users => Future.successful(IO { users.foreach(println) }))
    .foreach(_.unsafeRunSync())

}
