package domain

import Library.{BookId, Repo, _}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

object Main extends App {

  def addBook(book: Book)(implicit repo: Repo[Book, BookId, Future]) =
    repo.add(book)

  def getBooks(author: Author)(implicit authorHelper: AuthorHelper[Future]) =
    authorHelper.getBooks(author)

  def addAuthor(author: Author)(implicit repo: Repo[Author, AuthorId, Future]) =
    repo.add(author)

  def getAuthors(book: Book)(implicit bookHelper: BookHelper[Future]) =
    bookHelper.getAuthors(book)

  def getPublishedBooks(implicit publishHelper: PublishHelper[Future]) =
    publishHelper.getPublishedBooks

  implicit val EC: ExecutionContextExecutor = ExecutionContext.global

  val book1 = Book(100, "Book-100", List(10, 20))
  val book2 = Book(200, "Book-200", List(30))
  val book3 = Book(300, "Book-300", List(10, 40))

  val author1 = Author(10, "AAA", "BBB")
  val author2 = Author(20, "CCC", "DDD")
  val author3 = Author(30, "EEE", "FFF")
  val author4 = Author(40, "GGG", "HHH")

  for {
    _ <- addBook(book1)
    _ <- addBook(book2)
    _ <- addBook(book3)
    _ <- addAuthor(author1)
    _ <- addAuthor(author2)
    _ <- addAuthor(author3)
    _ <- addAuthor(author4)
  } yield ()

  getAuthors(book1).foreach(println)
  getAuthors(book2).foreach(println)

  getPublishedBooks.flatMap(
    pubs => Future.successful(pubs.foreach(_.flatMap(
      pub => Future.successful(println(pub))))))

  Thread.sleep(3000)

}
