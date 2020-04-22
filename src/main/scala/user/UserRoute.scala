package user

import request.RouteDispatcher
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Route
import cats.effect.IO
import cats.data.EitherT
import response.ErrorResponse
import com.typesafe.scalalogging.Logger

class UserRouteDispatcher(userService: UserService[IO])
    extends RouteDispatcher {

  val logger: Logger = Logger(classOf[UserRouteDispatcher])
  val userRoute: UserRoute[IO] = new UserRoute(userService)

  override val route: Route = extractRequest { request =>
    path("user") {
      get {
        parameter("email") { email => handleGetUser(email) }
      }
    }
  }

  def handleGetUser(email: String): Route = {
    logger.info(s"GET /user with email ${email}")
    val userEffect = userRoute.getUser(email)

    onSuccess(userEffect.value.unsafeToFuture()) {
      case Left(error: ErrorResponse) => complete(error.status, error)
      case Right(user: User)          => complete(user)
    }
  }

}

class UserRoute[F[_]](userService: UserService[F]) {

  def getUser(email: String): EitherT[F, ErrorResponse, User] = {
    userService.getUser(email)
  }

}
