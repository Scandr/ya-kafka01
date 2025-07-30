# Задание 1
## 0 Подготовка
Машина для работы: WSL ubuntu на Windows 10
Docker не нашелся через apt, но нашелся podman -> ставим пакеты командой
```
sudo apt isntall -y podman podman-compose podman-docker
```
podman - для работы с контейнерами
podman-compose - для управления контейнерами
podman-docker - чтобы система не ругалась на команду docker

## 1 Развертывание
Создать файл kraft-docker-compose.yaml со следующим содержимым:

```
version: "3.9"

services:
  kafka:
    image: registry.hub.docker.com/bitnami/kafka:3.4
    ports:
      - "9094:9094"
    environment:
      - KAFKA_ENABLE_KRAFT=yes
      - ALLOW_PLAINTEXT_LISTENER=yes
      - KAFKA_CFG_NODE_ID=0
      - KAFKA_CFG_PROCESS_ROLES=broker,controller
      - KAFKA_CFG_CONTROLLER_LISTENER_NAMES=CONTROLLER
      - KAFKA_CFG_CONTROLLER_QUORUM_VOTERS=0@kafka:9093
      - KAFKA_KRAFT_CLUSTER_ID=abcdefghijklmnopqrstuv
      - KAFKA_CFG_LISTENERS=PLAINTEXT://:9092,CONTROLLER://:9093,EXTERNAL://:9094
      - KAFKA_CFG_ADVERTISED_LISTENERS=PLAINTEXT://kafka:9092,EXTERNAL://127.0.0.1:9094
      - KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP=CONTROLLER:PLAINTEXT,EXTERNAL:PLAINTEXT,PLAINTEXT:PLAINTEXT
    volumes:
      - kafka_data:/bitnami/kafka
  ui:
    image: registry.hub.docker.com/provectuslabs/kafka-ui:v0.7.0
    ports:
      - "8080:8080"
    environment:
      - KAFKA_CLUSTERS_0_BOOTSTRAP_SERVERS=kafka:9092
      - KAFKA_CLUSTERS_0_NAME=kraft 
volumes:
  kafka_data: 
```

Из директории с эти файлом запустить podman-compose:
```
podman-compose -f kraft-docker-compose.yaml up -d
```
-f - файл docker-compose, из которого деплоить
-d - запустить процесс в фоне и вернуть консоль

## 2 Проверка
Посмотреть статус контейнеров:
```
podman ps -a
```
Вывод:
```
63f47f3b5f60  registry.hub.docker.com/bitnami/kafka:3.4              /opt/bitnami/scri...  24 seconds ago  Up 24 seconds  0.0.0.0:9094->9094/tcp  kafka_deploy_kafka_1
aec717ffef62  registry.hub.docker.com/provectuslabs/kafka-ui:v0.7.0  /bin/sh -c java -...  23 seconds ago  Up 23 seconds  0.0.0.0:8080->8080/tcp  kafka_deploy_ui_1
```
Должно быть состояние Up
Запустить тестовую команду 
```
docker exec -it kafka_deploy_kafka_1 kafka-topics.sh --list --bootstrap-server kafka:9092
```
Команда, видимо, возвращает список топиков. Вернет ничего, т.к. топиков еще нет
Также можно потыкать UI по адресу http://localhost:8080/

## 3 Параметры
Посмотреть выставленные параметры можно через UI: 
http://localhost:8080/ -> brokers -> brocker 0 -> Configs
Рядом можно посмотреть директорию логов: 
http://localhost:8080/ -> brokers -> brocker 0 -> Log directories

Или через команду: 
```
docker exec -it kafka_deploy_kafka_1 kafka-configs.sh --describe --broker 0 --bootstrap-server kafka:9092 --all
```
kafka-configs.sh - скрипт для работы с конфигами
--describe - вывести
--broker 0 - ID борокера (он пока один)
--bootstrap-server - сервер
--all - все конфиги

### Параметры при развертывании 
- KAFKA_ENABLE_KRAFT=yes - включить kraft
- ALLOW_PLAINTEXT_LISTENER - разрешить слушателей 
- KAFKA_ENABLE_KRAFT=yes включает режим KRaft, при котором Kafka работает без Zookeeper.
- KAFKA_CFG_PROCESS_ROLE=broker,controller ― узел выполняет две роли: контроллера и брокера. Другими словам, он управляет конфигурацией, а также хранит данные и обрабатывает запросы.
- KAFKA_CFG_CONTROLLER_LISTENER_NAMES=CONTROLLER указывает слушателя для контроллера, который используется для его связи с другими узлами.
- KAFKA_CFG_CONTROLLER_QUORUM_VOTERS=0@kafka-0:9093 ― список всех контроллеров кластера с их идентификаторами, адресами и портами.
- KAFKA_KRAFT_CLUSTER_ID=somevalue ― уникальный идентификатор кластера. Значение одинаково для всех контроллеров и брокеров, его мы получим после первого запуска.
- KAFKA_CFG_LISTENERS задаёт адреса и порты, на которых Kafka принимает соединения (внутренние и внешние). Здесь мы говорим Kafka, где искать входящие запросы. В конфигурационном файле вы указываете три слушателя: PLAINTEXT://:9092, CONTROLLER://:9093 и EXTERNAL://:9094. Первые два предназначены для внутренних коммуникаций внутри Docker, а последний ― для внешних соединений с хост-машиной.
- KAFKA_CFG_ADVERTISED_LISTENERS ― адреса и порты, которые Kafka «рекламирует» для подключения клиентов. Клиенты Kafka должны знать, как подключиться к брокеру. Если брокер Kafka работает внутри докер-сети, этот параметр может указывать на адрес в этой сети. Если нужно подключаться к брокеру локально, здесь указывают соответствующий порт и протокол. В вашем конфигурационном файле вы объявляете два слушателя PLAINTEXT://kafka-0:9092 и EXTERNAL://127.0.0.1:9094. Первый слушатель обслуживает внутренние коммуникации в Docker, второй ― внешние коммуникации с хост-машиной.
- KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP определяет, какой протокол безопасности используется для каждого типа соединений. Это помогает обеспечить безопасность данных при передаче между брокером и клиентами. В вашем конфигурационном файле вы указываете, что все слушатели (CONTROLLER, EXTERNAL, PLAINTEXT) будут использовать протокол PLAINTEXT, который не предполагает шифрования данных.

## Доп материалы
Дополнительные материалы изучать необязательно, но они помогут глубже разобраться в пройденной теме. Приятного и полезного изучения! 
1. Почтальон в мире IT: для чего нужен брокер сообщений Apache Kafka и как он устроен ― статья в блоге Яндекс Практикума. https://practicum.yandex.ru/blog/broker-soobsheniy-apache-kafka/ 
2. ZooKeeper: A Distributed Coordination Service for Distributed Applications ― официальная документация. https://zookeeper.apache.org/doc/r3.8.4/zookeeperOver.html
3. KRaft: Apache Kafka Without ZooKeeper ― статья на сайте Confluent. https://developer.confluent.io/learn/kraft/
4. Что такое Dead Letter Queue (DLQ) ― статья в блоге Яндекс Облака. https://yandex.cloud/ru/docs/message-queue/concepts/dlq
5. Message Delivery Guarantees ― статья на сайте Confluent. https://docs.confluent.io/kafka/design/delivery-semantics.html

# Задание 2
Для сборки JAR файлов приложения использовался maven в составе IntelliJ IDEA Community Edition 2025.1.2. Приложение состоит из 3х контейнеров: 

- producer
- single_consumer
- batch_consumer

и разворачиватеся с помощью [application-docker-compose.yaml](./deploy/application-docker-compose.yaml). Для Kafka docker-compose был переработан (см. [kraft-docker-compose.yaml](./deploy/kraft-docker-compose.yaml))

Java исходники и pom.xml для сборки приложения лежат в соответствующих директориях:

- [BatchMessageConsumer](./BatchMessageConsumer/)
- [kafkaProducer](./kafkaProducer/)
- [SingleMessageConsumer](./SingleMessageConsumer/)

Основная наcтроqка компонентов приложения проводится через переменные окружения, задающиеся в [application-docker-compose.yaml](./deploy/application-docker-compose.yaml).