package user

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import request.UserContext
import response.ErrorResponse

object UserRoute {
  val route: Route = get {
    path("self") {
      extractRequest { request =>
        getSelf(request)
      }
    }
  }

  def getSelf(request: HttpRequest): Route = {
    val uid: Int = UserContext.from(request).map(u => u.uid) match {
      case Left(e) => return complete(e.status, e)
      case Right(v) => v
    }

    UserService.getUser(uid) match {
      case Left(e: ErrorResponse) => complete(e.status, e)
      case Right(user: User) => complete(user)
    }
  }
}
