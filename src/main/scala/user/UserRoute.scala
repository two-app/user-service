package user

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.{HttpRequest, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.Logger
import request.UserContext
import response.ErrorResponse
import response.ErrorResponse.{ClientError, NotFoundError}

class UserRoute(userService: UserService) {
  val logger: Logger = Logger(classOf[UserRoute])

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
        logger.info(s"User Registration validation failed: ${e.reason}")
        val clientError: ErrorResponse = ClientError(e.reason)
        complete(clientError.status, clientError)
      case Right(userRegistration) => registerUser(userRegistration)
    }
  }

  def registerUser(ur: UserRegistration): Route = {
    logger.info("Registering user: {firstName: ${ur.firstName}, lastName: ${ur.lastName}, email: ${ur.email}}")
    onSuccess(userService.registerUser(ur)) {
      case Left(e: ErrorResponse) => complete(e.status, e)
      case Right(uid: Int) => complete(uid.toString)
    }
  }

  def getSelf(request: HttpRequest): Route = {
    logger.info("GET /self")
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
