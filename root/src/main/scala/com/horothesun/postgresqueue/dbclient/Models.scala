package com.horothesun.postgresqueue.dbclient

import com.horothesun.postgresqueue.Models._
import java.time.LocalDateTime
import skunk.Codec
import skunk.codec.all._

object Models {

  case class QueueRow(
    name: QueueName,
    visibilityTimeout: Option[QueueVisibilityTimeout]
  )
  object QueueRow {
    implicit val codec: Codec[QueueRow] = (QueueName.codec *: QueueVisibilityTimeout.codec.opt).to[QueueRow]
  }

  case class MessageRow(
    id: MessageId,
    queueName: QueueName,
    body: MessageBody,
    enqueuedAtUtc: LocalDateTime,
    lastReadAtUtc: Option[LocalDateTime],
    dequeuedAtUtc: Option[LocalDateTime]
  )
  object MessageRow {
    implicit val codec: Codec[MessageRow] =
      (MessageId.codec *: QueueName.codec *: MessageBody.codec *: timestamp *: timestamp.opt *: timestamp.opt)
        .to[MessageRow]
  }

}
