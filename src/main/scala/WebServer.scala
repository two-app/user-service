import akka.http.scaladsl.server.{HttpApp, Route}
import config.MasterRoute
import cats.effect.IOApp
import cats.effect.IO
import cats.effect.ExitCode

class Server extends HttpApp {
  override protected def routes: Route = MasterRoute.masterRoute
}

object WebServer extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {
    new Server().startServer("localhost", 8080)
    IO(ExitCode.Success)
  }
}