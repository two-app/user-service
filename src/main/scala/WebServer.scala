import akka.http.scaladsl.Http
import cats.effect.{ExitCode, IO, IOApp, _}
import com.typesafe.scalalogging.Logger
import config.RootActorSystem._
import config.{Config, Services}
import db.{DatabaseConfig, FlywayHelper}
import doobie._
import doobie.hikari._

import scala.concurrent.duration._
import scala.io.StdIn

object WebServer extends IOApp {

  val logger: Logger = Logger("WebServer")

  override def run(args: List[String]): IO[ExitCode] = {
    logger.info("Migrating database...")
    FlywayHelper.migrate()

    logger.info("Migrated database. Starting thread pool...")
    transactor.use { xa =>
      logger.info("Thread pool transactor successfully loaded.")
      val host: String = Config.load().getString("server.host")
      val port: Int = Config.load().getInt("server.port")
      val route = new Services(xa).masterRoute

      logger.info(
        f"Starting server on configured host $host and port $port."
      )

      val bind = Http().bindAndHandle(route, host, port)

      logger.info("Successfully bound to host and port.")
      logger.info("Press enter to terminate...")

      StdIn.readLine()

      logger.info("Terminating server with 5 second grace period...")
      bind.flatMap(_.terminate(5.second)).flatMap(_ => system.terminate())

      logger.info("Terminated.")
      IO(ExitCode.Success)
    }
  }

  def transactor: Resource[IO, Transactor[IO]] =
    for {
      context <- ExecutionContexts.fixedThreadPool[IO](
        DatabaseConfig.connectionPoolSize
      )
      blocker <- Blocker[IO]
      transactor <- HikariTransactor.newHikariTransactor[IO](
        driverClassName = DatabaseConfig.driver,
        url = DatabaseConfig.jdbcWithSchema,
        user = DatabaseConfig.username,
        pass = DatabaseConfig.password,
        connectEC = context,
        blocker = blocker
      )
    } yield transactor
}
