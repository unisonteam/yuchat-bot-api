package ai.yuchat.bot.examples.service.dto;

public class Incident {
    private final String name;
    private final String region;
    private final Chat channel;
    private String messageId;

    public Incident(String name, String region, Chat channel) {
        this.name = name;
        this.region = region;
        this.channel = channel;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String name() {
        return name;
    }

    public String region() {
        return region;
    }

    public Chat channel() {
        return channel;
    }

    public String messageId() {
        return messageId;
    }
}
