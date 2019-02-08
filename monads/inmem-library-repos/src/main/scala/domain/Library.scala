package domain

import cats.Monad
import cats.implicits._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.higherKinds

object Library {

  type AuthorId = Int
  type BookId = Int
  type PublishId = Int

  trait Entity[I] {
    def id: I
  }

  case class Author(id: AuthorId, firstName: String, secondName: String) extends Entity[AuthorId]
  case class Book(id: BookId, name: String, authorIds: List[AuthorId]) extends Entity[BookId]

  case class Publish(book: Book, authors: List[Author])

  trait Repo[E, I, F[_]] {
    def add(e: E): F[Either[String, Unit]]
    def update(e: E): F[Either[String, Unit]]
    def delete(e: E): F[Either[String, Unit]]
    def all(): F[List[E]]
    def get(id: I): F[Option[E]]
  }

  object Repo {
    def filter[E, I, F[_]: Monad](f: E => Boolean)(repo: Repo[E, I, F]): F[List[E]] = {
      repo.all().flatMap(entities =>
        implicitly[Monad[F]].pure(
          entities.filter(f)
            .foldLeft(List[E]())((acc, e) => acc ::: e :: Nil))
      )
    }
  }

  class ImpureRepo[E <: Entity[I], I, F[_]: Monad] extends Repo[E, I, F] {

    val entities = scala.collection.mutable.ArrayBuffer[E]()

    override def add(e: E): F[Either[String, Unit]] = {
      if (entities.contains(e)) {
        implicitly(Monad[F]).pure(Left("Exists!"))
      } else {
        entities += e
        implicitly(Monad[F]).pure(Right())
      }
    }

    override def update(e: E): F[Either[String, Unit]] = delete(e).flatMap {
      case Left(error) => implicitly(Monad[F]).pure(Left(error))
      case Right(()) => {
        entities += e
        implicitly(Monad[F]).pure(Right(()))
      }
    }

    override def delete(e: E): F[Either[String, Unit]] = {
      if (!entities.exists(_.id == e.id)) {
        implicitly(Monad[F]).pure(Left("Does not exist!"))
      } else {
        entities.filter(_.id == e.id).map(a => entities.remove(entities.indexOf(a)))
        implicitly(Monad[F]).pure(Right(()))
      }
    }

    override def all(): F[List[E]] =
      implicitly(Monad[F]).pure(entities.toList)

    override def get(id: I): F[Option[E]] = if (entities.exists(_.id == id)) {
      implicitly(Monad[F]).pure(Some(entities.filter(_.id == id).head))
    } else {
      implicitly(Monad[F]).pure(None)
    }
  }

  implicit val impureBookFutureRepo = new ImpureRepo[Book, BookId, Future]
  implicit val impureAuthorFutureRepo = new ImpureRepo[Author, AuthorId, Future]

  implicit val authorFutureHelper = new AuthorHelper[Future]
  implicit val bookFutureHelper = new BookHelper[Future]
  implicit val publishFutureHelper = new PublishHelper[Future]

  class BookHelper[F[_]: Monad] {
    def getAuthors(book: Book)(implicit authorRepo: Repo[Author, AuthorId, F]): F[List[Author]] = {
      book.authorIds.map(
        authorRepo.get(_).flatMap {
          case Some(author) => implicitly[Monad[F]].pure(author)
        }).foldLeft(implicitly(Monad[F]).pure(List.empty[Author]))(
          (acc, next) =>
            for {
              l <- acc
              a <- next
            } yield l ::: a :: Nil)
    }
  }

  class AuthorHelper[F[_]: Monad] {
    def getBooks(author: Author)(implicit repo: Repo[Book, BookId, F]): F[List[Book]] =
      Repo.filter[Book, BookId, F](_.authorIds.contains(author.id))(repo)
  }

  class PublishHelper[F[_]: Monad] {
    def getPublishedBooks(implicit bookRepo: Repo[Book, BookId, F],
                          authorRepo: Repo[Author, AuthorId, F],
                          bookHelper: BookHelper[F]) : F[List[F[Publish]]] =
      bookRepo.all
        .flatMap(books => implicitly(Monad[F]).pure(books.map(book => bookHelper.getAuthors(book)
            .flatMap(authors => implicitly(Monad[F]).pure(Publish(book, authors))))))
  }
}
