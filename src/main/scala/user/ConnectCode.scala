package user

import config.Config
import org.hashids.Hashids

object ConnectCode {
  def toId(connectCode: String): Option[Int] = {
    val idParts = new Hashids(Config.getProperty("hashids.salt"), 6).decode(connectCode)
    if (idParts.isEmpty) None else Option(idParts(0).toInt)
  }
}
