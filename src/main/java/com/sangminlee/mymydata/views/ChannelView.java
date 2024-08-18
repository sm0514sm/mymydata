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
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import reactor.core.Disposable;

import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 채널 뷰를 정의하는 클래스입니다.
 * 이 클래스는 특정 채널의 메시지를 표시하고, 새 메시지를 보내는 기능을 제공합니다.
 */
@Route(value = "channel", layout = MainLayout.class)
public class ChannelView extends VerticalLayout implements HasUrlParameter<String>, HasDynamicTitle {

    private static final int HISTORY_SIZE = 20;
    private final ChatService chatService;
    private final MessageService messageService;
    private final ChannelService channelService;
    private final MessageList messageList;
    private final LimitedSortedAppendOnlyList<Message> receivedMessages;
    private final Upload upload;
    private String channelId;
    private String channelName;

    /**
     * ChannelView의 생성자입니다.
     * 필요한 서비스와 UI 컴포넌트를 초기화합니다.
     *
     * @param chatService    채팅 관련 기능을 제공하는 서비스
     * @param messageService 메시지 관련 기능을 제공하는 서비스
     * @param channelService 채널 관련 기능을 제공하는 서비스
     * @param chatClient     AI 채팅 클라이언트
     */
    public ChannelView(ChatService chatService, MessageService messageService, ChannelService channelService, ChatClient chatClient) {
        this.chatService = chatService;
        this.messageService = messageService;
        this.channelService = channelService;

        receivedMessages = new LimitedSortedAppendOnlyList<>(HISTORY_SIZE, Comparator.comparing(Message::sequenceNumber));
        setSizeFull();

        messageList = new MessageList();
        messageList.setSizeFull();
        add(messageList);

        upload = setupUploader();
        add(upload);

        MessageInput messageInput = new MessageInput();
        messageInput.setWidthFull();
        messageInput.addSubmitListener(this::sendMessage);
        add(messageInput);
    }

    /**
     * 파일 업로드를 위한 Upload 컴포넌트를 설정합니다.
     *
     * @return 설정된 Upload 컴포넌트
     */
    private static Upload setupUploader() {
        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes("application/png", ".png");
        upload.setMaxFiles(1);

        upload.addSucceededListener(event -> {
            InputStream inputStream = buffer.getInputStream();
            InputStreamResource inputStreamResource = new InputStreamResource(inputStream);
            VaadinSession.getCurrent().setAttribute("uploadedFile", inputStreamResource);
        });
        upload.addFileRemovedListener(event -> VaadinSession.getCurrent().setAttribute("uploadedFile", null));
        return upload;
    }


    /**
     * URL 매개변수로 전달된 채널 ID를 설정합니다.
     * 채널이 존재하지 않으면 로비로 리다이렉트합니다.
     *
     * @param event     BeforeEvent 객체
     * @param channelId 채널 ID
     */
    @Override
    public void setParameter(BeforeEvent event, String channelId) {
        channelService.getChannel(channelId)
                .ifPresentOrElse(
                        channel -> this.channelName = channel.name(),
                        () -> event.forwardTo(LobbyView.class)
                );
        this.channelId = channelId;
        receivedMessages.clear();
        var subscription = subscribe();
        addDetachListener(v -> subscription.dispose());
    }

    /**
     * 사용자가 입력한 메시지를 전송합니다. <br>
     * 예외 처리 발생 시 (토큰 제한 초과, rate limit 초과 등), 마지막으로 입력한 메세지를 삭제합니다.
     *
     * @param event 메시지 제출 이벤트
     */
    private void sendMessage(MessageInput.SubmitEvent event) {
        var message = event.getValue();
        if (message.isBlank() || message.length() <= 1) {
            return;
        }
        var uploadedFile = (Resource) VaadinSession.getCurrent().getAttribute("uploadedFile");
        VaadinSession.getCurrent().setAttribute("uploadedFile", null);

        CompletableFuture
                .runAsync(() -> chatService.postMessage(channelId, message, Author.USER))
                .thenAccept((result -> chatService.answerMessage(channelId, message, uploadedFile)))
                .exceptionally(ex -> {
                    messageService.deleteLastUserMessage(channelId);
                    getUI().ifPresent(ui -> ui.access(() -> {
                        Notification notification = Notification.show(
                                "오류 발생: " + ex.getMessage(), 3000, Notification.Position.TOP_CENTER);
                        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                        notification.open();
                    }));
                    receivedMessages.getLast().ifPresent(receivedMessages::remove);
                    receiveMessages(List.of());
                    return null;
                })
                .thenRun(() -> getUI().ifPresent(ui -> ui.access(upload::clearFileList)));
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

    /**
     * 현재 채널의 메시지를 구독합니다.
     * 이전 메시지 기록을 불러오고 실시간 메시지 업데이트를 시작합니다.
     *
     * @return 구독 객체
     */
    private Disposable subscribe() {
        var subscription = messageService.getLiveMessages(channelId).subscribe(this::receiveMessages);
        var lastSeenMessageId = receivedMessages.getLast().map(Message::messageId).orElse(null);
        receiveMessages(messageService.getMessageHistory(channelId, HISTORY_SIZE, lastSeenMessageId));
        return subscription;
    }

    /**
     * 수신된 메시지를 처리하고 UI를 업데이트합니다.
     *
     * @param incoming 수신된 메시지 리스트
     */
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