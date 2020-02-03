package user

import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpRequest, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import pdi.jwt.{Jwt, JwtClaim}

class UserRouteTest extends AnyFlatSpec with Matchers with ScalaFutures with ScalatestRouteTest {

  val jwt: String = Jwt.encode(claim = JwtClaim(content = """{"uid": 1, "pid": 2, "cid": 3}"""))

  "GET /self without a token" should "return unauthorized" in {
    Get("/self") ~> UserRoute.route ~> check {
      response.status shouldEqual StatusCodes.Unauthorized
      responseAs[String] shouldEqual """{"status":"401 Unauthorized","reason":"Authorization not provided."}"""
    }
  }

  val InvalidTokenReq: HttpRequest = HttpRequest(uri = "/self", headers = List(RawHeader("Authorization", "Bearer X")))

  "GET /self with an invalid token" should "return unauthorized" in {
    InvalidTokenReq ~> UserRoute.route ~> check {
      response.status shouldEqual StatusCodes.Unauthorized
      responseAs[String] shouldEqual """{"status":"401 Unauthorized","reason":"Invalid token format."}"""
    }
  }

  val ValidTokenReq: HttpRequest = HttpRequest(uri = "/self", headers = List(RawHeader("Authorization", s"Bearer $jwt")))

  "GET /self with a valid token" should "return the user" in {
    ValidTokenReq ~> UserRoute.route ~> check {
      response.status shouldEqual StatusCodes.OK
      responseAs[String] shouldEqual """{"firstName":"Gerry","lastName":"Fletcher","uid":1}"""
    }
  }

}
