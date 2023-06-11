package com.horothesun.postgresqueue.dbclient

import cats.effect._
import cats.implicits._
import com.horothesun.postgresqueue.Models._
import fs2.Stream
import skunk._
import skunk.implicits._
import Models._

/*

PGPASSWORD=test_pwd psql --host localhost --port 5432 --username postgres --dbname test_db --quiet --no-align --tuples-only

EXPLAIN (ANALYZE, COSTS, VERBOSE, BUFFERS, FORMAT JSON)
WITH vts AS (
  SELECT COALESCE(visibility_timeout_sec, 5) AS visibility_timeout_sec
  FROM queues
  WHERE queue_name = 'queue-A'
)
SELECT message_id, queue_name, body, enqueued_at, last_read_at, dequeued_at
FROM messages AS m
WHERE m.queue_name = 'queue-A'
  AND m.dequeued_at IS NULL
  AND (
       m.last_read_at IS NULL
    OR EXTRACT(EPOCH FROM TIMEZONE('UTC', NOW())) - EXTRACT(EPOCH FROM TIMEZONE('UTC', m.last_read_at)) > ( SELECT * FROM vts )
  )
ORDER BY m.enqueued_at ASC, m.message_id DESC
LIMIT 1;

 */
trait DbClient {
  def insertQueue(queue: QueueRow): IO[Unit]
  def insertMessage(message: MessageRow): IO[Unit]
  def getAllQueues: Stream[IO, QueueRow]
  def getAllMessagesAcrossQueues: Stream[IO, MessageRow]
  def getAllMessages(queueName: QueueName): Stream[IO, MessageRow]
  def getQueue(queueName: QueueName): IO[Option[QueueRow]]
  def getTopMessage(queueName: QueueName): IO[Option[MessageRow]]
  def getAndRemoveTopMessage(queueName: QueueName): IO[Option[MessageRow]]
}

object DbClient {

  val insertQueueCommand: Command[QueueRow] =
    sql"INSERT INTO queues VALUES (${QueueRow.codec})".command

  val insertMessageCommand: Command[MessageRow] =
    sql"INSERT INTO messages VALUES (${MessageRow.codec})".command

  val allQueuesQuery: Query[Void, QueueRow] =
    sql"""
      SELECT queue_name, visibility_timeout_sec
      FROM queues
    """.query(QueueRow.codec)

  val allMessagesAcrossQueuesQuery: Query[Void, MessageRow] =
    sql"""
      SELECT message_id, queue_name, body, enqueued_at, last_read_at, dequeued_at
      FROM messages
      ORDER BY enqueued_at ASC, message_id DESC
    """.query(MessageRow.codec)

  val allMessagesQuery: Query[QueueName, MessageRow] =
    sql"""
      SELECT message_id, queue_name, body, enqueued_at, last_read_at, dequeued_at
      FROM messages
      WHERE queue_name = ${QueueName.codec}
      ORDER BY enqueued_at ASC, message_id DESC
    """.query(MessageRow.codec)

  val queueQuery: Query[QueueName, QueueRow] =
    sql"""
      SELECT queue_name, visibility_timeout_sec
      FROM queues
      WHERE queue_name = ${QueueName.codec}
    """.query(QueueRow.codec)

  val topMessageQuery: Query[QueueVisibilityTimeout *: QueueName *: QueueName *: EmptyTuple, MessageRow] =
    sql"""
      WITH vts AS (
        SELECT COALESCE(visibility_timeout_sec, ${QueueVisibilityTimeout.codec}) AS visibility_timeout_sec
        FROM queues
        WHERE queue_name = ${QueueName.codec}
      )
      SELECT message_id, queue_name, body, enqueued_at, last_read_at, dequeued_at
      FROM messages AS m
      WHERE m.queue_name = ${QueueName.codec}
        AND m.dequeued_at IS NULL
        AND (
             m.last_read_at IS NULL
          OR EXTRACT(EPOCH FROM TIMEZONE('UTC', NOW())) - EXTRACT(EPOCH FROM TIMEZONE('UTC', m.last_read_at)) > ( SELECT * FROM vts )
        )
      ORDER BY m.enqueued_at ASC, m.message_id DESC
      LIMIT 1
    """.query(MessageRow.codec)

  def create(session: Session[IO], defaultVisibilityTimeout: QueueVisibilityTimeout): IO[DbClient] =
    (
      session.prepare(insertQueueCommand),
      session.prepare(insertMessageCommand),
      session.prepare(allQueuesQuery),
      session.prepare(allMessagesAcrossQueuesQuery),
      session.prepare(allMessagesQuery),
      session.prepare(queueQuery),
      session.prepare(topMessageQuery)
    ).parTupled.map {
      case (
            insertQueuePrep,
            insertMessagePrep,
            allQueuesPrep,
            allMessagesAcrossQueuesPrep,
            allMessagesPrep,
            queuePrep,
            topMessagePrep
          ) =>
        new DbClient {

          override def insertQueue(queue: QueueRow): IO[Unit] = insertQueuePrep.execute(queue).void

          override def insertMessage(message: MessageRow): IO[Unit] = insertMessagePrep.execute(message).void

          override def getAllQueues: Stream[IO, QueueRow] = allQueuesPrep.stream(Void, chunkSize = 64)

          override def getAllMessagesAcrossQueues: Stream[IO, MessageRow] =
            allMessagesAcrossQueuesPrep.stream(Void, chunkSize = 64)

          override def getAllMessages(queueName: QueueName): Stream[IO, MessageRow] =
            allMessagesPrep.stream(queueName, chunkSize = 64)

          override def getQueue(queueName: QueueName): IO[Option[QueueRow]] = queuePrep.option(queueName)

          override def getTopMessage(queueName: QueueName): IO[Option[MessageRow]] =
            topMessagePrep.option((defaultVisibilityTimeout, queueName, queueName))

          override def getAndRemoveTopMessage(queueName: QueueName): IO[Option[MessageRow]] =
            ???

        }
    }

}
