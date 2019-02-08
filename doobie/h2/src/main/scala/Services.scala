
object Services {

  case class User(username: String, password: String)

  case class Credential(username: String, password: String)

  case class Token(value: String)

  trait Authenticate[F[_]] {
    def signUp(user: User): F[Either[String, Unit]]
    def authenticate(credential: Credential): F[Either[String, Token]]
    def logOut(username: String): F[Unit]
    def checkToken(token: Token): F[Either[String, Unit]]
    def disableToken(token: Token): F[Unit]
  }

}