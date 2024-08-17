package com.sangminlee.mymydata.views;

import com.sangminlee.mymydata.service.ChannelService;
import com.sangminlee.mymydata.vo.Channel;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.lumo.LumoUtility;

public class MainLayout extends AppLayout {

    private final ChannelService channelService;
    private H2 viewTitle;


    public MainLayout(ChannelService channelService) {
        this.channelService = channelService;
        setPrimarySection(Section.DRAWER);
        addNavbarContent();
        addDrawerContent();
    }

    private void addNavbarContent() {
        var toggle = new DrawerToggle();
        toggle.setAriaLabel("Menu toggle");
        toggle.setTooltipText("Menu toggle");

        viewTitle = new H2();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE,
                LumoUtility.Flex.GROW);

        var header = new Header(toggle, viewTitle);
        header.addClassNames(
                LumoUtility.AlignItems.CENTER,
                LumoUtility.Display.FLEX,
                LumoUtility.Padding.End.MEDIUM,
                LumoUtility.Width.FULL
        );

        addToNavbar(false, header);
    }

    private void addDrawerContent() {
        var appName = new Span("마이데이터 어시스턴트 Chat");
        appName.addClassNames(
                LumoUtility.AlignItems.CENTER, LumoUtility.Display.FLEX,
                LumoUtility.FontSize.LARGE, LumoUtility.FontWeight.SEMIBOLD,
                LumoUtility.Height.XLARGE, LumoUtility.Padding.Horizontal.MEDIUM
        );
        Scroller scroller = new Scroller(createChannelNav());
        scroller.addClassNames(LumoUtility.Padding.SMALL);

        addToDrawer(appName, scroller);
    }

    private SideNav createChannelNav() {
        SideNav nav = new SideNav();

        nav.addItem(new SideNavItem("Lobby", LobbyView.class, VaadinIcon.BUILDING.create()));

        channelService.getAllChannels().forEach(channel -> {
            RouterLink link = new RouterLink(channel.name(), ChannelView.class, channel.id());
            nav.addItem(new SideNavItem(channel.name(), link.getHref()));
        });
        return nav;
    }

    private String getCurrentPageTitle() {
        PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
        return title == null ? "" : title.value();
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        viewTitle.setText(getCurrentPageTitle());
    }


}