package user

import scala.util.Random
import spray.json.RootJsonFormat
import spray.json.DefaultJsonProtocol._

trait UserTestArbitraries {
  implicit val UserRegistrationTestFormat: RootJsonFormat[UserRegistration] =
    jsonFormat6(UserRegistration.apply)

  def randomUserRegistration(): UserRegistration =
    UserRegistration
      .from(
        "First",
        "Last",
        randomEmail(),
        "TestPassword",
        acceptedTerms = true,
        ofAge = true
      )
      .right
      .get

  def randomEmail(): String =
    "userServiceTest-" + Random.alphanumeric.take(10).mkString + "@twotest.com"
}
