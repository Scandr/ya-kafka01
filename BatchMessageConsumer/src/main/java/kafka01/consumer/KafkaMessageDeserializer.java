package kafka01.consumer;

import org.apache.kafka.common.serialization.Deserializer;
import java.nio.ByteBuffer;

public class KafkaMessageDeserializer implements Deserializer<KafkaMessage>  {
    @Override
    public KafkaMessage deserialize(String topic, byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);

        int nameSize = buffer.getInt();
        byte[] nameBytes = new byte[nameSize];
        buffer.get(nameBytes);
        String name = new String(nameBytes);

        int contentSize = buffer.getInt();
        byte[] contentBytes = new byte[contentSize];
        buffer.get(contentBytes);
        String content = new String(contentBytes);

        int id = buffer.getInt();
        int urgency = buffer.getInt();

        return new KafkaMessage(id, name, content, urgency);
    }
}
