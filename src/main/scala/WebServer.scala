import akka.http.scaladsl.server.{HttpApp, Route}
import config.MasterRoute
import cats.effect.IOApp
import cats.effect.IO
import cats.effect.ExitCode
import config.Config

class Server extends HttpApp {
  override protected def routes: Route = MasterRoute.masterRoute
}

object WebServer extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {
    val port: Int = Config.load().getInt("server.port")
    new Server().startServer("localhost", port)
    IO(ExitCode.Success)
  }
}