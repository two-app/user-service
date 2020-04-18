package user

import org.scalatest.funspec.AsyncFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.BeforeAndAfterEach
import db.FlywayHelper
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import scala.reflect.ClassTag
import cats.effect.IO
import config.MasterRoute
import authentication.AuthenticationDaoStub

class UserRouteTest
    extends AsyncFunSpec
    with Matchers
    with ScalatestRouteTest
    with BeforeAndAfterEach {

  val route: Route = new UserRouteDispatcher(
    new UserServiceImpl(
      MasterRoute.services.userDao,
      new AuthenticationDaoStub()
    )
  ).route

  override def beforeEach(): Unit = FlywayHelper.cleanMigrate()

  def GetUser[T: FromEntityUnmarshaller: ClassTag](email: String): T =
    Get(s"/user?email=${email}") ~> route ~> check {
      entityAs[T]
    }

  describe("getUser") {
    it("should return ok") {
      GetUser[String]("test") shouldBe "test"
    }
  }
}
