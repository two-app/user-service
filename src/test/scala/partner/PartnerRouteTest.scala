package partner

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.unmarshalling.Unmarshal
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import pdi.jwt.{Jwt, JwtClaim}
import response.ErrorResponse
import response.ErrorResponse.ClientError

class PartnerRouteTest extends AsyncFlatSpec with Matchers with ScalaFutures with ScalatestRouteTest {
  def jwt(uid: Int, pid: Int, cid: Int): String = Jwt.encode(
    claim = JwtClaim(content = s"""{"uid": $uid, "pid": $pid, "cid": $cid}""")
  )

  val route: Route = new PartnerRoute().route

  "POST /partner/xyz" should "return bad request for an already connected user" in {
    val header = RawHeader("Authorization", s"Bearer ${jwt(1, 2, 3)}")
    val req: HttpRequest = HttpRequest(HttpMethods.POST, "/partner/xyz", headers = List(header))
    req ~> route ~> check {
      status shouldBe StatusCodes.BadRequest
      Unmarshal(response).to[ErrorResponse].map(er => {
        er shouldBe ClientError("User already has a partner.")
      })
    }
  }
}
