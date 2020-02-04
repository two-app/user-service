package request

import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.headers.RawHeader
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import pdi.jwt.{Jwt, JwtClaim}
import response.ErrorResponse
import response.ErrorResponse.AuthorizationError

class UserContextTest extends AnyFlatSpec with Matchers {

  "a valid Auth header" should "return the correct user context" in {
    val jwt: String = Jwt.encode(claim = JwtClaim(content = """{"uid": 1, "pid": 2, "cid": 3}"""))
    val ctxEither: Either[ErrorResponse, UserContext] = contextFromValue(s"Bearer $jwt")

    ctxEither.isRight shouldBe true

    ctxEither.map(u => {
      u.uid shouldBe 1
      u.pid shouldBe Some(2)
      u.cid shouldBe Some(3)
    })
  }

  "providing no Auth header" should "return an error" in {
    UserContext.from(HttpRequest()) shouldBe Left(AuthorizationError("Authorization not provided."))
  }

  "providing the Auth header with an empty value" should "return an error" in {
    contextFromValue("") shouldBe Left(AuthorizationError("Authorization not provided."))
  }

  "providing the Auth header without the token" should "return an error" in {
    contextFromValue("Bearer") shouldBe Left(AuthorizationError("Invalid header format."))
  }

  "providing the Auth header without Bearer" should "return an error" in {
    contextFromValue("Bla xxx.yyy.zzz") shouldBe Left(AuthorizationError("Invalid header format."))
  }

  def contextFromValue(value: String): Either[ErrorResponse, UserContext] = {
    UserContext.from(HttpRequest(headers = List(RawHeader("Authorization", value))))
  }

}
