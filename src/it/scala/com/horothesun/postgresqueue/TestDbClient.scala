package com.horothesun.postgresqueue

import cats.effect._
import cats.implicits._
import com.horothesun.postgresqueue.DbClient.Models._
import com.horothesun.postgresqueue.DbClient._
import natchez.Trace.Implicits.noop
import skunk.implicits._
import skunk.Session

object TestDbClient {

  val selfCleaningDbClient: Resource[IO, DbClient] =
    session.flatMap { s =>
      val db = DbClient.create(s)
      Resource.make(IO(()))(_ => truncateAllTables(s)).as(db)
    }

  private def session: Resource[IO, Session[IO]] =
    Session
      .single[IO](
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
