package kafka01.consumer;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class SingleMessageConsumer {
    private static KafkaConsumer<String, KafkaMessage> createKafkaConsumer() {
        String bootstrapServersConfig = System.getenv("BOOTSTRAP_SERVERS_CONFIG");
        Properties props = new Properties();
        /// Настройка консьюмера
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServersConfig);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "single");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, kafka01.consumer.KafkaMessageDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");        // Начало чтения с самого начала
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");           // Автоматический коммит смещений
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "6000");           // Время ожидания активности от консьюмера

        return new KafkaConsumer<>(props);
    }

    public static void main(String[] args) {
        String topicName = System.getenv("KAFKA_TOPIC_NAME");

        KafkaConsumer<String, KafkaMessage> consumer = createKafkaConsumer();

        // Подписка на топик
        consumer.subscribe(Collections.singletonList(topicName));

        // Чтение сообщений в бесконечном цикле
        try {
            while (true) {
                try {
                    ConsumerRecords<String, KafkaMessage> records = consumer.poll(Duration.ofSeconds(100));
                    for (ConsumerRecord<String, KafkaMessage> record : records) {
                        System.out.printf("A message is received: key = %s, partition = %d, offset = %d%n",
                                record.key(), record.partition(), record.offset());
                        System.out.printf("Message contents: id = %d, name = %s, content = %s, urgency = %d%n", record.value().getId(), record.value().getName(), record.value().getContent(), record.value().getUrgency());
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
