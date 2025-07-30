package kafka01.consumer;

// Import libs
import org.apache.kafka.common.serialization.Deserializer;
import java.nio.ByteBuffer;

public class KafkaMessageDeserializer implements Deserializer<KafkaMessage>  {
    @Override
    public KafkaMessage deserialize(String topic, byte[] data) {
        // Get data into buffer var
        ByteBuffer buffer = ByteBuffer.wrap(data);
        
        // Get first int size part (4 bytes)
        int nameSize = buffer.getInt();
        // Allocate var for name value
        byte[] nameBytes = new byte[nameSize];
        // Get name value from buffer
        buffer.get(nameBytes);
        // Form a string
        String name = new String(nameBytes);

        // Get int size part after name value
        int contentSize = buffer.getInt();
        // Allocate var for content value
        byte[] contentBytes = new byte[contentSize];
        // Get content value from buffer
        buffer.get(contentBytes);
        // Form a string
        String content = new String(contentBytes);

        // Get next int size part 
        int id = buffer.getInt();
        // And the last int size part
        int urgency = buffer.getInt();

        // Return KafkaMessage class filled with values from buffer
        return new KafkaMessage(id, name, content, urgency);
    }
}
