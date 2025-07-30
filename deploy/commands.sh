## Install podman
apt install podman

## Get source git repo
git clone https://github.com/Scandr/ya-kafka01.git
# Go to deploy dir
cd ya-kafka01/deploy
## Deploy kafka cluster with kraft
podman-compose -f kraft-docker-compose.yaml up -d

## Create topic
podman exec -ti kafka_deploy_kafka-0_1 /opt/bitnami/kafka/bin/kafka-topics.sh --create --topic "supertopic" --bootstrap-server localhost:9092 --partitions 3 --replication-factor 2
## Output
Created topic supertopic.

## Show topics 
docker exec -it kafka_deploy_kafka-0_1 /opt/bitnami/kafka/bin/kafka-topics.sh --list --bootstrap-server localhost:9092  

## Deploy application containers
podman-compose -f application-docker-compose.yaml up -d

## View logs 
## Producer
podman logs kafka_deploy_single_prosucer_1
podman logs kafka_deploy_single_prosucer_2
## Single message consumer
podman logs kafka_deploy_single_consumer_1
podman logs kafka_deploy_single_consumer_2
## Batch consumer
podman logs kafka_deploy_batch_consumer_1
podman logs kafka_deploy_batch_consumer_2

## Removal
## Remove kafka containers and volumes
podman-compose -f kraft-docker-compose.yaml down -v
## Remove application containers and volumes
podman-compose -f application-docker-compose.yaml down -v
## Remove network
podman network rm kafka_deploy_default
## Remove volumes only
podman volume rm kafka_deploy_kafka_0_data kafka_deploy_kafka_data

## Build docker images
podman build -t docker.io/xillah/kafka:batch-consumer -f Dockerfile-BatchMessageConsumer .
podman build -t docker.io/xillah/kafka:single-consumer -f Dockerfile-SingleMessageConsumer .
podman build -t docker.io/xillah/kafka:producer -f Dockerfile-kafkaProducer .