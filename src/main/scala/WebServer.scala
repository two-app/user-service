import akka.http.scaladsl.server.{HttpApp, Route}
import authentication.AuthenticationServiceDao
import partner.PartnerRoute
import user.{QuillUserDao, UserRoute, UserServiceImpl}

object Server extends HttpApp {
  override protected def routes: Route = userRoute  ~ partnerRoute

  val userRoute: Route = new UserRoute(new UserServiceImpl(new QuillUserDao(), new AuthenticationServiceDao())).route
  val partnerRoute: Route = new PartnerRoute().route
}

object WebServer {
  def main(args: Array[String]): Unit = {
    Server.startServer("localhost", 8080)
  }
}