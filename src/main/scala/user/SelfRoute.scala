package user

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.{HttpRequest, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import authentication.Tokens
import cats.effect.IO
import com.typesafe.scalalogging.Logger
import request.UserContext
import response.ErrorResponse
import response.ErrorResponse.{ClientError, NotFoundError}
import cats.effect.ConcurrentEffect
import cats.effect.implicits._
import cats.implicits._
import scala.concurrent.Future
import cats.data.EitherT
import response.ErrorResponse.InternalError

class SelfRoute[F[_] : ConcurrentEffect](userService: UserService[F]) {
  val logger: Logger = Logger(classOf[SelfRoute[F]])

  val getSelfRoute: Route = path("self") {
    get {
      extractRequest { request => getSelf(request) }
    }
  }

  val postSelfRoute: Route = path("self") {
    post {
      extractRequest { request =>
        postSelf(request)
      }
    }
  }

  val route: Route = getSelfRoute ~ postSelfRoute

  def postSelf(request: HttpRequest): Route = {
    logger.info("POST /self")
    entity(as[Either[ModelValidationError, UserRegistration]]) {
      case Left(e) =>
        val clientError: ErrorResponse = ClientError(e.reason)
        complete(clientError.status, clientError)
      case Right(userRegistration) => registerUser(userRegistration)
    }
  }

  def registerUser(ur: UserRegistration): Route = {
    logger.info(f"Registering user: {firstName: ${ur.firstName}, lastName: ${ur.lastName}, email: ${ur.email}}")
    val registerUserFuture = userService.registerUser(ur)
      .value
      .toIO
      .unsafeToFuture()

    onSuccess(registerUserFuture) {
      case Left(error: ErrorResponse) => complete(error.status, error)
      case Right(tokens: Tokens) => complete(tokens)
    }
  }

  def getSelf(request: HttpRequest): Route = {
    logger.info("GET /self")

    val userEffect = for {
      ctx <- EitherT.fromEither[F](UserContext.from(request))
      user <- userService.getUser(ctx.uid).leftMap {
        case NotFoundError(e) => InternalError()
        case x => x
      }
    } yield user

    val userFuture = userEffect.value.toIO.unsafeToFuture()

    onSuccess(userFuture) {
      case Left(error: ErrorResponse) => complete(error.status, error)
      case Right(user: User) => complete(user)
    }
  }
}