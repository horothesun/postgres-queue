package com.horothesun.postgresqueue.DbClient

import com.horothesun.postgresqueue.Models._
import skunk.codec.all._
import skunk.Codec

import java.time.LocalDateTime

object Models {

  case class QueueRow(
    name: QueueName,
    visibilityTimeout: Option[QueueVisibilityTimeout]
  )
  object QueueRow {

    implicit val codec: Codec[QueueRow] =
      (
        QueueName.codec ~
          QueueVisibilityTimeout.codec.opt
      ).gimap[QueueRow]
  }

  case class MessageRow(
    id: MessageId,
    queueName: QueueName,
    body: MessageBody,
    enqueuedAt: LocalDateTime,
    lastReadAt: Option[LocalDateTime],
    dequeuedAt: Option[LocalDateTime]
  )
  object MessageRow {
    implicit val codec: Codec[MessageRow] =
      (
        MessageId.codec ~
          QueueName.codec ~
          MessageBody.codec ~
          timestamp ~
          timestamp.opt ~
          timestamp.opt
      ).gimap[MessageRow]
  }

}
