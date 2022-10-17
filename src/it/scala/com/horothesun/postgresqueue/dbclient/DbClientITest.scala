package com.horothesun.postgresqueue.dbclient

import com.horothesun.postgresqueue.dbclient.Models._
import com.horothesun.postgresqueue.Models._
import com.horothesun.postgresqueue.dbclient.DbClientITest._
import com.horothesun.postgresqueue.TestDbClient._
import munit.CatsEffectSuite

import java.time.LocalDateTime
import scala.concurrent.duration.DurationInt

class DbClientITest extends CatsEffectSuite {

  test("get all queues") {
    selfCleaningDbClient.use { db =>
      for {
        _ <- populateQueues(db, queueRows)
        qs <- db.getAllQueues
      } yield qs.toSet
    }.assertEquals(queueRows.toSet)
  }

  test("get all messages") {
    val messageRows = List(
      MessageRow(
        messageId1,
        queueNameA,
        MessageBody("body-01"),
        enqueuedAt = LocalDateTime.of(2022, 9, 24, 18, 54, 0),
        lastReadAt = None,
        dequeuedAt = None
      ),
      MessageRow(
        messageId2,
        queueNameB,
        MessageBody("body-02"),
        enqueuedAt = LocalDateTime.of(2022, 9, 24, 18, 55, 0),
        lastReadAt = None,
        dequeuedAt = None
      )
    )
    selfCleaningDbClient.use { db =>
      for {
        _ <- populateQueues(db, queueRows)
        _ <- populateMessages(db, messageRows)
        ms <- db.getAllMessages
      } yield ms.toSet
    }.assertEquals(messageRows.toSet)
  }

  test("get queue from name") {
    selfCleaningDbClient.use { db =>
      for {
        _ <- populateQueues(db, queueRows)
        q <- db.getQueue(queueNameA)
      } yield q
    }.assertEquals(Some(queueRowA))
  }

  test("get top message from queue name w/o any defined lastReadAt") {
    val messageRows = List(
      MessageRow(
        messageId1,
        queueNameA,
        MessageBody("body-01"),
        enqueuedAt = LocalDateTime.of(2022, 9, 24, 18, 54, 0),
        lastReadAt = None,
        dequeuedAt = Some(LocalDateTime.of(2022, 9, 25, 0, 0, 0))
      ),
      MessageRow(
        messageId2,
        queueNameB,
        MessageBody("body-02"),
        enqueuedAt = LocalDateTime.of(2022, 9, 24, 18, 55, 0),
        lastReadAt = None,
        dequeuedAt = None
      ),
      MessageRow(
        messageId3,
        queueNameA,
        MessageBody("body-03"),
        enqueuedAt = LocalDateTime.of(2022, 9, 24, 18, 56, 0),
        lastReadAt = None,
        dequeuedAt = None
      ),
      MessageRow(
        messageId4,
        queueNameA,
        MessageBody("body-04"),
        enqueuedAt = LocalDateTime.of(2022, 9, 24, 18, 57, 0),
        lastReadAt = None,
        dequeuedAt = None
      )
    )
    selfCleaningDbClient.use { db =>
      for {
        _ <- populateQueues(db, queueRows)
        _ <- populateMessages(db, messageRows)
        m <- db.getTopMessage(queueNameA)
      } yield m.map(_.id)
    }.assertEquals(Some(messageId3))
  }

  test("get top message from queue name with lastReadAt within visibility timeout") {
    val now = LocalDateTime.now()
    val tenPercentOfVisibilityTimeout = (queueVisibilityTimeoutA.value.toSeconds * 0.1).round
    val messageRows = List(
      MessageRow(
        messageId1,
        queueNameA,
        MessageBody("body-01"),
        enqueuedAt = now.minusMinutes(120),
        lastReadAt = Some(LocalDateTime.now().minusSeconds(tenPercentOfVisibilityTimeout)),
        dequeuedAt = None
      ),
      MessageRow(
        messageId2,
        queueNameB,
        MessageBody("body-02"),
        enqueuedAt = now.minusMinutes(119),
        lastReadAt = None,
        dequeuedAt = None
      ),
      MessageRow(
        messageId3,
        queueNameA,
        MessageBody("body-03"),
        enqueuedAt = now.minusMinutes(118),
        lastReadAt = None,
        dequeuedAt = None
      ),
      MessageRow(
        messageId4,
        queueNameA,
        MessageBody("body-04"),
        enqueuedAt = now.minusMinutes(117),
        lastReadAt = None,
        dequeuedAt = None
      )
    )
    selfCleaningDbClient.use { db =>
      for {
        _ <- populateQueues(db, queueRows)
        _ <- populateMessages(db, messageRows)
        m <- db.getTopMessage(queueNameA)
      } yield m.map(_.id)
    }.assertEquals(Some(messageId3))
  }

  test("get top message from queue name with lastReadAt outside visibility timeout") {
    val now = LocalDateTime.now()
    val doubleVisibilityTimeout = 2 * queueVisibilityTimeoutA.value.toSeconds
    val messageRows = List(
      MessageRow(
        messageId1,
        queueNameA,
        MessageBody("body-01"),
        enqueuedAt = now.minusMinutes(120),
        lastReadAt = Some(LocalDateTime.now().minusSeconds(doubleVisibilityTimeout)),
        dequeuedAt = None
      ),
      MessageRow(
        messageId2,
        queueNameB,
        MessageBody("body-02"),
        enqueuedAt = now.minusMinutes(119),
        lastReadAt = None,
        dequeuedAt = None
      ),
      MessageRow(
        messageId3,
        queueNameA,
        MessageBody("body-03"),
        enqueuedAt = now.minusMinutes(118),
        lastReadAt = None,
        dequeuedAt = None
      ),
      MessageRow(
        messageId4,
        queueNameA,
        MessageBody("body-04"),
        enqueuedAt = now.minusMinutes(117),
        lastReadAt = None,
        dequeuedAt = None
      )
    )
    selfCleaningDbClient.use { db =>
      for {
        _ <- populateQueues(db, queueRows)
        _ <- populateMessages(db, messageRows)
        m <- db.getTopMessage(queueNameA)
      } yield m.map(_.id)
    }.assertEquals(Some(messageId1))
  }

}

object DbClientITest {

  val queueNameA: QueueName = QueueName("queue-A")
  val queueNameB: QueueName = QueueName("queue-B")
  val queueNameC: QueueName = QueueName("queue-C")

  val queueVisibilityTimeoutA: QueueVisibilityTimeout = QueueVisibilityTimeout(10.seconds)
  val queueVisibilityTimeoutC: QueueVisibilityTimeout = QueueVisibilityTimeout(30.seconds)

  val queueRowA: QueueRow = QueueRow(queueNameA, Some(queueVisibilityTimeoutA))
  val queueRowB: QueueRow = QueueRow(queueNameB, None)
  val queueRowC: QueueRow = QueueRow(queueNameC, Some(queueVisibilityTimeoutC))

  val queueRows: List[QueueRow] = List(queueRowA, queueRowB, queueRowC)

  val messageId1: MessageId = MessageId(1)
  val messageId2: MessageId = MessageId(2)
  val messageId3: MessageId = MessageId(3)
  val messageId4: MessageId = MessageId(4)

}
