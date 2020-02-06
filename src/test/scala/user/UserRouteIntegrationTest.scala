package user

import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{ContentTypes, HttpRequest, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import pdi.jwt.{Jwt, JwtClaim}

import scala.util.Random

class UserRouteIntegrationTest extends AnyFlatSpec with Matchers with ScalaFutures with ScalatestRouteTest {

  val route: Route = new UserRoute(new UserServiceImpl(new QuillUserDao())).route

  def jwt(uid: Int = 1): String = Jwt.encode(claim = JwtClaim(content = s"""{"uid": $uid, "pid": 2, "cid": 3}"""))

  "GET /self without a token" should "return unauthorized" in {
    Get("/self") ~> route ~> check {
      response.status shouldEqual StatusCodes.Unauthorized
      responseAs[String] shouldEqual """{"status":"401 Unauthorized","reason":"Authorization not provided."}"""
    }
  }

  val InvalidTokenReq: HttpRequest = HttpRequest(uri = "/self", headers = List(RawHeader("Authorization", "Bearer X")))

  "GET /self with an invalid token" should "return unauthorized" in {
    InvalidTokenReq ~> route ~> check {
      response.status shouldEqual StatusCodes.Unauthorized
      responseAs[String] shouldEqual """{"status":"401 Unauthorized","reason":"Invalid token format."}"""
    }
  }

  "GET /self with a valid user" should "return the user details" in {
    registerUser(randomEmail()) ~> route ~> check {
      response.status shouldEqual StatusCodes.OK
      val uid = entityAs[String].toInt
      val getSelf = HttpRequest(uri = "/self", headers = List(RawHeader("Authorization", s"Bearer ${jwt(uid)}")))
      getSelf ~> route ~> check {
        response.status shouldEqual StatusCodes.OK
        entityAs[String] shouldEqual s"""{"firstName":"first","lastName":"last","uid":$uid}"""
      }
    }
  }

  "POST /self with a valid user" should "return a generated UID" in {
    registerUser(randomEmail()) ~> route ~> check {
      response.status shouldEqual StatusCodes.OK
      entityAs[String].toInt should be > 0
    }
  }

  "POST /self with duplicate email" should "return a bad request" in {
    val email = randomEmail()
    registerUser(email) ~> route ~> check {
      response.status shouldEqual StatusCodes.OK
      registerUser(email) ~> route ~> check {
        response.status shouldEqual StatusCodes.BadRequest
        responseAs[String] shouldEqual """{"status":"400 Bad Request","reason":"An account with this email exists."}"""
      }
    }
  }

  // TODO reimplement this test when createUser is done
  //  val ValidTokenReq: HttpRequest = HttpRequest(uri = "/self", headers = List(RawHeader("Authorization", s"Bearer ${jwt()}")))
  //
  //  "GET /self with a valid token" should "return the user" in {
  //    registerUser("testUser@two.com") ~> route ~> check {
  //      response.status shouldEqual StatusCodes.OK
  //    }
  //
  //    ValidTokenReq ~> route ~> check {
  //      response.status shouldEqual StatusCodes.OK
  //      responseAs[String] shouldEqual """{"firstName":"Gerry","lastName":"Fletcher","uid":1}"""
  //    }
  //  }

  def registerUser(email: String): HttpRequest = {
    val userRegistration = s"""{"firstName": "first", "lastName": "last", "email": "$email", "password": "strongpass", "acceptedTerms": true, "ofAge": true}"""
    Post("/self").withEntity(ContentTypes.`application/json`, userRegistration)
  }

  def randomEmail(): String = "userServiceWorkflowTest-" + Random.alphanumeric.take(10).mkString + "@twotest.com"

}
