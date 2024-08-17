package com.sangminlee.mymydata.views;

import com.sangminlee.mymydata.service.ChannelService;
import com.sangminlee.mymydata.vo.Channel;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.virtuallist.VirtualList;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Lobby")
public class LobbyView extends VerticalLayout {

    private final ChannelService channelService;
    private final VirtualList<Channel> channels;
    private final TextField channelNameField;
    private final Button addChannelButton;

    public LobbyView(ChannelService channelService) {
        this.channelService = channelService;
        setSizeFull();

        channels = new VirtualList<>();
        channels.setRenderer(new ComponentRenderer<>(this::createChannelComponent));
        add(channels);
        expand(channels);

        channelNameField = new TextField();
        channelNameField.setPlaceholder("New channel name");

        addChannelButton = new Button("Add channel", event -> addChannel());
        addChannelButton.setDisableOnClick(true);

        var toolbar = new HorizontalLayout(channelNameField, addChannelButton);
        toolbar.setWidthFull();
        toolbar.expand(channelNameField);
        add(toolbar);
    }

    private Component createChannelComponent(Channel channel) {
        return new RouterLink(channel.name(), ChannelView.class, channel.id());
    }

    private void refreshChannels() {
        channels.setItems(channelService.getAllChannels());
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        refreshChannels();
    }

    private void addChannel() {
        try {
            var nameOfNewChannel = channelNameField.getValue();
            if (!nameOfNewChannel.isBlank()) {
                channelService.createChannel(nameOfNewChannel);
                channelNameField.clear();
                refreshChannels();
            }
            UI.getCurrent().getPage().reload();
        } finally {
            addChannelButton.setEnabled(true);
        }
    }
}