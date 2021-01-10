package com.pingpong.ui.view;

import com.pingpong.basicclass.game.Game;
import com.pingpong.basicclass.game.Team;
import com.pingpong.basicclass.player.Player;
import com.pingpong.ui.servicesrest.ServicesRest;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.tabs.Tabs;

import java.util.HashMap;
import java.util.Map;

public class PausedGameVIew extends Div {

    private Button resumeGameBtn = new Button("Resume Game");
    Div pageGame;

    Map<Integer, Player> mapPlayer;

    private Game gameSelected = null;
    private Tabs tabs;

    public PausedGameVIew(Div pageGame, Tabs tabs) {
        this.pageGame = pageGame;
        this.tabs = tabs;

        setupGrid();

        resumeGameBtn.addClickListener(e -> resumeGame());
        resumeGameBtn.setVisible(false);
        add(resumeGameBtn);

        setVisible(false);

    }

    private void resumeGame() {
        if (gameSelected != null) {

            pageGame.removeAll();
            gameSelected.resumeGame();

            GameScore gameScore = new GameScore(pageGame, gameSelected, new DisplayTeam(getMapPlayerTeam(gameSelected.getTeamA(), mapPlayer)), new DisplayTeam(getMapPlayerTeam(gameSelected.getTeamB(), mapPlayer)));
            gameScore.refreshScreen();
            pageGame.add(gameScore);

            tabs.setSelectedIndex(2);
        }
    }


    private Map<Integer, Player> getMapPlayerTeam(Team team, Map<Integer, Player> mapPlayer) {
        Map<Integer, Player> map = new HashMap<>();

        map.put(team.getTeamPlayer1().getPlayerId(), mapPlayer.get(team.getTeamPlayer1().getPlayerId()));

        if (team.getTeamPlayer2() != null) {
            map.put(team.getTeamPlayer2().getPlayerId(), mapPlayer.get(team.getTeamPlayer2().getPlayerId()));
        } else {
            map.put(null, new Player());
        }

        return map;
    }

    private void setupGrid() {
        Grid<Game> grid = new Grid<>(Game.class);

        grid.setItems(ServicesRest.getPausedGames());

        grid.removeColumnByKey("gameTime");
        grid.removeColumnByKey("id");
        grid.removeColumnByKey("teamA");
        grid.removeColumnByKey("teamB");
        grid.removeColumnByKey("teamWinnerId");

        mapPlayer = ServicesRest.mapPlayer("");


        grid.addColumn(game -> getTeamPlayerName(mapPlayer, game.getTeamA()) + " VS " + getTeamPlayerName(mapPlayer, game.getTeamB()))
                .setAutoWidth(true).setSortable(true).setHeader("Players").setKey("players");

        grid.addColumn(game -> game.getTeamA().getScore() + " - " + game.getTeamB().getScore())
                .setAutoWidth(true).setSortable(true).setHeader("Score").setKey("score");

       // grid.addColumn(game -> getTeamPlayerName(mapPlayer, game.getTeamB()))
       //         .setHeader("Team B").setKey("teamB");

        grid.addColumn(game -> game.toStringLastTimePlayed())
                .setSortable(true).setHeader("Last Time Played").setKey("lastTimePlayed");

        grid.addColumn(game -> game.toStringTimePlayed())
                .setSortable(true).setHeader("Time Played").setKey("timePlayed");


        // Connect selected Customer to editor or hide if none is selected
        grid.asSingleSelect().addValueChangeListener(e -> {
            resumeGameBtn.setVisible(true);
            gameSelected = e.getValue();
        });

        add(grid);
    }

    private String getTeamPlayerName(Map<Integer, Player> mapPlayer, Team teamA) {
        String result = mapPlayer.get(teamA.getTeamPlayer1().getPlayerId()).getName();

        if (teamA.getTeamPlayer2() != null) {
            result += " & " + mapPlayer.get(teamA.getTeamPlayer2().getPlayerId()).getName();
        }
        return result;
    }

}
