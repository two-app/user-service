import akka.http.scaladsl.server.{HttpApp, Route}
import config.MasterRoute

object Server extends HttpApp {
  override protected def routes: Route = MasterRoute.masterRoute
}

object WebServer {
  def main(args: Array[String]): Unit = {
    Server.startServer("localhost", 8080)
  }
}