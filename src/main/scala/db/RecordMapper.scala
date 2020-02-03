package db {

  trait RecordMapper[Record, T] {
    def from(record: Record): T

    def to(model: T): Record
  }

}