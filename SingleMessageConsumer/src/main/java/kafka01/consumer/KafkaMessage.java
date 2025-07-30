package kafka01.consumer;

public class KafkaMessage {
    private int id;
    private String name;
    private String content;
    private int urgency;

    // Конструктор
    public KafkaMessage(int id, String name, String content, int urgency) {
        this.id = id;
        this.name = name;
        this.content = content;
        this.urgency = urgency;
    }
    // Геттеры и сеттеры
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }

    public int getUrgency() {
        return urgency;
    }
}