# check assignment strategy of consumer group named "test-group"
kafka-consumer-groups --bootstrap-server PLAINTEXT_HOST://localhost:29092 --describe --group test-group --state

# GROUP                     COORDINATOR (ID)          ASSIGNMENT-STRATEGY  STATE           #MEMBERS
# test-group                localhost:29092 (1)       RoundRobinAssigner   Stable          1
