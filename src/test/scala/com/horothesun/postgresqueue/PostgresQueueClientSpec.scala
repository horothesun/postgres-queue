package com.horothesun.postgresqueue

import cats.effect._
import munit.CatsEffectSuite
import skunk.Session

class PostgresQueueClientSpec extends CatsEffectSuite {

  val session: Resource[IO, Session[IO]] = TestDbClient.session

  test("integration test 1") {
    session.use { s =>
      IO(42)
    }.assertEquals(42)
  }

}
