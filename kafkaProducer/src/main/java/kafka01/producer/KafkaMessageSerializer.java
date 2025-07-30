package kafka01.producer;

import org.apache.kafka.common.serialization.Serializer;
import java.nio.ByteBuffer;

public class KafkaMessageSerializer implements Serializer<KafkaMessage> {
    @Override
    public byte[] serialize(String topic, KafkaMessage msg) {
        byte[] nameBytes = msg.getName().getBytes();
        int nameSize = nameBytes.length;
//        System.out.printf("nameSize = %d%n", nameSize);

        byte[] contentBytes = msg.getContent().getBytes();
        int contentSize = contentBytes.length;
//        System.out.printf("contentSize = %d%n", contentSize);

        ByteBuffer buffer = ByteBuffer.allocate(4 + nameSize + 4 + contentSize + 4 + 4);
        buffer.putInt(nameSize);
        buffer.put(nameBytes);
        buffer.putInt(contentSize);
        buffer.put(contentBytes);
        buffer.putInt(msg.getId());
        buffer.putInt(msg.getUrgency());
        return buffer.array();
    }
}
