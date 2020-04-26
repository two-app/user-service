package partner

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import authentication.Tokens
import cats.data.EitherT
import cats.implicits._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.funspec.AsyncFunSpec
import org.scalatest.matchers.should.Matchers
import response.ErrorResponse
import response.ErrorResponse.ClientError
import authentication.AuthTestArbitraries
import user.UserTestArbitraries
import db.DatabaseTestMixin
import config.MasterRoute
import cats.effect.IO
import user.UserServiceImpl
import authentication.AuthenticationDaoStub
import user.SelfRoute
import request.UserContext
import response.ErrorResponse.NotFoundError
import user.User
import scala.reflect.ClassTag

class PartnerRouteTest
    extends AsyncFunSpec
    with Matchers
    with ScalatestRouteTest
    with BeforeAndAfterEach
    with AuthTestArbitraries
    with UserTestArbitraries
    with DatabaseTestMixin {

  val userOneConnectCode = "zQp7Wl"
  val userTwoConnectCode = "0Q3ar8"

  val partnerRoute: Route = new PartnerRoute(
    new PartnerServiceImpl[IO](
      new UserServiceImpl[IO](
        new MasterRoute(xa).services.userDao,
        new AuthenticationDaoStub()
      ),
      new MasterRoute(xa).services.coupleDao,
      new AuthenticationDaoStub(),
      new MasterRoute(xa).services.partnerDao
    )
  ).route

  val userRoute: Route = new SelfRoute(
    new UserServiceImpl[IO](
      new MasterRoute(xa).services.userDao,
      new AuthenticationDaoStub()
    )
  ).route

  override def beforeEach(): Unit = cleanMigrate()

  def registerUser(): Tokens =
    Post("/self", randomUserRegistration()) ~> userRoute ~> check {
      entityAs[Tokens]
    }

  def connectToPartner[T: FromEntityUnmarshaller: ClassTag](
      tokens: Tokens,
      connectCode: String
  ): T =
    Post(s"/partner/$connectCode").withHeaders(
      authHeader(tokens.accessToken)
    ) ~> partnerRoute ~> check {
      entityAs[T]
    }

  def getPartner[T: FromEntityUnmarshaller: ClassTag](tokens: Tokens): T = {
    Get("/partner").withHeaders(authHeader(tokens.accessToken)) ~> partnerRoute ~> check {
      entityAs[T]
    }
  }

  describe("GET /partner") {
    describe("Connected user") {
      it("should return the partner") {
        val userTokens: Tokens = registerUser()
        val partnerTokens: Tokens = registerUser()
        val partnerContext: UserContext =
          extractContext(partnerTokens.accessToken)

        // connect and retrieve partner
        val newUserTokens = connectToPartner[Tokens](
          userTokens,
          connectCodeFromId(partnerContext.uid)
        )
        val partner = getPartner[User](userTokens)

        partner.uid shouldBe partnerContext.uid
      }
    }

    describe("Unconnected user") {
      it("should return a not found error") {
        val userTokens: Tokens = registerUser()
        val errorResponse = getPartner[ErrorResponse](userTokens)

        errorResponse shouldBe NotFoundError(
          "You haven't connected with a partner yet."
        )
      }
    }
  }

  describe("POST /partner/{connectCode}") {
    it("should return new tokens for two unconnected users") {
      val userTokens: Tokens = registerUser()
      val uid: Int = extractContext(userTokens.accessToken).uid

      val partnerTokens: Tokens = registerUser()
      val pid: Int = extractContext(partnerTokens.accessToken).uid
      val partnerConnectCode: String = connectCodeFromId(pid)

      val newUserTokens = connectToPartner[Tokens](
        userTokens,
        partnerConnectCode
      )

      val newUserContext: UserContext =
        extractContext(newUserTokens.accessToken)

      newUserContext.uid shouldBe uid
      newUserContext.pid shouldBe Option(pid)
      newUserContext.cid shouldBe Option(1)
    }

    it("should return Bad Request for an already connected user") {
      val userTokens: Tokens = registerUser()

      val partnerTokens: Tokens = registerUser()
      val pid: Int = extractContext(partnerTokens.accessToken).uid
      val partnerConnectCode: String = connectCodeFromId(pid)

      connectToPartner(userTokens, partnerConnectCode)

      val errorResponse =
        connectToPartner[ErrorResponse](userTokens, partnerConnectCode)

      errorResponse shouldBe ClientError(
        "User already has a partner."
      )
    }

    it("should return Bad Request for an already connected partner") {
      val userTokens: Tokens = registerUser()

      val partnerTokens: Tokens = registerUser()
      val pid: Int = extractContext(partnerTokens.accessToken).uid
      val partnerConnectCode: String = connectCodeFromId(pid)

      connectToPartner(userTokens, partnerConnectCode)

      val thirdUserToken: Tokens = registerUser()
      val errorResponse =
        connectToPartner[ErrorResponse](thirdUserToken, partnerConnectCode)

      errorResponse shouldBe ClientError(
        "Partner already has a partner."
      )
    }

    it("should return bad request trying to connect with yourself") {
      val userTokens: Tokens = registerUser()
      val userContext: UserContext = extractContext(userTokens.accessToken)
      val userConnectCode: String = connectCodeFromId(userContext.uid)

      val errorResponse =
        connectToPartner[ErrorResponse](userTokens, userConnectCode)

      errorResponse shouldBe ClientError(
        "You can't partner with yourself."
      )
    }

    it("should return bad request with an incorrect connect code") {
      val userTokens: Tokens = registerUser()
      val errorResponse = connectToPartner[ErrorResponse](userTokens, "RandomConnectCode")

      errorResponse shouldBe ClientError("Invalid connect code.")
    }
  }
}
