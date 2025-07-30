cd /home/yelena/kafka/kafka_deploy
podman-compose -f kraft-docker-compose.yaml up -d

docker exec -it kafka_deploy_kafka_1 kafka-topics.sh --list --bootstrap-server kafka:9092 

## Removal 
podman-compose -f kraft-docker-compose.yaml down -v
podman network rm kafka_deploy_default
podman volume rm kafka_deploy_kafka_0_data kafka_deploy_kafka_data

## Create topic
podman exec -ti kafka_deploy_kafka-0_1  /opt/bitnami/kafka/bin/kafka-topics.sh --create --topic "supertopic" --bootstrap-server localhost:9092 --partitions 3 --replication-factor 2
## Output
Created topic supertopic.