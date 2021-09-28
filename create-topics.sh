kafka-topics \
--bootstrap-server localhost:9092 \
--create \
--topic purchases  \
--replication-factor 1 \
--partitions 12


kafka-topics \
--bootstrap-server localhost:9092 \
--create \
--topic stock-events  \
--replication-factor 1 \
--partitions 12


kafka-topics \
--bootstrap-server localhost:9092 \
--create \
--topic stock-state  \
--replication-factor 1 \
--partitions 12 \
--config cleanup.policy=compact

