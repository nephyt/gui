package com.pingpong.ui.view;

import com.pingpong.basicclass.player.Player;
import com.pingpong.basicclass.servicecount.AllServiceCount;
import com.pingpong.basicclass.servicecount.ServiceCount;
import com.pingpong.basicclass.stats.PlayerStats;
import com.pingpong.basicclass.stats.PlayersStats;
import com.pingpong.ui.servicesrest.ServicesRest;
import com.pingpong.ui.util.Utils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.KeyNotifier;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.IFrame;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Push
@Route("")
@PreserveOnRefresh
public class MainView extends VerticalLayout implements KeyNotifier {

    private PlayerEditor editor = new PlayerEditor();

    private Grid<Player> grid;

    private Button addNewBtn;

    PlayersStats playersStats;
    AllServiceCount serviceCount;


    public MainView() {

        Tab tabPlayer = new Tab("Players");
        Tab tabGame = new Tab("Game");
        Tab tabGamePaused = new Tab("Game Paused");

        Tabs tabs = new Tabs(tabPlayer, tabGamePaused, tabGame);
        tabs.setWidthFull();
        tabs.setFlexGrowForEnclosedTabs(1);

        Div pagePlayers = buildPagePlayers();
        Div pageGame = buildPageGame();
        PausedGameVIew pageGamePaused = new PausedGameVIew(pageGame, tabs);

        Map<Tab, Component> tabsToPages = new HashMap<>();
        tabsToPages.put(tabPlayer, pagePlayers);
        tabsToPages.put(tabGamePaused, pageGamePaused);
        tabsToPages.put(tabGame, pageGame);

        Div pages = new Div(pagePlayers, pageGamePaused, pageGame);
        pages.setWidthFull();

        tabs.addSelectedChangeListener(event -> {
            tabsToPages.values().forEach(page -> page.setVisible(false));
            Component selectedPage = tabsToPages.get(tabs.getSelectedTab());
            selectedPage.setVisible(true);

            if (tabs.getSelectedIndex() == 0) {
                fillGrid("");
            }
            if (tabs.getSelectedIndex() == 1) {
                ((PausedGameVIew)selectedPage).fillGrid();
            }

        });

        add(tabs, pages);

    }

    private Div buildPagePlayers() {

        addNewBtn = new Button("New player", VaadinIcon.PLUS.create());

        Div pagePlayers = new Div();

        setupGridPlayers();

        TextField filter = new TextField();
        filter.setPlaceholder("Filter by last name");
        filter.setValueChangeMode(ValueChangeMode.EAGER);
        filter.addValueChangeListener(e -> fillGrid(e.getValue()));


        HorizontalLayout actions = new HorizontalLayout(filter, addNewBtn);


        pagePlayers.add(actions,grid, editor);

        pagePlayers.setWidthFull();


        // Instantiate and edit new Customer the new button is clicked
        addNewBtn.addClickListener(e -> editor.editCustomer(new Player()));

        // Listen changes made by the editor, refresh data from backend
        editor.setChangeHandler(() -> {
            editor.setVisible(false);
            fillGrid(filter.getValue());
        });

        fillGrid("");

        return pagePlayers;
    }

    private void setupGridPlayers() {

        grid = new Grid<>(Player.class);

        grid.removeColumnByKey("creationDate");
        grid.removeColumnByKey("picture");
        grid.removeColumnByKey("victorySongPath");
        grid.removeColumnByKey("youtubeEmbedVictorySongPath");
        grid.removeColumnByKey("id");

        grid.addComponentColumn(player -> {
            Image result = new Image();
            result.setWidth("60px");
            result.setHeight("60px");
            if (player.getPicture() != null) {
                StreamResource resource = new StreamResource("dummyImageName.jpg", () -> new ByteArrayInputStream(player.getPicture()));

                result.setSrc(resource);
            }
            return result;
        }).setHeader("Picture").setKey("picture");

        grid.addComponentColumn(player -> {
            IFrame result = new IFrame();
            result.setWidth("60px");
            result.setHeight("60px");
            if (player.getVictorySongPath() != null) {
                String victorySong = player.getYoutubeEmbedVictorySongPath();

                if (victorySong != null) {
                    result.setSrc(victorySong);
                }

            }
            return result;
        }).setHeader("Victory Song").setKey("song");

        grid.addColumn(player -> {
            PlayerStats stats = playersStats.getPlayerStatsForPlayer(player.getId());

            String result = stats.getNumberOfGamePlayed() + "/";
            result += stats.getNumberOfGameWin() + "/";
            result += stats.getNumberOfGameLost();

            return result;
        }).setHeader("#Games/W/L").setKey("gamePlayed");


        grid.addColumn(player -> {
            ServiceCount stats = serviceCount.getServiceCountForPlayer(player.getId());

            String result = stats.getBallServe() + "/";
            result += stats.getBallServeWin() + "/";
            result += stats.getBallServeFail();

            return result;
        }).setHeader("#BallsServe/W/L").setKey("ballServe");

        grid.addColumn(player -> {
            PlayerStats stats = playersStats.getPlayerStatsForPlayer(player.getId());

            return Utils.formatTimePlayed(stats.getTimePlayed());

        }).setHeader("Time").setKey("time");


        List<Grid.Column<Player>> orderColumn = new ArrayList<>();

        orderColumn.add(grid.getColumnByKey("picture"));
        orderColumn.add(grid.getColumnByKey("name"));
        orderColumn.add(grid.getColumnByKey("song"));
        orderColumn.add(grid.getColumnByKey("status"));
        orderColumn.add(grid.getColumnByKey("gamePlayed"));
        orderColumn.add(grid.getColumnByKey("ballServe"));
        orderColumn.add(grid.getColumnByKey("time"));

        grid.setColumnOrder(orderColumn);

        grid.setWidthFull();

        // Connect selected Customer to editor or hide if none is selected
        grid.asSingleSelect().addValueChangeListener(e -> {
            editor.editCustomer(e.getValue());
        });
    }

    private Div buildPageGame() {
/*
        byte[] targetArray = FileUtils.readFileToByteArray(new File(player.getPicturePath()));
        StreamResource resource = new StreamResource("dummyImageName.jpg", () -> new ByteArrayInputStream(targetArray));

        image.setSrc(resource);
        image.setVisible(true);
*/

        Div pageGame = new Div();
        pageGame.setWidthFull();
        pageGame.setVisible(false);


        GameSetting gameSetting = new GameSetting(ServicesRest.listPlayer(""), pageGame);
        gameSetting.setVisible(true);

        pageGame.add(gameSetting);

        return pageGame;

    }

    private void fillGrid(String filterText) {
        if ("".equals(filterText)){
            playersStats = ServicesRest.getPlayersStats();
            serviceCount = ServicesRest.getPlayerCountService();
        }
        grid.setItems(ServicesRest.listPlayer(filterText));
    }


}