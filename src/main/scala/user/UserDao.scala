package user

import java.util.Date

import cats.data.EitherT
import cats.data.OptionT
import com.typesafe.scalalogging.Logger
import db.DatabaseError
import db.DuplicateRecordError
import db.DateTimeModule._
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor
import cats.effect.Bracket
import cats.implicits._
import java.sql.SQLException
import java.time.Instant

trait UserDao[F[_]] {
  def storeUser(ur: UserRegistration): EitherT[F, DatabaseError, Int]

  def getUser(uid: Int): OptionT[F, UserRecord]
}

class DoobieUserDao[F[_]: Bracket[*[_], Throwable]](
    val xa: Transactor[F]
) extends UserDao[F] {
  val logger: Logger = Logger(classOf[DoobieUserDao[F]])

  override def storeUser(
      userRegistration: UserRegistration
  ): EitherT[F, DatabaseError, Int] =
    EitherT(
      UserSql
        .insert(UserRecord.asInsertRecord(userRegistration))
        .attemptSql
        .transact(xa)
    ).leftMap(DatabaseError.fromException)

  override def getUser(uid: Int): OptionT[F, UserRecord] = OptionT(
    UserSql.select(uid).transact(xa)
  )
}

private object UserSql {
  def insert(ur: UserRecord): ConnectionIO[Int] =
    sql"""
         | INSERT INTO user (pid, cid, email, first_name,
         |                   last_name, accepted_terms,
         |                   of_age, created_at)
         | VALUES (${ur.pid}, ${ur.cid}, ${ur.email}, ${ur.firstName},
         |         ${ur.lastName}, ${ur.acceptedTerms}, ${ur.ofAge},
         |         ${ur.createdAt})
         |""".stripMargin.update
      .withUniqueGeneratedKeys[Int]("uid")

  def select(uid: Int): ConnectionIO[Option[UserRecord]] =
    sql"""
         | SELECT uid, pid, cid, email, first_name,
         |        last_name, accepted_terms,
         |        of_age, created_at
         | FROM user
         | WHERE uid = $uid
         |""".stripMargin
      .query[UserRecord]
      .option
}

final case class UserRecord(
    uid: Int,
    pid: Option[Int],
    cid: Option[Int],
    email: String,
    firstName: String,
    lastName: String,
    acceptedTerms: Boolean,
    ofAge: Boolean,
    createdAt: Instant
)

object UserRecord {
  def asInsertRecord(ur: UserRegistration): UserRecord = {
    new UserRecord(
      uid = 0,
      pid = None,
      cid = None,
      email = ur.email,
      firstName = ur.firstName,
      lastName = ur.lastName,
      acceptedTerms = ur.acceptedTerms,
      ofAge = ur.ofAge,
      createdAt = Instant.now()
    )
  }
}
