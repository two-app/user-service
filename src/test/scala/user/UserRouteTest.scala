package user

import org.scalatest.funspec.AsyncFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.BeforeAndAfterEach
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Directives._
import db.DatabaseTestMixin
import scala.reflect.ClassTag
import cats.effect.IO
import config.MasterRoute
import authentication.AuthenticationDaoStub
import response.ErrorResponse
import response.ErrorResponse.ClientError
import response.ErrorResponse.NotFoundError
import authentication.Tokens
import authentication.AuthTestArbitraries
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.MissingQueryParamRejection
import akka.http.scaladsl.server.MalformedQueryParamRejection

class UserRouteTest
    extends AsyncFunSpec
    with Matchers
    with ScalatestRouteTest
    with BeforeAndAfterEach
    with UserTestArbitraries
    with AuthTestArbitraries
    with DatabaseTestMixin {

  val selfRoute: Route = new SelfRoute(
    new UserServiceImpl(
      new MasterRoute(xa).services.userDao,
      new AuthenticationDaoStub()
    )
  ).route

  val route: Route = selfRoute ~ (new UserRouteDispatcher(
    new UserServiceImpl(
      new MasterRoute(xa).services.userDao,
      new AuthenticationDaoStub()
    )
  ).route)
  
  override def beforeEach(): Unit = cleanMigrate()

  def GetUser[T: FromEntityUnmarshaller: ClassTag](email: String): T =
    Get(s"/user?email=${email}") ~> route ~> check {
      entityAs[T]
    }

  def GetUser[T: FromEntityUnmarshaller: ClassTag](uid: Int): T =
    Get(s"/user?uid=${uid}") ~> route ~> check {
      entityAs[T]
    }

  def PostSelf[T: FromEntityUnmarshaller: ClassTag](
      registration: UserRegistration
  ): T =
    Post("/self", registration) ~> route ~> check {
      entityAs[T]
    }

  describe("getUser by email") {
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

  describe("getUser by uid") {
    it("should return bad request with a malformed number") {
      Get(s"/user?uid=blablabla") ~> route ~> check {
        val rej: MalformedQueryParamRejection =  rejections.last.asInstanceOf[MalformedQueryParamRejection]
        rej.parameterName shouldBe "uid"
        rej.errorMsg shouldBe "'blablabla' is not a valid 32-bit signed integer value"
      }
    }

    it("should return not found for a uid that doesn't exist") {
      GetUser[ErrorResponse](99) shouldBe NotFoundError(
        "User does not exist."
      )
    }

    it("should return a user that does exist") {
      val registration = randomUserRegistration()

      val uid: Int =
        extractContext(PostSelf[Tokens](registration).accessToken).uid
      val user = GetUser[User](uid)

      user.uid shouldBe uid
      user.pid shouldBe None
      user.cid shouldBe None
      user.firstName shouldBe registration.firstName
      user.lastName shouldBe registration.lastName
    }
  }
}
