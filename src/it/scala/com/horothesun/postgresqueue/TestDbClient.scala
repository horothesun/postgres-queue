package com.horothesun.postgresqueue

import cats.effect._
import cats.implicits._
import com.horothesun.postgresqueue.DbClient._
import com.horothesun.postgresqueue.DbClient.Models._
import com.horothesun.postgresqueue.Models._
import natchez.Trace.Implicits.noop
import skunk.implicits._
import skunk.Session

import java.time.LocalDateTime
import scala.concurrent.duration._

object TestDbClient {

  val testDbClient: Resource[IO, DbClient] =
    session.flatMap { s =>
      val db = DbClient.create(s)
      Resource
        .make(IO(()) /* populateQueues(db) >> populateMessages(db)*/ )(_ => IO(()) /*truncateAllTables(s)*/ )
        .as(db)
    }

  def session: Resource[IO, Session[IO]] =
    Session
      .single[IO](
        host = "localhost",
        port = 5432,
        user = "postgres",
        database = "test_db",
        password = Some("test_pwd")
      )

  private def populateQueues(dbClient: DbClient): IO[Unit] =
    List(
      QueueRow(QueueName("queue-A"), Some(QueueVisibilityTimeout(1.seconds))),
      QueueRow(QueueName("queue-B"), None),
      QueueRow(QueueName("queue-C"), Some(QueueVisibilityTimeout(3.seconds)))
    ).traverse_(dbClient.insertQueue)

  private def populateMessages(dbClient: DbClient): IO[Unit] =
    List(
      MessageRow(
        MessageId(1),
        QueueName("queue-A"),
        MessageBody("body-01"),
        enqueuedAt = LocalDateTime.of(2022, 9, 24, 18, 54, 0),
        lastReadAt = None,
        dequeuedAt = None
      )
    ).traverse_(dbClient.insertMessage)

  private def truncateAllTables(s: Session[IO]): IO[Unit] =
    List(
      sql"TRUNCATE TABLE queues",
      sql"TRUNCATE TABLE messages"
    ).map(_.command)
      .traverse_(s.execute)

  def getTableNames(s: Session[IO]): IO[List[String]] =
    s.execute(
      sql"SELECT DISTINCT table_name FROM information_schema.columns"
        .query(skunk.codec.all.name)
    )

}
