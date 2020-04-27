package user

import request.RouteDispatcher
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Route
import cats.effect.IO
import cats.data.EitherT
import response.ErrorResponse
import com.typesafe.scalalogging.Logger
import cats.effect.ConcurrentEffect
import cats.effect.implicits._

class UserRouteDispatcher[F[_]: ConcurrentEffect](userService: UserService[F])
    extends RouteDispatcher {

  val logger: Logger = Logger[UserRouteDispatcher[F]]
  val userRoute: UserRoute[F] = new UserRoute(userService)

  override val route: Route = extractRequest { request =>
    path("user") {
      get {
        concat(
          parameter("email") { email => handleGetUser(email) },
          parameter("uid".as[Int]) { uid => handleGetUser(uid) }
        )
      }
    }
  }

  def handleGetUser(email: String): Route = {
    logger.info(s"GET /user with email ${email}")
    val userFuture = userRoute.getUser(email)
      .value
      .toIO
      .unsafeToFuture()

    onSuccess(userFuture) {
      case Left(error: ErrorResponse) => complete(error.status, error)
      case Right(user: User)          => complete(user)
    }
  }

  def handleGetUser(uid: Int): Route = {
    logger.info(s"GET /user with UID ${uid}")
    val userFuture = userRoute.getUser(uid)
      .value
      .toIO
      .unsafeToFuture()

    onSuccess(userFuture) {
      case Left(error: ErrorResponse) => complete(error.status, error)
      case Right(user: User)          => complete(user)
    }
  }

}

class UserRoute[F[_]](userService: UserService[F]) {

  def getUser(email: String): EitherT[F, ErrorResponse, User] = {
    userService.getUser(email)
  }

  def getUser(uid: Int): EitherT[F, ErrorResponse, User] = {
    userService.getUser(uid)
  }

}
