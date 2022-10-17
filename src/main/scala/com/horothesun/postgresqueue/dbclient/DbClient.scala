package com.horothesun.postgresqueue.dbclient

import cats.effect._
import com.horothesun.postgresqueue.dbclient.Models._
import com.horothesun.postgresqueue.Models._
import skunk.implicits._
import skunk.Session

import scala.concurrent.duration.DurationInt

/*
psql test_db --host localhost --port 5432 --username postgres --password --quiet --no-align --tuples-only

EXPLAIN (ANALYZE, COSTS, VERBOSE, BUFFERS, FORMAT JSON)
WITH vts AS (
  SELECT COALESCE(visibility_timeout_sec, 10) AS visibility_timeout_sec
  FROM queues
  WHERE queue_name = 'queue-A'
)
SELECT message_id, queue_name, body, enqueued_at, last_read_at, dequeued_at
FROM messages AS m
WHERE m.queue_name = 'queue-A'
  AND m.dequeued_at IS NULL
  AND (
       m.last_read_at IS NULL
    OR (EXTRACT(EPOCH FROM NOW()) - EXTRACT(EPOCH FROM m.last_read_at)) > ( SELECT * FROM vts )
  )
ORDER BY m.enqueued_at ASC, m.message_id DESC
LIMIT 1;

 */
trait DbClient {
  def insertQueue(queue: QueueRow): IO[Unit]
  def insertMessage(message: MessageRow): IO[Unit]
  def getAllQueues: IO[List[QueueRow]]
  def getAllMessages: IO[List[MessageRow]]
  def getQueue(queueName: QueueName): IO[Option[QueueRow]]
  def getTopMessage(queueName: QueueName): IO[Option[MessageRow]]
  def getAndRemoveTopMessage(queueName: QueueName): IO[Option[MessageRow]]
}

object DbClient {

  def create(session: Session[IO]): DbClient = new DbClient {

    val defaultVisibilityTimeout: QueueVisibilityTimeout = QueueVisibilityTimeout(10.seconds)

    override def insertQueue(queue: QueueRow): IO[Unit] =
      session
        .prepare(sql"INSERT INTO queues VALUES (${QueueRow.codec})".command)
        .use(_.execute(queue))
        .void

    override def insertMessage(message: MessageRow): IO[Unit] =
      session
        .prepare(sql"INSERT INTO messages VALUES (${MessageRow.codec})".command)
        .use(_.execute(message))
        .void

    override def getAllQueues: IO[List[QueueRow]] =
      session.execute(
        sql"""
          SELECT queue_name, visibility_timeout_sec
          FROM queues
        """
          .query(QueueRow.codec)
      )

    override def getAllMessages: IO[List[MessageRow]] =
      session.execute(
        sql"""
          SELECT message_id, queue_name, body, enqueued_at, last_read_at, dequeued_at
          FROM messages
        """.query(MessageRow.codec)
      )

    override def getQueue(queueName: QueueName): IO[Option[QueueRow]] =
      session
        .prepare(
          sql"""
            SELECT queue_name, visibility_timeout_sec
            FROM queues
            WHERE queue_name = ${QueueName.codec}
          """.query(QueueRow.codec)
        )
        .use(ps => ps.option(queueName))

    override def getTopMessage(queueName: QueueName): IO[Option[MessageRow]] =
      session
        .prepare(
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
                OR (EXTRACT(EPOCH FROM NOW()) - EXTRACT(EPOCH FROM m.last_read_at)) > ( SELECT * FROM vts )
              )
            ORDER BY m.enqueued_at ASC, m.message_id DESC
            LIMIT 1
          """.query(MessageRow.codec)
        )
        .use(ps => ps.option(defaultVisibilityTimeout ~ queueName ~ queueName))

    override def getAndRemoveTopMessage(queueName: QueueName): IO[Option[MessageRow]] =
      ???

  }

}
