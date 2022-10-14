package com.horothesun.postgresqueue

import com.horothesun.postgresqueue.TestDbClient._
import munit.CatsEffectSuite

class PostgresQueueClientITest extends CatsEffectSuite {

  test("integration test 1") {
    testDbClient.use { db =>
      db.getAllQueues
    }
      .assertEquals(List.empty)
  }

  test("integration test 2") {
    session.use { s =>
      getTableNames(s).map(_.filter(Set("messages", "queues").contains).sorted)
    }
      .assertEquals(List("messages", "queues"))
  }

}
