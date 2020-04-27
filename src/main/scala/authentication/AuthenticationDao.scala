package authentication

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.{
  ContentTypes,
  HttpEntity,
  HttpMethods,
  HttpRequest
}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import config.Config
import spray.json.DefaultJsonProtocol.{jsonFormat2, _}
import spray.json.{RootJsonFormat, _}

import scala.concurrent.ExecutionContext.Implicits.{global => ec}
import scala.concurrent.Future
import cats.effect.IO
import cats.effect.Async
import scala.util.Failure
import scala.util.Success
import com.typesafe.scalalogging.Logger

case class Credentials(uid: Int, password: String)

case class Tokens(accessToken: String, refreshToken: String)

object Tokens {
  implicit val TokensFormat: RootJsonFormat[Tokens] = jsonFormat2(Tokens.apply)
}

trait AuthenticationDao[F[_]] {
  def storeCredentials(uid: Int, password: String): F[Tokens]

  def createTokens(uid: Int, pid: Option[Int], cid: Option[Int]): F[Tokens]
}

class AuthenticationServiceDao[F[_]: Async] extends AuthenticationDao[F] {
  implicit val CredentialsFormat: RootJsonFormat[Credentials] = jsonFormat2(
    Credentials
  )
  implicit val system: ActorSystem = ActorSystem()
  implicit val materialise: ActorMaterializer = ActorMaterializer()

  val logger: Logger = Logger[AuthenticationServiceDao[F]]

  override def storeCredentials(uid: Int, password: String): F[Tokens] = {
    val credentials = Credentials(uid, password)
    val request = HttpRequest(
      method = HttpMethods.POST,
      uri =
        Config.getProperty("service.authentication.location") + "/credentials",
      entity = HttpEntity(
        ContentTypes.`application/json`,
        credentials.toJson.compactPrint
      )
    )

    logger.info("Performing POST to authentication service on /credentials")

    val futureTokens: Future[Tokens] =
      Http().singleRequest(request).flatMap(r => Unmarshal(r).to[Tokens])
    httpFutureToF(futureTokens)
  }

  override def createTokens(
      uid: Int,
      pid: Option[Int],
      cid: Option[Int]
  ): F[Tokens] = {
    case class TokenRequest(uid: Int, pid: Option[Int], cid: Option[Int])
    implicit val f: RootJsonFormat[TokenRequest] = jsonFormat3(TokenRequest)
    val request = HttpRequest(
      method = HttpMethods.POST,
      uri = Config.getProperty("service.authentication.location") + "/tokens",
      entity = HttpEntity(
        ContentTypes.`application/json`,
        TokenRequest(uid, pid, cid).toJson.compactPrint
      )
    )

    logger.info(
      s"Performing POST to authentication service on /tokens with UID ${uid}, PID ${pid} and CID ${cid}"
    )

    val futureTokens: Future[Tokens] =
      Http().singleRequest(request).flatMap(r => Unmarshal(r).to[Tokens])

    httpFutureToF(futureTokens)
  }

  private def httpFutureToF[A](future: Future[A]): F[A] = {
    Async[F].async { cb =>
      future.onComplete {
        case Success(value)     => cb(Right(value))
        case Failure(exception) => cb(Left(exception))
      }
    }
  }
}
