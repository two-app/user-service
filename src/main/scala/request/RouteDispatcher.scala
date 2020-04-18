package request
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

trait RouteDispatcher {
  def route: Route
}

object RouteDispatcher {

  /**
    * @param routes to concatenate.
    * @return a single akka route composed by concatenating the routes.
    */
  def mergeRoutes(routes: Route*): Route = {
    routes.reduce(_ ~ _);
  }
}
