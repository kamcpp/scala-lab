import doobie._
import doobie.implicits._

import Database.implicits._

import Domain.User

import scala.concurrent.Future

object DAO {

  class UserDAO extends Traits.DAO[User, Future] {
    override def add(user: User): Future[Either[String, Unit]] =
      sql"SELECT COUNT(*) FROM USER WHERE username = ${user.username}"
        .query[Int].unique.transact(Database.xa).unsafeToFuture().flatMap {
          case 0 => sql"INSERT INTO USER (id, username, password, active) VALUES (${user.id.toString}, ${user.username}, ${user.password}, 1)"
            .update.run.transact(Database.xa).unsafeToFuture().flatMap(_ => Future.successful(Right()))
          case _ => Future.successful(Left("A user with this username exists!"))
        }

    override def update(e: User): Future[Either[String, Unit]] = ???

    override def delete(id: Int): Future[Either[String, Unit]] = ???

    override def get(id: Int): Future[Option[User]] = ???

    override def all: Future[List[User]] =
      sql"SELECT id, username, password, active FROM USER".query[User].list.transact(Database.xa).unsafeToFuture.flatMap(user => Future.successful(user))
  }
}
