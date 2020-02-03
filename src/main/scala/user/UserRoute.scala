package user

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import response.ErrorResponse
import spray.json.DefaultJsonProtocol._
import spray.json._

class UserRoute {
  implicit val userFormat: RootJsonFormat[User] = jsonFormat3(User.apply)

  val route: Route = get {
    path("self") {
      getSelf
    }
  }

  def getSelf: Route = {
    UserService.getUser(1) match {
      case Left(error: ErrorResponse) => complete(error.status, error.reason)
      case Right(user: User) => complete(user)
    }
  }
}
