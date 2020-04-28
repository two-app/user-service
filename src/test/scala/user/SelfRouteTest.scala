package user

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{ContentTypes, HttpRequest, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import config.TestServices
import spray.json.DefaultJsonProtocol.{jsonFormat2, _}
import spray.json.{RootJsonFormat, _}
import authentication.{AuthenticationDao, Tokens}
import org.scalatest.matchers.should.Matchers
import pdi.jwt.{Jwt, JwtClaim}
import request.UserContext

import scala.util.Random
import org.scalatest.funspec.AsyncFunSpec
import authentication.AuthenticationDaoStub
import response.ErrorResponse
import response.ErrorResponse.AuthorizationError
import response.ErrorResponse.ClientError
import org.scalatest.BeforeAndAfterEach
import db.DatabaseTestMixin
import scala.reflect.ClassTag
import authentication.AuthTestArbitraries

class SelfRouteTest
    extends AsyncFunSpec
    with Matchers
    with ScalatestRouteTest
    with BeforeAndAfterEach
    with UserTestArbitraries
    with AuthTestArbitraries
    with DatabaseTestMixin {

  val route: Route = TestServices.masterRoute

  implicit val UserRegistrationFormat: RootJsonFormat[UserRegistration] =
    jsonFormat6(UserRegistration.apply)

  def PostSelf[T: FromEntityUnmarshaller: ClassTag](
      registration: UserRegistration
  ): T = {
    Post("/self", registration) ~> route ~> check {
      entityAs[T]
    }
  }

  def GetSelf[T: FromEntityUnmarshaller: ClassTag](
      tokens: Tokens
  ): T = {
    Get("/self").withHeaders(authHeader(tokens.accessToken)) ~> route ~> check {
      entityAs[T]
    }
  }

  override def beforeEach(): Unit = cleanMigrate()

  describe("POST /self") {
    it("should return a 400 Bad Request with a duplicate email") {
      val registration: UserRegistration = randomUserRegistration()

      PostSelf[Tokens](registration)
      val response = PostSelf[ErrorResponse](registration)

      response shouldBe ClientError("An account with this email exists.")
    }

    it("should store a valid user") {
      val registration: UserRegistration = randomUserRegistration()

      val tokens = PostSelf[Tokens](registration)

      val user: User = GetSelf[User](tokens)

      user.uid shouldBe extractContext(tokens).uid
      user.pid shouldBe None
      user.cid shouldBe None
      user.firstName shouldBe registration.firstName
      user.lastName shouldBe registration.lastName
    }
  }

  describe("GET /self") {
    it("should return 401 Unauthorized without a token") {
      Get("/self") ~> route ~> check {
        response.status shouldBe StatusCodes.Unauthorized
        responseAs[ErrorResponse] shouldBe AuthorizationError(
          "Authorization not provided."
        )
      }
    }

    it("should return 401 Unauthorized with an invalid token") {
      def request = Get("/self").withHeaders(authHeader("x"))

      request ~> route ~> check {
        responseAs[ErrorResponse] shouldBe AuthorizationError(
          "Invalid token format."
        )
      }
    }
  }

}
