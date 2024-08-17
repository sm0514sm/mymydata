package com.sangminlee.mymydata.views;

import com.sangminlee.mymydata.constant.Author;
import com.sangminlee.mymydata.service.ChannelService;
import com.sangminlee.mymydata.service.ChatService;
import com.sangminlee.mymydata.service.MessageService;
import com.sangminlee.mymydata.util.LimitedSortedAppendOnlyList;
import com.sangminlee.mymydata.vo.Message;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import reactor.core.Disposable;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Route(value = "channel", layout = MainLayout.class)
public class ChannelView extends VerticalLayout implements HasUrlParameter<String>, HasDynamicTitle {

    private static final int HISTORY_SIZE = 20;
    private final ChatService chatService;
    private final MessageService messageService;
    private final ChannelService channelService;
    private final MessageList messageList;
    private final LimitedSortedAppendOnlyList<Message> receivedMessages;
    private String channelId;
    private String channelName;

    public ChannelView(ChatService chatService, MessageService messageService, ChannelService channelService) {
        this.chatService = chatService;
        this.messageService = messageService;
        this.channelService = channelService;

        receivedMessages = new LimitedSortedAppendOnlyList<>(HISTORY_SIZE, Comparator.comparing(Message::sequenceNumber));
        setSizeFull();

        messageList = new MessageList();
        messageList.setSizeFull();
        add(messageList);

        MessageInput messageInput = new MessageInput();
        messageInput.setWidthFull();
        messageInput.addSubmitListener(this::sendMessage);
        add(messageInput);
    }

    @Override
    public void setParameter(BeforeEvent event, String channelId) {
        channelService.getChannel(channelId)
                .ifPresentOrElse(channel -> this.channelName = channel.name(),
                        () -> event.forwardTo(LobbyView.class));
        this.channelId = channelId;
        receivedMessages.clear();
        var subscription = subscribe();
        addDetachListener(v -> subscription.dispose());
    }

    private void sendMessage(MessageInput.SubmitEvent event) {
        var message = event.getValue();
        if (message.isBlank() || message.length() <= 1) {
            return;
        }
        CompletableFuture
                .runAsync(() -> chatService.postMessage(channelId, message, Author.USER))
                .thenAccept(result -> {
                    try {
                        chatService.answerMessage(channelId, message);
                    } catch (Exception e) {
                        messageService.deleteLastUserMessage(channelId);
                    }
                });
    }

    @Override
    public String getPageTitle() {
        return channelName;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        var subscription = subscribe();
        addDetachListener(event -> subscription.dispose());
    }

    private Disposable subscribe() {
        var subscription = messageService.getLiveMessages(channelId).subscribe(this::receiveMessages);
        var lastSeenMessageId = receivedMessages.getLast().map(Message::messageId).orElse(null);
        receiveMessages(messageService.getMessageHistory(channelId, HISTORY_SIZE, lastSeenMessageId));
        return subscription;
    }

    private void receiveMessages(List<Message> incoming) {
        getUI().ifPresent(ui -> ui.access(() -> {
            receivedMessages.addAll(incoming);
            messageList.setItems(receivedMessages.stream()
                    .map(this::createMessageListItem)
                    .toList());
        }));
    }

    private MessageListItem createMessageListItem(Message message) {
        MessageListItem item = new MessageListItem(message.message(), message.timestamp(), message.author());
        item.setUserColorIndex(message.color());
        return item;
    }
}