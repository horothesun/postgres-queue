package com.horothesun.postgresqueue

import com.horothesun.postgresqueue.TestDbClient._
import munit.CatsEffectSuite

class PostgresQueueClientITest extends CatsEffectSuite {

  test("check queues") {
    testDbClient.use { db =>
      db.getAllQueues.map(_.size)
    }.assertEquals(3)
  }

  test("check messages") {
    testDbClient.use { db =>
      db.getAllMessages.map(_.size)
    }.assertEquals(1)
  }

}
