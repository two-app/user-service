package db

import java.sql.SQLException

trait DatabaseError
object DuplicateRecordError extends DatabaseError

object DatabaseError {
  def fromException(e: SQLException): DatabaseError = {
    e.getErrorCode() match {
      case 1062 => DuplicateRecordError
      case _ => throw new RuntimeException("Failed to read SQLException", e)
    }
  }
}
