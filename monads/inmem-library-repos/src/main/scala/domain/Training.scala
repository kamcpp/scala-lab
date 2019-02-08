package domain

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

object Training extends App {

  def removeEffect(eList : List[Option[Int]]): List[Int] =
    eList.flatMap {
      case None => List()
      case Some(a) => List(a)
    }

  val list : List[Option[Int]] = List(Some(1), None, Some(2), Some(3), None)
  println(removeEffect(list))

  implicit val EC: ExecutionContextExecutor = ExecutionContext.global

  val fList: List[Future[Int]] =
    List(Future.failed[Int](new Exception()), Future.successful(1), Future.successful(2))

  val f2List = fList.map(_.map(Some(_)))
  val f3List: Future[List[Int]] = fList.map(_.map(Some(_)).recover { case _ => None })
      .foldLeft(Future.successful(List.empty[Int]))(
        (acc, next) => next.zip(acc).map {
          case (Some(n), acc) => acc ::: n :: Nil
          case (_, acc) => acc
        })

  println(fList)
  println(f2List)
  println(f3List)

}
