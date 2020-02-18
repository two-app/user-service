package authentication

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import config.Config
import spray.json.DefaultJsonProtocol.{jsonFormat2, _}
import spray.json.{RootJsonFormat, _}

import scala.concurrent.ExecutionContext.Implicits.{global => ec}
import scala.concurrent.Future

case class Credentials(uid: Int, password: String)

case class Tokens(accessToken: String, refreshToken: String)

object Tokens {
  implicit val TokensFormat: RootJsonFormat[Tokens] = jsonFormat2(Tokens.apply)
}

trait AuthenticationDao {
  def storeCredentials(uid: Int, password: String): Future[Tokens]

  def createTokens(uid: Int, pid: Option[Int], cid: Option[Int]): Future[Tokens]
}

class AuthenticationServiceDao extends AuthenticationDao {
  implicit val CredentialsFormat: RootJsonFormat[Credentials] = jsonFormat2(Credentials)
  implicit val system: ActorSystem = ActorSystem()
  implicit val materialise: ActorMaterializer = ActorMaterializer()

  override def storeCredentials(uid: Int, password: String): Future[Tokens] = {
    val credentials = Credentials(uid, password)
    val request = HttpRequest(
      method = HttpMethods.POST,
      uri = Config.getProperty("service.authentication.location") + "/credentials",
      entity = HttpEntity(ContentTypes.`application/json`, credentials.toJson.compactPrint)
    )

    Http().singleRequest(request).flatMap(r => Unmarshal(r).to[Tokens])
  }

  override def createTokens(uid: Int, pid: Option[Int], cid: Option[Int]): Future[Tokens] = {
    case class TokenRequest(uid: Int, pid: Option[Int], cid: Option[Int])
    implicit val f: RootJsonFormat[TokenRequest] = jsonFormat3(TokenRequest)
    val request = HttpRequest(
      method = HttpMethods.POST,
      uri = Config.getProperty("service.authentication.location") + "/tokens",
      entity = HttpEntity(ContentTypes.`application/json`, TokenRequest(uid, pid, cid).toJson.compactPrint)
    )

    Http().singleRequest(request).flatMap(r => Unmarshal(r).to[Tokens])
  }
}
