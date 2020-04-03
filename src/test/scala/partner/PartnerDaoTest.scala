package partner

import org.scalatest.funspec.AsyncFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.BeforeAndAfterEach
import cats.effect.IO
import doobie.util.transactor.Transactor.Aux
import db.TransactorUtil
import db.FlywayHelper
import user.UserDao
import user.DoobieUserDao
import user.UserTestArbitraries
import request.UserContext
import couple.CoupleDao
import couple.DoobieCoupleDao

class PartnerDaoTest
    extends AsyncFunSpec
    with Matchers
    with BeforeAndAfterEach
    with UserTestArbitraries {

  val xa: Aux[IO, Unit] = TransactorUtil.transactor()
  val partnerDao: PartnerDao[IO] = new DoobiePartnerDao[IO](xa)
  val userDao: UserDao[IO] = new DoobieUserDao[IO](xa)
  val coupleDao: CoupleDao[IO] = new DoobieCoupleDao[IO](xa)

  override def beforeEach(): Unit = FlywayHelper.cleanMigrate()

  describe("getPartnerId") {
    describe("Connected user") {
      it("should return the PID") {
        val uid = registerUser()
        val pid = registerUser()
        val cid = coupleDao.storeCouple(uid, pid).unsafeRunSync()

        coupleDao.connectUserToPartner(uid, pid, cid).unsafeRunSync()

        val maybePartnerId = partnerDao.getPartnerId(uid).value.unsafeRunSync()

        maybePartnerId shouldBe Option(pid)
      }
    }

    describe("Unconnected user") {
      it("should return None") {
        val uid = registerUser()

        val maybePartnerId: Option[Int] =
          partnerDao.getPartnerId(uid).value.unsafeRunSync()

        maybePartnerId shouldBe None
      }
    }
  }

  def registerUser(): Int =
    userDao
      .storeUser(randomUserRegistration())
      .value
      .unsafeRunSync()
      .right
      .get // uid
}
