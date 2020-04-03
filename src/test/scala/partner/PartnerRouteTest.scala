package partner

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.unmarshalling.Unmarshal
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
import db.FlywayHelper
import config.MasterRoute
import cats.effect.IO
import user.UserServiceImpl
import authentication.AuthenticationDaoStub
import user.UserRoute
import request.UserContext

class PartnerRouteTest
    extends AsyncFunSpec
    with Matchers
    with ScalatestRouteTest
    with BeforeAndAfterEach
    with AuthTestArbitraries
    with UserTestArbitraries {

  val userOneConnectCode = "zQp7Wl"
  val userTwoConnectCode = "0Q3ar8"

  val partnerRoute: Route = new PartnerRoute(
    new PartnerServiceImpl[IO](
      new UserServiceImpl[IO](
        MasterRoute.services.userDao,
        new AuthenticationDaoStub()
      ),
      MasterRoute.services.coupleDao,
      new AuthenticationDaoStub()
    )
  ).route

  val userRoute: Route = new UserRoute(
    new UserServiceImpl[IO](
      MasterRoute.services.userDao,
      new AuthenticationDaoStub()
    )
  ).route

  override def beforeEach(): Unit = FlywayHelper.cleanMigrate()

  def registerUserRequest(): HttpRequest =
    Post("/self", randomUserRegistration())
  def connectRequest(tokens: Tokens, connectCode: String): HttpRequest =
    Post(s"/partner/$connectCode")
      .withHeaders(List(authHeader(tokens.accessToken)))

  describe("POST /partner/{connectCode}") {
    it("should return new tokens for two unconnected users") {
      // Register the first user
      registerUserRequest() ~> userRoute ~> check {
        val userTokens: Tokens = entityAs[Tokens]
        val uid: Int = extractContext(userTokens.accessToken).uid

        // Register the second user and generate their connect code
        registerUserRequest() ~> userRoute ~> check {
          val partnerTokens: Tokens = entityAs[Tokens]
          val pid: Int = extractContext(partnerTokens.accessToken).uid
          val partnerConnectCode = connectCodeFromId(pid) // TODO have CC in UserContext

          // Connect the two users, verifying returned tokens
          connectRequest(userTokens, partnerConnectCode) ~> partnerRoute ~> check {
            response.status shouldBe StatusCodes.OK
            val newUserTokens: Tokens = entityAs[Tokens]
            val newUserContext: UserContext =
              extractContext(newUserTokens.accessToken)

            newUserContext.uid shouldBe uid
            newUserContext.pid shouldBe Option(pid)
            newUserContext.cid shouldBe Option(1)
          }
        }
      }
    }

    it("should return Bad Request for an already connected user") {
      registerUserRequest() ~> userRoute ~> check {
        val userTokens: Tokens = entityAs[Tokens]
        registerUserRequest() ~> userRoute ~> check {
          val partnerTokens: Tokens = entityAs[Tokens]
          val pid: Int = extractContext(partnerTokens.accessToken).uid
          val partnerConnectCode: String = connectCodeFromId(pid)

          // Connect user, then reattempt connect
          connectRequest(userTokens, partnerConnectCode) ~> partnerRoute ~> check {
            response.status shouldBe StatusCodes.OK

            connectRequest(userTokens, partnerConnectCode) ~> partnerRoute ~> check {
              response.status shouldBe StatusCodes.BadRequest
              entityAs[ErrorResponse] shouldBe ClientError("User already has a partner.")
            }
          }
        }
      }
    }

    it("should return Bad Request for an already connected partner") {
      registerUserRequest() ~> userRoute ~> check {
        val userTokens: Tokens = entityAs[Tokens]
        registerUserRequest() ~> userRoute ~> check {
          val partnerTokens: Tokens = entityAs[Tokens]
          val pid: Int = extractContext(partnerTokens.accessToken).uid
          val partnerConnectCode: String = connectCodeFromId(pid)

          connectRequest(userTokens, partnerConnectCode) ~> partnerRoute ~> check {
            response.status shouldBe StatusCodes.OK

            // register new user and attempt connection with partner
            registerUserRequest() ~> userRoute ~> check {
              val newUserTokens: Tokens = entityAs[Tokens]

              connectRequest(newUserTokens, partnerConnectCode) ~> partnerRoute ~> check {
                response.status shouldBe StatusCodes.BadRequest
                entityAs[ErrorResponse] shouldBe ClientError(
                  "Partner already has a partner."
                )
              }
            }
          }
        }
      }
    }

    it("should return bad request trying to connect with yourself") {
      registerUserRequest() ~> userRoute ~> check {
        val userTokens: Tokens = entityAs[Tokens]
        val userContext: UserContext = extractContext(userTokens.accessToken)
        val userConnectCode: String = connectCodeFromId(userContext.uid)

        connectRequest(userTokens, userConnectCode) ~> partnerRoute ~> check {
          response.status shouldBe StatusCodes.BadRequest
          entityAs[ErrorResponse] shouldBe ClientError("You can't partner with yourself.")
        }
      }
    }

    it("should return bad request with an incorrect connect code") {
      registerUserRequest() ~> userRoute ~> check {
        val userTokens: Tokens = entityAs[Tokens]
        
        connectRequest(userTokens, "RandomConnectCode") ~> partnerRoute ~> check {
          entityAs[ErrorResponse] shouldBe ClientError("Invalid connect code.")
        }
      }
    }
  }
}
