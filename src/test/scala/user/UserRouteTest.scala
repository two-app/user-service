package user

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{ContentTypes, HttpRequest, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.unmarshalling.Unmarshal
import spray.json.DefaultJsonProtocol.{jsonFormat2, _}
import spray.json.{RootJsonFormat, _}
import authentication.{AuthenticationDao, Tokens}
import org.scalatest.matchers.should.Matchers
import pdi.jwt.{Jwt, JwtClaim}
import request.UserContext

import scala.util.Random
import org.scalatest.funspec.AsyncFunSpec
import authentication.AuthenticationDaoStub
import config.MasterRoute
import response.ErrorResponse
import response.ErrorResponse.AuthorizationError
import response.ErrorResponse.ClientError
import org.scalatest.BeforeAndAfterEach
import db.FlywayHelper

class SelfRouteTest extends AsyncFunSpec with Matchers with ScalatestRouteTest with BeforeAndAfterEach {

  val route: Route = new SelfRoute(
    new UserServiceImpl(
      MasterRoute.services.userDao,
      new AuthenticationDaoStub()
    )
  ).route

  override def beforeEach(): Unit = FlywayHelper.cleanMigrate()

  def authHeader(token: String): List[RawHeader] =
    List(RawHeader("Authorization", s"Bearer $token"))

  describe("POST /self") {
    implicit val UserRegistrationFormat: RootJsonFormat[UserRegistration] =
      jsonFormat6(UserRegistration.apply)

    it("should return a 400 Bad Request with a duplicate email") {
      val registration: UserRegistration = newUser()

      Post("/self", registration) ~> route ~> check {
        response.status shouldBe StatusCodes.OK

        Post("/self", registration) ~> route ~> check {
          entityAs[ErrorResponse] shouldBe ClientError(
            "An account with this email exists."
          )
        }
      }
    }

    it("should store a valid user") {
      val registration: UserRegistration = newUser()

      Post("/self", registration) ~> route ~> check {
        response.status shouldBe StatusCodes.OK
        val tokens: Tokens = entityAs[Tokens]
        val tokenUID: Int = UserContext.from(tokens.accessToken).right.get.uid

        Get("/self").withHeaders(authHeader(tokens.accessToken)) ~> route ~> check {
          response.status shouldBe StatusCodes.OK
          val user: User = responseAs[User]

          user.uid shouldBe tokenUID
          user.pid shouldBe None
          user.cid shouldBe None
          user.firstName shouldBe registration.firstName
          user.lastName shouldBe registration.lastName
        }
      }
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

  def newUser(): UserRegistration = UserRegistration(
    firstName = "First",
    lastName = "Last",
    email = randomEmail(),
    password = "StrongPassword",
    acceptedTerms = true,
    ofAge = true
  )

  def randomEmail(): String =
    "userServiceWorkflowTest-" + Random.alphanumeric
      .take(10)
      .mkString + "@twotest.com"

}
