import java.util.UUID

object Traits {

  // Represents an entity which has in `id` filed
  trait Entity {
    def id: UUID
  }

  // Represent a DAO object
  trait DAO[E <: Entity, F[_]] {
    def add(e: E): F[Either[String, Unit]]

    def update(e: E): F[Either[String, Unit]]

    def delete(id: Int): F[Either[String, Unit]]

    def get(id: Int): F[Option[E]]

    def all(): F[List[E]]
  }

}
