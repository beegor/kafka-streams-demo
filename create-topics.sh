kafka-topics \
--bootstrap-server localhost:9092 \
--create \
--topic products  \
--replication-factor 1 \
--partitions 12 \
--config cleanup.policy=compact

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


kafka-topics \
--bootstrap-server localhost:9092 \
--create \
--topic product-profit-report  \
--replication-factor 1 \
--partitions 12 \
--config cleanup.policy=compact


