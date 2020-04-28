package request
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import cats.data.EitherT
import response.ErrorResponse
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.marshalling._
import akka.http.scaladsl.model._
import cats.effect.implicits._
import cats.effect.ConcurrentEffect
import com.typesafe.scalalogging.Logger

trait RouteDispatcher {
  def route: Route

  def completeEffectfulEither[F[_]: ConcurrentEffect, A](
      either: EitherT[F, ErrorResponse, A]
  )(
      implicit marshaller: ToResponseMarshaller[A],
      logger: Logger
  ): Route = {
    val fut = either.value.toIO.unsafeToFuture()
    onSuccess(fut) {
      case Left(error: ErrorResponse) =>
        logger.warn(s"Responding with error: ${error}")
        complete(error.status, error)
      case Right(result) =>
        logger.info(s"Completing with result: ${result}")
        complete(result)
    }
  }
}

object RouteDispatcher {

  /**
    * @param routes to concatenate.
    * @return a single akka route composed by concatenating the routes.
    */
  def mergeRoutes(routes: Route*): Route = {
    routes.reduce(_ ~ _);
  }

  def mergeDispatchers(dispatchers: RouteDispatcher*): Route = {
    dispatchers.map(_.route).reduce(_ ~ _)
  }
}
