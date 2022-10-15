package com.horothesun.postgresqueue

import com.horothesun.postgresqueue.DbClient.Models._
import com.horothesun.postgresqueue.Models._
import com.horothesun.postgresqueue.PostgresQueueClientITest._
import com.horothesun.postgresqueue.TestDbClient._
import munit.CatsEffectSuite

import java.time.LocalDateTime
import scala.concurrent.duration.DurationInt

class PostgresQueueClientITest extends CatsEffectSuite {

  test("check queues") {
    selfCleaningDbClient.use { db =>
      for {
        _ <- populateQueues(db, queueRows)
        rs <- db.getAllQueues
      } yield rs
    }.map(rs => assertEquals(rs.size, 3))
  }

  test("check messages") {
    selfCleaningDbClient.use { db =>
      for {
        _ <- populateQueues(db, queueRows)
        _ <- populateMessages(db, messageRows)
        rs <- db.getAllMessages
      } yield rs
    }.map(rs => assertEquals(rs.size, 1))
  }

}

object PostgresQueueClientITest {

  val queueRows: List[QueueRow] = List(
    QueueRow(QueueName("queue-A"), Some(QueueVisibilityTimeout(1.seconds))),
    QueueRow(QueueName("queue-B"), None),
    QueueRow(QueueName("queue-C"), Some(QueueVisibilityTimeout(3.seconds)))
  )

  val messageRows: List[MessageRow] = List(
    MessageRow(
      MessageId(1),
      QueueName("queue-A"),
      MessageBody("body-01"),
      enqueuedAt = LocalDateTime.of(2022, 9, 24, 18, 54, 0),
      lastReadAt = None,
      dequeuedAt = None
    )
  )

}
