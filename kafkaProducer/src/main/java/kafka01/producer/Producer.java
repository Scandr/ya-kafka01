package kafka01.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class Producer {

    private static KafkaProducer<String, KafkaMessage> createKafkaProducer() {
        String bootstrapServersConfig = System.getenv("BOOTSTRAP_SERVERS_CONFIG");
        Properties props = new Properties();
        // Конфигурация продюсера – адрес сервера, сериализаторы для ключа и значения.
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServersConfig);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, kafka01.producer.KafkaMessageSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "1");
        props.put("retries", 3);

        return new KafkaProducer(props);
    }

    public static void main(String[] args) {

        String topicName = System.getenv("KAFKA_TOPIC_NAME");
        int msgPeriod = Integer.parseInt(System.getenv("MESSAGE_PERIOD"));

        KafkaProducer<String, KafkaMessage> producer = createKafkaProducer();

        try {
            int messageID = 0;
            while (true) {
                try {
                    String messageKey = String.format("key-%2d", messageID);
                    String messageText = String.format("message-%2d", messageID);

                    // Отправка сообщения
                    KafkaMessage myMsg = new KafkaMessage(messageID, String.format("msg%d", messageID), messageText, 0);
                    producer.send(new ProducerRecord<String, KafkaMessage>(topicName, messageKey, myMsg));

                    System.out.println(String.format("Message ID = %2d. Sleep for %d ms", messageID, msgPeriod));
                    messageID++;
                    try {
                        Thread.sleep(msgPeriod);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                } catch (Exception e) {
                    System.out.println("Error during sending: " + e);
                }
            }
        } finally {
            // Закрытие продюсера
            producer.close();
        }
    }
}