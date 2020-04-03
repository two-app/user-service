package couple

import db.FlywayHelper
import doobie.util.transactor.Transactor.Aux
import cats.implicits._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.funspec.AsyncFunSpec
import org.scalatest.matchers.should.Matchers

import scala.util.Random
import db.TransactorUtil
import cats.effect.IO
import user.UserDao
import user.DoobieUserDao
import user.UserRegistration

class DoobieCoupleDaoTest
    extends AsyncFunSpec
    with Matchers
    with BeforeAndAfterEach {

  val xa: Aux[IO, Unit] = TransactorUtil.transactor()
  val coupleDao: CoupleDao[IO] = new DoobieCoupleDao[IO](xa)
  val userDao: UserDao[IO] = new DoobieUserDao[IO](xa)

  override def beforeEach(): Unit = FlywayHelper.cleanMigrate()

  describe("storeCouple") {
    it("should generate unique CIDs") {
      def performStore(ur: UserRegistration): Int =
        userDao.storeUser(ur).value.unsafeRunSync().right.get

      val userAndPartners: List[(Int, Int)] = (1 to 5).toList
        .map(unq => (newUser(), newUser()))
        .map(couple => (performStore(couple._1), performStore(couple._2)))

      val cids: List[Int] = userAndPartners.map(couple =>
        coupleDao.storeCouple(couple._1, couple._2).unsafeRunSync()
      )

      cids shouldBe (1 to 5)
    }
  }

  describe("connectUserToPartner") {
    it("should update both users PID and CID") {
      val uid = userDao.storeUser(newUser()).value.unsafeRunSync().right.get
      val pid = userDao.storeUser(newUser()).value.unsafeRunSync().right.get
      val cid = coupleDao.storeCouple(uid, pid).unsafeRunSync()

      coupleDao.connectUserToPartner(uid, pid, cid).unsafeRunSync()

      val user = userDao.getUser(uid).value.unsafeRunSync().get
      val partner = userDao.getUser(pid).value.unsafeRunSync().get

      user.pid.get shouldBe partner.uid
      partner.pid.get shouldBe user.uid
      
      user.cid shouldBe Option(cid)
      partner.cid shouldBe Option(cid)
    }
  }

  describe("getCouple") {
    it("should return the UID and PID of the connected users") {
      val uid = userDao.storeUser(newUser()).value.unsafeRunSync().right.get
      val pid = userDao.storeUser(newUser()).value.unsafeRunSync().right.get
      val cid = coupleDao.storeCouple(uid, pid).unsafeRunSync()

      val coupleRecord: CoupleRecord = coupleDao.getCouple(cid).value.unsafeRunSync().get

      coupleRecord.uid shouldBe uid
      coupleRecord.pid shouldBe pid
    }

    it("should return None for a CID that doesn't exist") {
      val maybeCouple: Option[CoupleRecord] = coupleDao.getCouple(5).value.unsafeRunSync()

      maybeCouple shouldBe None
    }
  }

  def newUser(): UserRegistration =
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
    "quillCoupleTest-" + Random.alphanumeric.take(10).mkString + "@twotest.com"
}
