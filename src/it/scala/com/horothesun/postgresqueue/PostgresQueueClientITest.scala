package com.horothesun.postgresqueue

import com.horothesun.postgresqueue.TestDbClient.testDbClient
import munit.CatsEffectSuite

class PostgresQueueClientITest extends CatsEffectSuite {

  test("integration test 1") {
    testDbClient.use { db =>
      db.getAllQueues
    }
      .assertEquals(List.empty)
  }

}
