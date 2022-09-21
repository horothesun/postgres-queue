package com.horothesun.postgresqueue

import cats.effect._
import cats.implicits._
import com.horothesun.postgresqueue.DbClient.Models._
import natchez.Trace.Implicits.noop
import skunk._
import skunk.codec.all._
import skunk.implicits._

object TestDbClient {

  val session: Resource[IO, Session[IO]] =
    Session
      .single[IO](
        host = "localhost",
        port = 5432,
        user = "postgres",
        database = "test_db",
        password = Some("test_pwd")
      )

  def insertQueues(s: Session[IO], queues: List[QueueRow]): IO[Unit] =
    ???

  def insertMessages(s: Session[IO], messages: List[MessageRow]): IO[Unit] =
    ???

  def truncateAllTables(s: Session[IO]): IO[Unit] =
    List(
      sql"TRUNCATE TABLE queues",
      sql"TRUNCATE TABLE messages"
    )
      .map(_.command)
      .traverse_(s.execute)

}
