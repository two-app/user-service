package db

import com.github.mauricio.async.db.mysql.exceptions.MySQLException

trait DatabaseError

object DatabaseError {
  def fromException(e: MySQLException): DatabaseError = {
    e.errorMessage.errorCode match {
      case 1062 => DuplicateEntry()
    }
  }

  final case class Other() extends DatabaseError

  final case class DuplicateEntry() extends DatabaseError
}
