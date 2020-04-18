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

class SelfRoute(userService: UserService[IO]) {
  val logger: Logger = Logger(classOf[SelfRoute])

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

    onSuccess(userService.registerUser(ur).value.unsafeToFuture()) {
      case Left(error: ErrorResponse) => complete(error.status, error)
      case Right(tokens: Tokens) => complete(tokens)
    }
  }

  def getSelf(request: HttpRequest): Route = {
    logger.info("GET /self")
    
    UserContext.from(request).map(u => u.uid).fold(
      e => complete(e.status, e),
      uid => onSuccess(userService.getUser(uid).value.unsafeToFuture()) {
        case Left(_: NotFoundError) => complete(StatusCodes.InternalServerError)
        case Left(e: ErrorResponse) => complete(e.status, e)
        case Right(user: User) => complete(user)
      }
    )
  }
}
