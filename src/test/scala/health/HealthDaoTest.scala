package health

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import db.DatabaseTestMixin
import org.scalatest.BeforeAndAfterEach
import cats.effect.IO

class HealthDaoTest
    extends AnyFunSpec
    with Matchers
    with BeforeAndAfterEach
    with DatabaseTestMixin {

  val healthDao: HealthDao[IO] = new DoobieHealthDao[IO](xa)
  override def beforeEach(): Unit = cleanMigrate()

  describe("performSimpleStatement") {
    it("should return 1") {
      healthDao.performSimpleStatement().value.unsafeRunSync() shouldBe Right(1)
    }
  }
}
