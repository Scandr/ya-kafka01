package kafka01.producer;

// Import libs
import org.apache.kafka.common.serialization.Serializer;
import java.nio.ByteBuffer;

public class KafkaMessageSerializer implements Serializer<KafkaMessage> {
    @Override
    public byte[] serialize(String topic, KafkaMessage msg) {
        // Transform name value into bytes
        byte[] nameBytes = msg.getName().getBytes();
        // Get size of the name
        int nameSize = nameBytes.length;

        // Transform content value into bytes
        byte[] contentBytes = msg.getContent().getBytes();
        // Get size of the content
        int contentSize = contentBytes.length;

        // Allocate buffer for the whole class = int name size + name value + int conetent size + content value + int id + int urgency 
        ByteBuffer buffer = ByteBuffer.allocate(4 + nameSize + 4 + contentSize + 4 + 4);
        // Write name size to buffer (int = 4 bytes)
        buffer.putInt(nameSize);
        // Write name value
        buffer.put(nameBytes);
        // Write content size to buffer (int = 4 bytes)
        buffer.putInt(contentSize);
        // Write content value
        buffer.put(contentBytes);
        // Write id to buffer (int = 4 bytes)
        buffer.putInt(msg.getId());
        // Write urgency to buffer (int = 4 bytes)
        buffer.putInt(msg.getUrgency());
        
        return buffer.array();
    }
}
