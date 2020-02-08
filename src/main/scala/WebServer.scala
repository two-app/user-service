import akka.http.scaladsl.server.{HttpApp, Route}
import authentication.AuthenticationServiceDao
import user.{QuillUserDao, UserRoute, UserServiceImpl}

object Server extends HttpApp {
  override protected def routes: Route = new UserRoute(new UserServiceImpl(new QuillUserDao(), new AuthenticationServiceDao())).route
}

object WebServer {
  def main(args: Array[String]): Unit = {
    Server.startServer("localhost", 8080)
  }
}