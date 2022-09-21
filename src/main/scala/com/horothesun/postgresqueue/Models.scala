package com.horothesun.postgresqueue

import skunk.codec.all._
import skunk.Encoder

import scala.concurrent.duration.Duration

object Models {

  object Queue {
    case class Name(value: String)
    object Name {
      implicit val skunkEncoder: Encoder[Queue.Name] = varchar(50).contramap[Queue.Name](_.value)
    }
    case class VisibilityTimeout(value: Duration)
    object VisibilityTimeout {
      implicit val skunkEncoder: Encoder[Queue.VisibilityTimeout] =
        int8.contramap[Queue.VisibilityTimeout](_.value.toSeconds)
    }
  }

  object Message {
    case class Id(value: Int)
    object Id {
      implicit val skunkEncoder: Encoder[Message.Id] = int4.contramap[Message.Id](_.value)
    }
    case class Body(value: String)
    object Body {
      implicit val skunkEncoder: Encoder[Message.Body] = varchar(500).contramap[Message.Body](_.value)
    }
  }

}
