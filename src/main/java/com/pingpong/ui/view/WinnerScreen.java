package com.pingpong.ui.view;

import com.pingpong.basicclass.game.Game;
import com.pingpong.basicclass.game.GameTime;
import com.pingpong.basicclass.game.Team;
import com.pingpong.basicclass.player.Player;
import com.pingpong.ui.servicesrest.ServicesRest;
import com.pingpong.ui.util.Utils;
import com.pingpong.ui.web.controller.GameController;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.concurrent.TimeUnit;

public class WinnerScreen extends VerticalLayout {

    Div pageGame;

    public WinnerScreen(Div pageGame) {
        this.pageGame = pageGame;

        pageGame.removeAll();

        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        Utils.disableSelection(this);
    }

    public void showWinner(Game game, DisplayTeam displayTeamA, DisplayTeam displayTeamB) {

        Integer winnerTeamId = game.getTeamWinnerId();
        Player winner1;
        Player winner2 = null;

        DisplayTeam loserDisplayTeam;
        Team loserTeam;

        String finalScoreStr = "";

        if (winnerTeamId == game.getTeamA().getId()) {
            finalScoreStr = game.getTeamA().getScore() + " - " + game.getTeamB().getScore();
            loserDisplayTeam = displayTeamB;
            loserTeam = game.getTeamB();

            if (game.getTeamA().getRightPlayer() != null) {
                winner1 = displayTeamA.getPlayerById(game.getTeamA().getRightPlayer());

                if (game.getTeamA().getLeftPlayer() != null) {
                    winner2 = displayTeamA.getPlayerById(game.getTeamA().getLeftPlayer());
                }
            } else {
                winner1 = displayTeamA.getPlayerById(game.getTeamA().getLeftPlayer());
                if (game.getTeamA().getLeftPlayer() != null) {
                    winner1 = displayTeamA.getPlayerById(game.getTeamA().getLeftPlayer());
                }
            }
        } else {
            finalScoreStr = game.getTeamB().getScore() + " - " + game.getTeamA().getScore();
            loserDisplayTeam = displayTeamA;
            loserTeam = game.getTeamA();
            if (game.getTeamB().getRightPlayer() != null) {
                winner1 = displayTeamB.getPlayerById(game.getTeamB().getRightPlayer());

                if (game.getTeamB().getLeftPlayer() != null) {
                    winner2 = displayTeamB.getPlayerById(game.getTeamB().getLeftPlayer());
                }
            } else {
                winner1 = displayTeamB.getPlayerById(game.getTeamB().getLeftPlayer());
            }
        }


        String playersNames = winner1.getName();

        if (winner2 != null) {
            playersNames += " & " + winner2.getName();
        }
        Html winnerName = new Html("<font>" + playersNames + "</font>");
        winnerName.getElement().getStyle().set("font-size", "49px");


        Html finalScore = new Html("<font>" + finalScoreStr + "</font>");
        finalScore.getElement().getStyle().set("font-size", "30px");

        String loserNameStr = "";
        if (loserTeam.getRightPlayer() != null) {
            loserNameStr = loserDisplayTeam.getPlayerById(loserTeam.getRightPlayer()).getName();
        }
        if (loserTeam.getLeftPlayer() != null) {
            if (!"".equals(loserNameStr)) {
                loserNameStr += " & ";
            }
            loserNameStr += loserDisplayTeam.getPlayerById(loserTeam.getLeftPlayer()).getName();
        }

        Html loserName = new Html("<font>" + loserNameStr + "</font>");
        loserName.getElement().getStyle().set("font-size", "24px");



        Html time = new Html("<font>Time : " + getTimePlayed(game) + "</font>");
        loserName.getElement().getStyle().set("font-size", "24px");


        Image winnerImg = new Image();
        winnerImg.setWidth("25%");
        winnerImg.setSrc("winner/winner1.jpg");


        add(winnerName);
        add(finalScore);
        add(loserName);
        add(time);
      //  add(winnerImg);

        DisplayPlayer winnerDisplaySong = new DisplayPlayer();
        add(winnerDisplaySong.getVictorySong(winner1));

        HorizontalLayout buttonsDiv = new HorizontalLayout();

        Button rematch = new Button("Rematch");
        Button changePlayers = new Button("Change Players");

        buttonsDiv.add(rematch);
        buttonsDiv.add(changePlayers);
        rematch.addClickListener(e -> rematchGame(game, displayTeamA, displayTeamB));
        changePlayers.addClickListener(e -> changePlayers());

        add(buttonsDiv);

        pageGame.add(this);



    }

    private String getTimePlayed(Game game) {
        long millis = 0;

        for (GameTime time : game.getGameTime()) {
               millis += (time.getEndDate().getTime() - time.getCreationDate().getTime());

        }

        return String.format("%02d min, %02d sec",
                TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
        );
    }

    private Team createTeam(Integer player1, Integer player2, boolean hasService) {
        return new Team(player1, player2, hasService);
    }

    private boolean teamWin(Integer idTeamWin, Integer idTeam) {
        return idTeamWin == idTeam;
    }

    private Team createNewTeam(Integer teamWinId, Team team) {
        return createTeam(team.getTeamPlayer1Id(), team.getTeamPlayer2Id(), teamWin(teamWinId, team.getId()));
    }

    private void changePlayers() {
        pageGame.removeAll();


        GameController.setGameScore(null);

        pageGame.add(new GameSetting(ServicesRest.listPlayer(""), pageGame));

    }

    private void rematchGame(Game game, DisplayTeam displayTeamA, DisplayTeam displayTeamB) {

        GameController.setGameScore(null);
        pageGame.removeAll();

        Team teamA = createNewTeam(game.getTeamWinnerId(), game.getTeamA());
        Team teamB = createNewTeam(game.getTeamWinnerId(), game.getTeamB());

        Game newGame = new Game(teamA, teamB,game.getMaxScore());

        Game gameInProgress = ServicesRest.saveGame(newGame);

        GameScore gameScore = new GameScore(pageGame, gameInProgress, displayTeamA, displayTeamB);
        gameScore.setVisible(true);

        gameScore.refreshScreen();

        pageGame.add(gameScore);


    }

}
