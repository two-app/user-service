import akka.http.scaladsl.server.{HttpApp, Route}
import user.UserRoute

object Server extends HttpApp {
  override protected def routes: Route = UserRoute.route
}

object WebServer {
  def main(args: Array[String]): Unit = {
    Server.startServer("localhost", 8080)
  }
}