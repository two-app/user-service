package health

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes
import cats.data.EitherT
import scala.reflect.ClassTag
import response.ErrorResponse.InternalError
import response.ErrorResponse
import cats.effect.IO
import java.sql.SQLException
import cats.effect.ContextShift
import scala.concurrent.ExecutionContext

class HealthRouteTest extends AnyFunSpec with Matchers with ScalatestRouteTest {

  // Implicit values required for testing
  implicit val contextShift: ContextShift[IO] =
    IO.contextShift(ExecutionContext.global)
  implicit val timer = IO.timer(ExecutionContext.global)

  describe("healthy") {
    val healthRoute: Route =
      new HealthRouteDispatcher[IO](() => EitherT.pure(1)).route

    it("should return 200 OK") {
      Get("/health") ~> healthRoute ~> check {
        response.status shouldBe StatusCodes.OK
      }
    }
  }

  describe("unhealthy") {
    describe("controlled failure") {
      val unhealthyRoute: Route = new HealthRouteDispatcher[IO](() =>
        EitherT.leftT(InternalError())
      ).route

      it("should return an error response") {
        Get("/health") ~> unhealthyRoute ~> check {
          entityAs[ErrorResponse] shouldBe InternalError()
        }
      }
    }

    describe("side effect failure") {
      val unhealthyRoute: Route = new HealthRouteDispatcher(() =>
        EitherT.left(IO.raiseError(new SQLException("Test Message")))
      ).route

      it("should return an error response") {
        Get("/health") ~> unhealthyRoute ~> check {
          entityAs[ErrorResponse] shouldBe InternalError()
        }
      }
    }
  }

}
