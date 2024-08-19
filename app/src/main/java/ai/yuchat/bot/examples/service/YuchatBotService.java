package ai.yuchat.bot.examples.service;

import ai.yuchat.bot.event.model.Update;
import ai.yuchat.bot.examples.client.YuchatHttpClient;
import ai.yuchat.bot.examples.service.dto.Chat;
import ai.yuchat.bot.examples.service.dto.Incident;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class YuchatBotService {
    private static final String MESSAGE_TEMPLATE = """
            ***Incident*** ❗
              | name: _%s_,
              | region: _%s_,
              | status: CREATED
            """;

    private static final Map<String, Chat> channels = Map.of(
            "Архангельск", new Chat("ZpmkcDhQtE", "w:aBTzGGLmwS", "Архангельск"),
            "Москва", new Chat("ZpmkcDhQtE", "w:aBTxfEtmb2", "Москва"),
            "ЦУМС", new Chat("ZpmkcDhQtE", "w:aBTw50Gmci", "ЦУМС")
    );

    private final YuchatHttpClient yuchatHttpClient;

    // Local state of the service.
    private final Map<String, Incident> openIncidents;
    private final AtomicLong lastSeenUpdateId;
    private final ScheduledExecutorService executorService;

    public YuchatBotService(YuchatHttpClient yuchatHttpClient) {
        this.yuchatHttpClient = yuchatHttpClient;

        this.executorService = Executors.newSingleThreadScheduledExecutor();
        this.openIncidents = new ConcurrentHashMap<>();
        this.lastSeenUpdateId = new AtomicLong(0);
    }

    public void listenUpdates() {
        System.out.println("Listening for updates...");
        executorService.scheduleAtFixedRate(this::processUpdates, 0, 1, TimeUnit.SECONDS);
    }

    void processUpdates() {
        updates().forEach(this::processUpdate);
    }

    void processUpdate(Update update) {
        long updateId = update.getUpdateId();
        System.out.println("Processing update: " + update);

        long localLastSeenUpdateId = lastSeenUpdateId.get();
        if (localLastSeenUpdateId > updateId) {
            System.out.println("Got the update that has been already processed, skip. UpdateId: " + update);
            return;
        }

        // Remember updateId as the last seen updateId.
        lastSeenUpdateId.compareAndSet(localLastSeenUpdateId, updateId);

        String markdownText = update.getNewChatMessage().getMarkdown().trim();

        if (markdownText.startsWith("/create")) {
            createIncident(markdownText, update);
            return;
        }

        if (markdownText.startsWith("/edit")) {
            editIncident(markdownText, update);
            return;
        }

        if (markdownText.startsWith("/delete")) {
            deleteIncident(markdownText, update);
            return;
        }

        // Reply to my message
        if (update.getNewChatMessage().getParentMessageId() != null
                && update.getNewChatMessage().getParentMessageAuthor().equals("aBIopDgFsG")) {
            String whoReplied = update.getNewChatMessage().getAuthor();
            String myMsgId = update.getNewChatMessage().getParentMessageId();

            Optional<Incident> incident = openIncidents.values().stream().filter(
                   i -> i.messageId().equals(myMsgId)
            ).findFirst();

            if (incident.isEmpty()) {
                System.out.println("Can not find incident with msg id " + myMsgId);
                return;
            }

            var incidentRaw = incident.get();

            String personalChatId = yuchatHttpClient.createPersonalChat(incidentRaw.channel().workspaceId(), whoReplied);

            yuchatHttpClient.send("Вы подписались на обнобления по инциденту.", incidentRaw.channel().workspaceId(), personalChatId);

            yuchatHttpClient.forward(
                    "Вот он: ",
                    incidentRaw.channel().workspaceId(),
                    incidentRaw.channel().chatId(),
                    incidentRaw.messageId(),
                    personalChatId
            );
        }
    }

    private void deleteIncident(String markdownText, Update update) {
        System.out.println("Delete incident from message: " + markdownText);
        Incident incidentToDelete = openIncidents.get(parseIncident(markdownText, update).name());

        yuchatHttpClient.delete(
                incidentToDelete.channel().workspaceId(),
                incidentToDelete.channel().chatId(),
                incidentToDelete.messageId()
        );

        openIncidents.remove(incidentToDelete.name());
    }

    List<Update> updates() {
        System.out.println("Get updates [offset=" + lastSeenUpdateId.get() + "]");
        var updates = yuchatHttpClient.getUpdates(lastSeenUpdateId.get());
        System.out.println(updates);
        return updates;
    }

    void createIncident(String fromMessage, Update causeUpdate) {
        System.out.println("Creating incident from message: " + fromMessage);
        Incident incident = parseIncident(fromMessage, causeUpdate);

        var messageId = send(
                MESSAGE_TEMPLATE.formatted(incident.name(), incident.region()), incident.channel()
        );

        incident.setMessageId(messageId);
        openIncidents.put(incident.name(), incident);
    }

    void editIncident(String fromMessage, Update causeUpdate) {
        System.out.println("Editing incident from message: " + fromMessage);
        Incident incident = parseIncident(fromMessage, causeUpdate);
        Incident incidentToUpdate = openIncidents.get(incident.name());

        String original = MESSAGE_TEMPLATE.formatted(incidentToUpdate.name(), incidentToUpdate.region());
        String updatedText = original.replace("CREATED", "UPDATED");

        edit(updatedText, incidentToUpdate.channel(), incidentToUpdate.messageId());
    }

    String send(String msg, Chat channel) {
        if (channel == null) {
            System.out.println("Chat not found");
            return null;
        }

        return yuchatHttpClient.send(msg, channel.workspaceId(), channel.chatId()).getMessageId();
    }

    void edit(String msg, Chat channel, String messageId) {
        if (channel == null) {
            System.out.println("Channel not found");
            return;
        }

        yuchatHttpClient.edit(msg, channel.workspaceId(), channel.chatId(), messageId);
    }

    private Incident parseIncident(String fromMessage, Update causeUpdate) {
        String[] split = fromMessage.split(" ");
        String name = split[1];
        String region = split[2];

        System.out.println("Looking for channel: " + region);
        return new Incident(name, region, findChannel(region));
    }

    private Chat findChannel(String name) {
        return channels.get(name.trim());
    }
}

