package user

import org.scalatest.funspec.AsyncFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.BeforeAndAfterEach
import db.FlywayHelper
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Directives._
import scala.reflect.ClassTag
import cats.effect.IO
import config.MasterRoute
import authentication.AuthenticationDaoStub
import response.ErrorResponse
import response.ErrorResponse.ClientError
import response.ErrorResponse.NotFoundError
import authentication.Tokens
import authentication.AuthTestArbitraries

class UserRouteTest
    extends AsyncFunSpec
    with Matchers
    with ScalatestRouteTest
    with BeforeAndAfterEach
    with UserTestArbitraries
    with AuthTestArbitraries {

  val selfRoute: Route = new SelfRoute(
    new UserServiceImpl(
      MasterRoute.services.userDao,
      new AuthenticationDaoStub()
    )
  ).route

  val route: Route = selfRoute ~ (new UserRouteDispatcher(
    new UserServiceImpl(
      MasterRoute.services.userDao,
      new AuthenticationDaoStub()
    )
  ).route)
  override def beforeEach(): Unit = FlywayHelper.cleanMigrate()

  def GetUser[T: FromEntityUnmarshaller: ClassTag](email: String): T =
    Get(s"/user?email=${email}") ~> route ~> check {
      entityAs[T]
    }

  def PostSelf[T: FromEntityUnmarshaller: ClassTag](
      registration: UserRegistration
  ): T =
    Post("/self", registration) ~> route ~> check {
      entityAs[T]
    }

  describe("getUser") {
    it("should return a bad request with a malformed email") {
      GetUser[ErrorResponse]("emailATgmail.com") shouldBe ClientError(
        "Badly formatted email."
      )
    }

    it("should return not found for an email that doesn't exist") {
      GetUser[ErrorResponse]("unknown@two.com") shouldBe NotFoundError(
        "User does not exist."
      )
    }

    it("should return a user that does exist") {
      val registration = randomUserRegistration()

      val uid = extractContext(PostSelf[Tokens](registration).accessToken).uid
      val user = GetUser[User](registration.email)

      user.uid shouldBe uid
      user.pid shouldBe None
      user.cid shouldBe None
      user.firstName shouldBe registration.firstName
      user.lastName shouldBe registration.lastName
    }
  }
}
