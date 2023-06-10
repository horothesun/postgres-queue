package com.horothesun.postgresqueue

import cats.effect._
import munit.CatsEffectSuite

class PostgresQueueClientITest extends CatsEffectSuite {

  test("integration test 1") {
    IO(42).assertEquals(42)
  }

}

object PostgresQueueClientITest {}
