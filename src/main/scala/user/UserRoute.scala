package user

import request.RouteDispatcher
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import cats.effect.IO
import cats.data.EitherT
import response.ErrorResponse

class UserRouteDispatcher(userService: UserService[IO]) extends RouteDispatcher {

  val userRoute: UserRoute[IO] = new UserRoute(userService)
  override val route: Route = extractRequest { request =>
    path("user") {
      get {
        parameter("email") { email =>
          handleGetUser(email)
        }
      }
    }
  }

  def handleGetUser(email: String): Route = {
    complete(email)
  }

}

class UserRoute[F[_]](userService: UserService[F]) {

  def getUser(email: String): EitherT[F, ErrorResponse, User] = ???

}
