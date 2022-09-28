package com.horothesun.postgresqueue.DbClient

import cats.effect._
import com.horothesun.postgresqueue.DbClient.Models._
import com.horothesun.postgresqueue.Models._
import skunk.implicits._
import skunk.Session

/*
psql --host localhost --port 5432 --username postgres --password --quiet --no-align --tuples-only

EXPLAIN (ANALYZE, COSTS, VERBOSE, BUFFERS, FORMAT JSON)
WITH vts AS (
  SELECT COALESCE(visibility_timeout_sec, 10) AS visibility_timeout_sec
  FROM queues
  WHERE queue_id = 123456
)
SELECT *
FROM messages AS m
WHERE m.queue_id = 123456
  AND m.dequeued_at IS NULL
  AND (
    m.last_read_at IS NULL
    OR NOW() - m.last_read_at > ( SELECT * FROM vts ) * interval '1 second'
  )
ORDER BY m.enqueued_at DESC, m.message_id DESC
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

    override def insertQueue(queue: QueueRow): IO[Unit] =
      session
        .prepare(sql"INSERT INTO queues VALUES ${QueueRow.codec}".command)
        .use(_.execute(queue))
        .void

    override def insertMessage(message: MessageRow): IO[Unit] =
      session
        .prepare(sql"INSERT INTO messages VALUES ${MessageRow.codec}".command)
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
      session.option(
        sql"""
          SELECT queue_name, visibility_timeout_sec
          FROM queues
        """
          .query(QueueRow.codec)
      )

    override def getTopMessage(queueName: QueueName): IO[Option[MessageRow]] =
      ???

    override def getAndRemoveTopMessage(queueName: QueueName): IO[Option[MessageRow]] =
      ???

  }

}
