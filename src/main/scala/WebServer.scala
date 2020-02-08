import akka.http.scaladsl.server.{HttpApp, Route}
import user.{QuillUserDao, UserRoute, UserServiceImpl}

object Server extends HttpApp {
  override protected def routes: Route = new UserRoute(new UserServiceImpl(new QuillUserDao())).route
}

object WebServer {
  def main(args: Array[String]): Unit = {
    Server.startServer("localhost", 8080)
  }
}