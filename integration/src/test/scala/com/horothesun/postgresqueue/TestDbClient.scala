package com.horothesun.postgresqueue

import cats.effect._
import cats.syntax.all._
import com.horothesun.postgresqueue.Models._
import dbclient._
import dbclient.Models._
import natchez.Trace.Implicits.noop
import scala.concurrent.duration.DurationInt
import skunk.syntax.all._
import skunk.Session

object TestDbClient {

  val testDefaultVisibilityTimeout: QueueVisibilityTimeout = QueueVisibilityTimeout(5.seconds)

  val selfCleaningDbClient: Resource[IO, DbClient] =
    session.flatMap(s => Resource.make(DbClient.create(s, testDefaultVisibilityTimeout))(_ => truncateAllTables(s)))

  private def session: Resource[IO, Session[IO]] =
    Session.single[IO](
      host = "localhost",
      port = 5432,
      user = "postgres",
      database = "test_db",
      password = Some("test_pwd")
    )

  def populateQueues(db: DbClient, rs: List[QueueRow]): IO[Unit] =
    rs.traverse_(db.insertQueue)

  def populateMessages(db: DbClient, rs: List[MessageRow]): IO[Unit] =
    rs.traverse_(db.insertMessage)

  private def truncateAllTables(s: Session[IO]): IO[Unit] =
    s.execute(sql"TRUNCATE TABLE queues, messages".command).void

}
