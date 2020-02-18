package partner

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import akka.http.scaladsl.unmarshalling.Unmarshal
import authentication.{AuthenticationDao, Tokens}
import config.MasterRoute
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import pdi.jwt.{Jwt, JwtClaim}
import response.ErrorResponse
import response.ErrorResponse.ClientError

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

class PartnerRouteTest extends AsyncFlatSpec with Matchers with ScalaFutures with ScalatestRouteTest with BeforeAndAfterEach {

  class AuthDaoStub extends AuthenticationDao {
    override def storeCredentials(uid: Int, password: String): Future[Tokens] = ???

    override def createTokens(uid: Int, pid: Option[Int], cid: Option[Int]): Future[Tokens] = Future.successful(
      Tokens("testAccessToken", "testRefreshToken")
    )
  }

  def jwt(uid: Int, pid: Int, cid: Int): String = Jwt.encode(
    claim = JwtClaim(content = s"""{"uid": $uid, "pid": $pid, "cid": $cid}""")
  )

  def unconnectedJwt(uid: Int, connectCode: String): String = Jwt.encode(
    claim = JwtClaim(content = s"""{"uid": $uid, "connectCode": "$connectCode"}""")
  )

  def authHeader(jwt: String): RawHeader = RawHeader("Authorization", s"Bearer $jwt")

  val userOneConnectCode = "zQp7Wl"
  val userTwoConnectCode = "0Q3ar8"

  implicit def default(implicit system: ActorSystem): RouteTestTimeout = RouteTestTimeout(new DurationInt(5).second)

  val route: Route = new PartnerRoute(new PartnerServiceImpl(MasterRoute.userService, MasterRoute.coupleDao, new AuthDaoStub())).route

  "POST /partner/xyz for an already connected user" should "return bad request" in {
    val header = authHeader(jwt(1, 2, 3))
    val req: HttpRequest = HttpRequest(HttpMethods.POST, s"/partner/$userTwoConnectCode", headers = List(header))
    req ~> route ~> check {
      status shouldBe StatusCodes.BadRequest
      Unmarshal(response).to[ErrorResponse].map(er => {
        er shouldBe ClientError("User already has a partner.")
      })
    }
  }

  "POST /partner/xyz with the same connect code as requesting user" should "return bad request" in {
    val header = authHeader(unconnectedJwt(1, userOneConnectCode))
    val req = HttpRequest(HttpMethods.POST, s"/partner/$userOneConnectCode", headers = List(header))
    req ~> route ~> check {
      status shouldBe StatusCodes.BadRequest
      Unmarshal(response).to[ErrorResponse].map(er => {
        er shouldBe ClientError("You can't partner with yourself.")
      })
    }
  }

  "POST /partner/xyz" should "return tokens for two unconnected users" in {
    val header = authHeader(unconnectedJwt(1, userOneConnectCode))
    val req = HttpRequest(HttpMethods.POST, s"/partner/$userTwoConnectCode", headers = List(header))
    req ~> route ~> check {
      status shouldBe StatusCodes.OK
    }
  }
}
