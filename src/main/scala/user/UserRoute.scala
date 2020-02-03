package user

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
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
    UserDao.getUser.map {
      case Left(error) => complete(StatusCodes.BadRequest, error.reason)
      case Right(user) => complete(user)
    }.getOrElse(complete(StatusCodes.NotFound))
  }
}
