package health

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import cats.effect.IO
import cats.data.EitherT
import java.sql.SQLException
import response.ErrorResponse.InternalError

class HealthServiceTest extends AnyFunSpec with Matchers {

  describe("healthy") {
    val healthService: HealthService[IO] =
      new HealthServiceImpl[IO](() => EitherT.pure(1))

    it("should return healthy") {
      healthService.getHealth().value.unsafeRunSync() shouldBe Right(1)
    }
  }

  describe("unhealthy") {
    val errorMessage = "Test Error"
    val healthService: HealthService[IO] =
      new HealthServiceImpl[IO](() => EitherT.leftT(new SQLException(errorMessage)))

    it("should return an Internal Error") {
      healthService.getHealth().value.unsafeRunSync() shouldBe Left(
        InternalError(
          s"Failed to interact with database. Exception message: ${errorMessage}"
        )
      )
    }
  }

}
