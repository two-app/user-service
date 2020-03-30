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
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import pdi.jwt.{Jwt, JwtClaim}
import response.ErrorResponse
import response.ErrorResponse.ClientError

import scala.concurrent.Future

class PartnerRouteTest extends AsyncFlatSpec with Matchers with ScalaFutures with ScalatestRouteTest with BeforeAndAfterEach {

  def jwt(uid: Int, pid: Int, cid: Int): String = Jwt.encode(
    claim = JwtClaim(content = s"""{"uid": $uid, "pid": $pid, "cid": $cid}""")
  )

  def unconnectedJwt(uid: Int, connectCode: String): String = Jwt.encode(
    claim = JwtClaim(content = s"""{"uid": $uid, "connectCode": "$connectCode"}""")
  )

  def authHeader(jwt: String): RawHeader = RawHeader("Authorization", s"Bearer $jwt")

  val userOneConnectCode = "zQp7Wl"
  val userTwoConnectCode = "0Q3ar8"

  // "POST /partner/xyz" should "return tokens for two unconnected users" in {
  //   val tokens = Tokens("testAccess", "testRefresh")
  //   val route = new TestBed().onConnectUsers(Right(tokens)).build()
  //   val header = authHeader(unconnectedJwt(1, userOneConnectCode))
  //   val req = HttpRequest(HttpMethods.POST, s"/partner/$userTwoConnectCode", headers = List(header))

  //   req ~> route ~> check {
  //     status shouldBe StatusCodes.OK
  //     entityAs[Tokens] shouldBe tokens
  //   }
  // }

  // "POST /partner/xyz for an already connected user token" should "return bad request" in {
  //   val route = new TestBed().onConnectUsersFail().build()
  //   val header = authHeader(jwt(1, 2, 3))
  //   val req: HttpRequest = HttpRequest(HttpMethods.POST, s"/partner/$userTwoConnectCode", headers = List(header))
  //   req ~> route ~> check {
  //     status shouldBe StatusCodes.BadRequest
  //     Unmarshal(response).to[ErrorResponse].map(er => {
  //       er shouldBe ClientError("User already has a partner.")
  //     })
  //   }
  // }

  // "POST /partner/xyz with the same connect code as requesting user" should "return bad request" in {
  //   val route = new TestBed().onConnectUsersFail().build()
  //   val header = authHeader(unconnectedJwt(1, userOneConnectCode))
  //   val req = HttpRequest(HttpMethods.POST, s"/partner/$userOneConnectCode", headers = List(header))
  //   req ~> route ~> check {
  //     status shouldBe StatusCodes.BadRequest
  //     Unmarshal(response).to[ErrorResponse].map(er => {
  //       er shouldBe ClientError("You can't partner with yourself.")
  //     })
  //   }
  // }

  // "POST /partner/xyz with a connect code that does not exist" should "return bad request" in {
  //   val error = ClientError("x")
  //   val route = new TestBed().onConnectUsers(Left(error)).build()
  //   val header = authHeader(unconnectedJwt(1, userOneConnectCode))
  //   val req = HttpRequest(HttpMethods.POST, s"/partner/$userTwoConnectCode", headers = List(header))

  //   req ~> route ~> check {
  //     status shouldBe StatusCodes.BadRequest
  //     entityAs[ErrorResponse] shouldBe error
  //   }
  // }

  // private class TestBed {
  //   var partnerServiceStub: PartnerService = _

  //   def onConnectUsers(errorOrTokens: Either[ErrorResponse, Tokens]): TestBed = {
  //     partnerServiceStub = (_, _) => EitherT.fromEither[Future](errorOrTokens)
  //     this
  //   }

  //   def onConnectUsersFail(): TestBed = {
  //     partnerServiceStub = (_, _) => fail("Expected partner service not to be called.")
  //     this
  //   }

  //   def build(): Route = new PartnerRoute(partnerServiceStub).route
  // }
}
