package db {

  trait RecordMapper[Record, T] {
    def fromRecord(record: Record): T

    def toRecord(model: T): Record
  }

}