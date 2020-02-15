package user

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{ContentTypes, HttpRequest, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.unmarshalling.Unmarshal
import authentication.{AuthenticationDao, Tokens}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import pdi.jwt.{Jwt, JwtClaim}
import request.UserContext

import scala.concurrent.Future
import scala.util.Random

class UserRouteTest extends AsyncFlatSpec with Matchers with ScalaFutures with ScalatestRouteTest {

  class AuthDaoStub extends AuthenticationDao {
    override def storeCredentials(uid: Int, password: String): Future[Tokens] = Future.successful(
      Tokens(jwt(uid, "testConnectCode"), "testRefresh")
    )
  }

  def jwt(uid: Int, connectCode: String): String = Jwt.encode(
    claim = JwtClaim(content = s"""{"uid": $uid, "connectCode": "$connectCode"}""")
  )

  val route: Route = new UserRoute(new UserServiceImpl(new QuillUserDao(), new AuthDaoStub())).route

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
      Unmarshal(response).to[Tokens].map(t => t.accessToken).map(accessToken => {
        val getSelf = HttpRequest(uri = "/self", headers = List(RawHeader("Authorization", s"Bearer $accessToken")))
        val uid = UserContext.from(accessToken).right.get.uid
        getSelf ~> route ~> check {
          response.status shouldEqual StatusCodes.OK
          entityAs[String] shouldEqual s"""{"firstName":"first","lastName":"last","uid":$uid}"""
        }
      })
    }
  }

  "POST /self with a valid user" should "return a generated UID" in {
    registerUser(randomEmail()) ~> route ~> check {
      response.status shouldEqual StatusCodes.OK
      Unmarshal(response).to[Tokens].map(t => t.accessToken).map(at => UserContext.from(at).right.get).map(c => {
        c.uid should be > 0
        c.pid shouldBe None
        c.cid shouldBe None
        c.connectCode.isDefined shouldBe true
        c.connectCode.get shouldBe "testConnectCode"
      })
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

  def registerUser(email: String): HttpRequest = {
    val userRegistration = s"""{"firstName": "first", "lastName": "last", "email": "$email", "password": "strongpass", "acceptedTerms": true, "ofAge": true}"""
    Post("/self").withEntity(ContentTypes.`application/json`, userRegistration)
  }

  def randomEmail(): String = "userServiceWorkflowTest-" + Random.alphanumeric.take(10).mkString + "@twotest.com"

}
