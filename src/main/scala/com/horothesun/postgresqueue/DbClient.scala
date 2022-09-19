package com.horothesun.postgresqueue

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
trait DbClient {}
