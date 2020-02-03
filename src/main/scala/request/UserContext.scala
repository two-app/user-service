package request

import akka.http.scaladsl.model.HttpRequest
import pdi.jwt.{Jwt, JwtClaim, JwtOptions}
import response.ErrorResponse
import response.ErrorResponse.AuthorizationError

import scala.util.{Failure, Success}
import spray.json._
import DefaultJsonProtocol._

case class UserContext(uid: Int, pid: Option[Int], cid: Option[Int], connectCode: Option[String])

object UserContext {

  implicit val f: RootJsonFormat[UserContext] = jsonFormat4(UserContext.apply)

  def from(request: HttpRequest): Either[ErrorResponse, UserContext] = {
    val token: String = extractToken(request) match {
      case Left(e) => return Left(e)
      case Right(token) => token
    }

    val jwt: JwtClaim = Jwt.decode(token, JwtOptions(signature = false, expiration = false, notBefore = false)) match {
      case Failure(_) => return Left(AuthorizationError("Invalid token format."))
      case Success(v) => v
    }

    Right(jwt.content.parseJson.convertTo[UserContext])
  }

  private def extractToken(request: HttpRequest): Either[AuthorizationError, String] = {
    val header: String = request.headers
      .find(h => h.lowercaseName() == "authorization")
      .getOrElse(return Left(AuthorizationError("Authorization not provided.")))
      .value()

    if (header.isEmpty) return Left(AuthorizationError("Authorization not provided."))

    val parts = header.split(" ")
    if (parts.length != 2 || parts(0) != "Bearer") return Left(AuthorizationError("Invalid header format."))

    Right(parts(1))
  }
}
