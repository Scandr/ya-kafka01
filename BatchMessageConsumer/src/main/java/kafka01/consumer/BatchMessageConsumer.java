package kafka01.consumer;

// Import libs
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

// Custom consumer class
public class BatchMessageConsumer {
    private static KafkaConsumer<String, KafkaMessage> createKafkaConsumer() {
        // Get settings from env vars
        String bootstrapServersConfig = System.getenv("BOOTSTRAP_SERVERS_CONFIG");
        String consumerGroupName = System.getenv("CONSUMER_GROUP_NAME");
        int maxPollRecordsConfig = Integer.parseInt(System.getenv("MAX_POLL_RECORDS_CONFIG"));
        int fetchMinBytesConfig = Integer.parseInt(System.getenv("FETCH_MIN_BYTES_CONFIG"));
        int fetchMaxWaitMsConfig = Integer.parseInt(System.getenv("FETCH_MAX_WAIT_MS_CONFIG"));

        Properties props = new Properties();
        /// Настройка консьюмера
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServersConfig);  // Адрес брокера Kafka
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupName);        // Уникальный идентификатор группы
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, kafka01.consumer.KafkaMessageDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");        // Начало чтения с самого начала
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");           // Автоматический коммит смещений
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "6000");           // Время ожидания активности от консьюмера
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecordsConfig); // Максимальное число сообщений в батче
        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, fetchMinBytesConfig); // Минимальный размер батча в байтах
        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, fetchMaxWaitMsConfig); //

        return new KafkaConsumer<>(props);
    }
    public static void main(String[] args) {
        // Get settings from env vars
        String topicName = System.getenv("KAFKA_TOPIC_NAME");
        // Create consumer
        KafkaConsumer<String, KafkaMessage> consumer = createKafkaConsumer();

        // Подписка на топик
        consumer.subscribe(Collections.singletonList(topicName));

        // Чтение сообщений в бесконечном цикле
        try {
            while (true) {
                try {
                    ConsumerRecords<String, KafkaMessage> records = consumer.poll(Duration.ofSeconds(100));
                    if (!records.isEmpty()) {
                        System.out.printf("New batch of massages:%n");
                        for (ConsumerRecord<String, KafkaMessage> record : records) {
                            System.out.printf("A message is received: key = %s, partition = %d, offset = %d%n",
                                    record.key(), record.partition(), record.offset());
                            System.out.printf("Message contents: id = %d, name = %s, content = %s, urgency = %d%n", record.value().getId(), record.value().getName(), record.value().getContent(), record.value().getUrgency());
                        }
                        System.out.printf("End of the batch. Committing offset%n");
                        consumer.commitSync();
                    }
                } catch (Exception e) {
                    System.out.println("Error during Kafka polling: " + e);
                }
            }
        } finally {
            consumer.close();
        }
    }
}