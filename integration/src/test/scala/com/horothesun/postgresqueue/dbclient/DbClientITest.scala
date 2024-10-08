package com.horothesun.postgresqueue.dbclient

import DbClientITest._
import Models._
import com.horothesun.postgresqueue.Models._
import com.horothesun.postgresqueue.TestDbClient._
import java.time.{LocalDateTime, ZoneOffset}
import munit.CatsEffectSuite
import scala.concurrent.duration.DurationInt

class DbClientITest extends CatsEffectSuite {

  test("get all queues") {
    selfCleaningDbClient.use { db =>
      for {
        _ <- populateQueues(db, queueRows)
        qs <- db.getAllQueues.compile.toList
      } yield qs.toSet
    }.assertEquals(queueRows.toSet)
  }

  test("get all messages") {
    val messageRows = List(
      MessageRow(
        messageId1,
        queueNameA,
        MessageBody("body-01"),
        enqueuedAtUtc = LocalDateTime.of(2022, 9, 24, 18, 54, 0),
        lastReadAtUtc = None,
        dequeuedAtUtc = None
      ),
      MessageRow(
        messageId2,
        queueNameB,
        MessageBody("body-02"),
        enqueuedAtUtc = LocalDateTime.of(2022, 9, 24, 18, 55, 0),
        lastReadAtUtc = None,
        dequeuedAtUtc = None
      )
    )
    selfCleaningDbClient.use { db =>
      for {
        _ <- populateQueues(db, queueRows)
        _ <- populateMessages(db, messageRows)
        ms <- db.getAllMessagesAcrossQueues.compile.toList
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
        enqueuedAtUtc = LocalDateTime.of(2022, 9, 24, 18, 54, 0),
        lastReadAtUtc = None,
        dequeuedAtUtc = Some(LocalDateTime.of(2022, 9, 25, 0, 0, 0))
      ),
      MessageRow(
        messageId2,
        queueNameB,
        MessageBody("body-02"),
        enqueuedAtUtc = LocalDateTime.of(2022, 9, 24, 18, 55, 0),
        lastReadAtUtc = None,
        dequeuedAtUtc = None
      ),
      MessageRow(
        messageId3,
        queueNameA,
        MessageBody("body-03"),
        enqueuedAtUtc = LocalDateTime.of(2022, 9, 24, 18, 56, 0),
        lastReadAtUtc = None,
        dequeuedAtUtc = None
      ),
      MessageRow(
        messageId4,
        queueNameA,
        MessageBody("body-04"),
        enqueuedAtUtc = LocalDateTime.of(2022, 9, 24, 18, 57, 0),
        lastReadAtUtc = None,
        dequeuedAtUtc = None
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
    val now = nowUtc()
    val tenPercentOfVisibilityTimeout = (queueVisibilityTimeoutA.value.toSeconds * 0.1).round
    val messageRows = List(
      MessageRow(
        messageId1,
        queueNameA,
        MessageBody("body-01"),
        enqueuedAtUtc = now.minusMinutes(120),
        lastReadAtUtc = Some(now.minusSeconds(tenPercentOfVisibilityTimeout)),
        dequeuedAtUtc = None
      ),
      MessageRow(
        messageId2,
        queueNameB,
        MessageBody("body-02"),
        enqueuedAtUtc = now.minusMinutes(119),
        lastReadAtUtc = None,
        dequeuedAtUtc = None
      ),
      MessageRow(
        messageId3,
        queueNameA,
        MessageBody("body-03"),
        enqueuedAtUtc = now.minusMinutes(118),
        lastReadAtUtc = None,
        dequeuedAtUtc = None
      ),
      MessageRow(
        messageId4,
        queueNameA,
        MessageBody("body-04"),
        enqueuedAtUtc = now.minusMinutes(117),
        lastReadAtUtc = None,
        dequeuedAtUtc = None
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
    val now = nowUtc()
    val twiceVisibilityTimeout = 2 * queueVisibilityTimeoutA.value.toSeconds
    val messageRows = List(
      MessageRow(
        messageId1,
        queueNameA,
        MessageBody("body-01"),
        enqueuedAtUtc = now.minusMinutes(120),
        lastReadAtUtc = Some(now.minusSeconds(twiceVisibilityTimeout)),
        dequeuedAtUtc = None
      ),
      MessageRow(
        messageId2,
        queueNameB,
        MessageBody("body-02"),
        enqueuedAtUtc = now.minusMinutes(119),
        lastReadAtUtc = None,
        dequeuedAtUtc = None
      ),
      MessageRow(
        messageId3,
        queueNameA,
        MessageBody("body-03"),
        enqueuedAtUtc = now.minusMinutes(118),
        lastReadAtUtc = None,
        dequeuedAtUtc = None
      ),
      MessageRow(
        messageId4,
        queueNameA,
        MessageBody("body-04"),
        enqueuedAtUtc = now.minusMinutes(117),
        lastReadAtUtc = None,
        dequeuedAtUtc = None
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

  def nowUtc(): LocalDateTime = LocalDateTime.now(ZoneOffset.UTC)

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
