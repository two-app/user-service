package user

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.{HttpRequest, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.Logger
import request.UserContext
import response.ErrorResponse
import response.ErrorResponse.NotFoundError

class UserRoute(userService: UserService) {
  val logger: Logger = Logger(classOf[UserRoute])

  val route: Route = get {
    path("self") {
      extractRequest { request =>
        getSelf(request)
      }
    }
  }

  def getSelf(request: HttpRequest): Route = {
    logger.info("GET /self request.")
    val uid: Int = UserContext.from(request).map(u => u.uid) match {
      case Left(e) => return complete(e.status, e)
      case Right(v) => v
    }

    logger.info(s"Extracted requesting user ID $uid.")

    onSuccess(userService.getUser(uid)) {
      case Left(_: NotFoundError) => complete(StatusCodes.InternalServerError)
      case Left(e: ErrorResponse) => complete(e.status, e)
      case Right(user: User) => complete(user)
    }
  }
}
