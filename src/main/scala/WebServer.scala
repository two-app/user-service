import akka.http.scaladsl.server.{HttpApp, Route}
import config.MasterRoute
import config.Config
import db.FlywayHelper
import cats.effect._
import doobie._
import doobie.implicits._
import doobie.hikari._
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.HikariConfig
import db.DatabaseConfig
import com.typesafe.scalalogging.Logger

class Server(xa: Transactor[IO]) extends HttpApp {
  override protected def routes: Route = new MasterRoute(xa).masterRoute
}

object WebServer extends IOApp {

  val logger: Logger = Logger("WebServer")

  // loads JDBC connection pool and applies flyway migrations
  val transactor: Resource[IO, HikariTransactor[IO]] =
    for {
      ce <- ExecutionContexts.fixedThreadPool[IO](
        Config.load().getInt("db.connectionPoolSize")
      )
      be <- Blocker[IO]
      ds <- createDataSourceResource[IO]()
      _ <- Resource.liftF(IO(FlywayHelper.migrate(ds)))
      xa = HikariTransactor[IO](ds, ce, be)
    } yield xa

  def run(args: List[String]): IO[ExitCode] = {
    logger.info("Application started up. Loading Transactor...")
    transactor.use { xa =>
      logger.info("Transactor successfully loaded.")
      val host: String = Config.load().getString("server.host")
      val port: Int = Config.load().getInt("server.port")

      logger.info(s"Starting server on configured host ${host} and port ${port}.")
      new Server(xa).startServer(host, port)

      logger.info("Server finished blocking. Exiting with success.")
      IO(ExitCode.Success)
    }
  }

  private def createDataSourceResource[M[_]: Sync]()
      : Resource[M, HikariDataSource] = {
    val hikariConfig = new HikariConfig()
    hikariConfig.setDriverClassName(DatabaseConfig.driver)
    hikariConfig.setJdbcUrl(DatabaseConfig.jdbc)
    hikariConfig.setUsername(DatabaseConfig.username)
    hikariConfig.setPassword(DatabaseConfig.password)

    val alloc = Sync[M].delay(new HikariDataSource(hikariConfig))
    val free = (ds: HikariDataSource) => Sync[M].delay(ds.close())
    Resource.make(alloc)(free)
  }
}
