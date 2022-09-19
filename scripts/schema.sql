CREATE TABLE IF NOT EXISTS queues (
  queue_id serial PRIMARY KEY,
  visibility_timeout_sec INT
);

CREATE TABLE IF NOT EXISTS messages (
  message_id serial PRIMARY KEY,
  queue_id INT NOT NULL,
  body VARCHAR ( 500 ) NOT NULL,
  enqueued_at TIMESTAMP NOT NULL,
  last_read_at TIMESTAMP,
  dequeued_at TIMESTAMP,
  FOREIGN KEY ( queue_id ) REFERENCES queues ( queue_id )
);

--CREATE INDEX IF NOT EXISTS messages_queue_id_dequeued_at_last_read_at_idx
--ON messages (
--  queue_id,
--  dequeued_at NULLS FIRST,
--  last_read_at ASC NULLS FIRST
--);

CREATE INDEX IF NOT EXISTS messages_queue_id_dequeued_at_idx
ON messages (
  queue_id,
  dequeued_at NULLS FIRST
);

--CREATE INDEX IF NOT EXISTS messages_last_read_at_idx
--ON messages ( last_read_at ASC NULLS FIRST );

--CREATE INDEX IF NOT EXISTS messages_enqueued_at_message_id_idx
--ON messages (
--  enqueued_at DESC,
--  message_id DESC
--);
