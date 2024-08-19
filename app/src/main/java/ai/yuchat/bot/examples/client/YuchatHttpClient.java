package ai.yuchat.bot.examples.client;

import ai.yuchat.bot.api.event.ChatMessageApi;
import ai.yuchat.bot.event.invoker.ApiClient;
import ai.yuchat.bot.event.invoker.ApiException;
import ai.yuchat.bot.event.model.*;
import ai.yuchat.bot.examples.service.YuchatBotService;

import java.util.List;

/**
 * HTTP client that wraps the generated ChatMessageApi.
 */
public class YuchatHttpClient {
    private final ChatMessageApi api;

    public YuchatHttpClient(ApiClient client) {
        this.api = new ChatMessageApi(client);
    }

    /**
     * Returns the list of updates. Given fromUpdateId = 5, the method returns updates with ids 6,7,...8.
     * If you want to read the next updates, then set fromUpdateId = 8. For more information, see usage
     * example {@link YuchatBotService}.
     *
     * @param fromUpdateId the offset.
     */
    public List<Update> getUpdates(long fromUpdateId) {
        try {
            return api.getUpdates(fromUpdateId, 100,  500);
        } catch (ApiException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    /** Sends a text message msg to the chat with given workspaceId and chatId. */
    public SendChatMessagePublicV1Response send(String msg, String workspaceId, String chatId) {
        var message = new SendChatMessagePublicV1Request();
        message.setWorkspaceId(workspaceId);
        message.setChatId(chatId);
        message.setMarkdown(msg);

        try {
            return api.sendChatMessage(message);
        } catch (ApiException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /** Updates the message with the given newTextMsg. */
    public void edit(String newTextMsg, String workspaceId, String chatId, String chatMessageId) {
        var message = new EditChatMessagePublicV1Request();
        message.setWorkspaceId(workspaceId);
        message.setChatId(chatId);
        message.setChatMessageId(chatMessageId);
        message.setNewMarkdown(newTextMsg);

        try {
            api.editChatMessage(message);
        } catch (ApiException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /** Deletes the message with workspaceId, chatId and chatMessageId. */
    public void delete(String workspaceId, String chatId, String chatMessageId) {
        var deleteRequest = new DeleteChatMessagePublicV1Request();
        deleteRequest.setWorkspaceId(workspaceId);
        deleteRequest.setChatId(chatId);
        deleteRequest.setChatMessageId(chatMessageId);

        try {
            api.deleteChatMessage(deleteRequest);
        } catch (ApiException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Forwards the given message with workspaceId, fromChatId and fromChatMessageId to the
     * chat with toChatId in the same workspace with the title.
     */
    public void forward(String title, String workspaceId, String fromChatId,
                        String fromChatMessageId, String toChatId) {

        var forwardRequest = new ForwardChatMessagePublicV1Request();
        forwardRequest.setMarkdown(title);
        forwardRequest.setWorkspaceId(workspaceId);
        forwardRequest.setSourceChatId(fromChatId);
        forwardRequest.setSourceChatMessageId(fromChatMessageId);
        forwardRequest.setTargetChatId(toChatId);

        try {
            api.forwardChatMessage(forwardRequest);
        } catch (ApiException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /** Creates the personal chat with participantId and returns the chatId. */
    public String createPersonalChat(String workspaceId, String participantId) {
        var createChatRequest = new CreateChatPublicV1Request();
        createChatRequest.setWorkspaceId(workspaceId);
        createChatRequest.setChatType(ChatType.PERSONAL);
        createChatRequest.setParticipants(List.of(participantId));

        try {
            return api.createChat(createChatRequest).getChatId();
        } catch (ApiException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
