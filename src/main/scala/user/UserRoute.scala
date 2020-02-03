package user

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import request.UserContext
import response.ErrorResponse
import spray.json.DefaultJsonProtocol._
import spray.json._

class UserRoute {
  implicit val userFormat: RootJsonFormat[User] = jsonFormat3(User.apply)

  val route: Route = get {
    path("self") {
      extractRequest { request =>
        getSelf(request)
      }
    }
  }

  def getSelf(request: HttpRequest): Route = {
    val uid: Int = UserContext(request).map(u => u.uid) match {
      case Left(e) => return respondWithError(e)
      case Right(v) => v
    }

    UserService.getUser(uid) match {
      case Left(error: ErrorResponse) => complete(error.status, error.reason)
      case Right(user: User) => complete(user)
    }
  }

  def respondWithError(e: ErrorResponse): Route = complete(e.status, e.reason)
}
