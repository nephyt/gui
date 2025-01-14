package com.pingpong.ui.view;

import com.pingpong.basicclass.enumeration.GameStatus;
import com.pingpong.basicclass.game.Game;
import com.pingpong.basicclass.player.Player;
import com.pingpong.ui.services.MqttListener;
import com.pingpong.ui.services.ServicesButtons;
import com.pingpong.ui.services.ServicesRest;
import com.pingpong.ui.util.Utils;
import com.pingpong.ui.web.controller.GameController;
import com.pingpong.ui.web.controller.GameSettingController;
import com.pingpong.ui.web.controller.WinnerScreenController;
import com.vaadin.flow.component.html.Div;

import java.util.List;
import java.util.Map;

public class PageGame extends Div {

    GameSetting gameSetting = new GameSetting(ServicesRest.listPlayer(""), this);

    GameScore gameScore = new GameScore();

    WinnerScreen winnerScreen = new WinnerScreen(this);

    public PageGame() {
        setWidthFull();
        setVisible(false);

        gameSetting.setVisible(true);
        gameScore.setVisible(false);

        add(gameSetting);
        add(gameScore);
        add(winnerScreen);
    }

    public void refreshSoundList() {
        System.out.println("Updating the scoring sound with all the sound");
        gameSetting.refreshPointSound();
    }

    public void refreshStatePage() {
        if (gameSetting.isVisible()) {
            ServicesButtons.getInstance().playerSelection();

            GameSettingController.setGameSetting(gameSetting);
            MqttListener.setStateGameSetting();

            System.out.println("Set game score a null dans refreshStatePage");
            GameController.setGameScore(null);
            WinnerScreenController.setWinnerScreen(null);

            if (Utils.getNeedUpdate()) {
                Utils.setNeedUpdate(false);
                List<Player> listPlayers = ServicesRest.listPlayer("");
                gameSetting.getPlayerSelectorTeamA().refreshListPlayer(listPlayers);
                gameSetting.getPlayerSelectorTeamB().refreshListPlayer(listPlayers);
            }
        } else if (gameScore.isVisible()) {
            GameSettingController.setGameSetting(null);

            System.out.println("Set game score dans refreshStatePage");
            GameController.setGameScore(gameScore);
            MqttListener.setStateGameScore();
            WinnerScreenController.setWinnerScreen(null);

            if (GameStatus.ACTIVE.getCode().equals(gameScore.getGame().getGameStatus().getCode())) {
                ServicesButtons.getInstance().startServerModeButton(gameScore.getGame().determineServerState());
            } else if (GameStatus.PAUSE.getCode().equals(gameScore.getGame().getGameStatus().getCode())) {
                ServicesButtons.getInstance().pauseGame();
            }
        } else if (winnerScreen.isVisible()) {
            GameSettingController.setGameSetting(null);
            //GameController.setGameScore(null); will be set to null in winner screen
            WinnerScreenController.setWinnerScreen(winnerScreen);
            MqttListener.setStateWinnerScreen();

            ServicesButtons.getInstance().startModeWinner(gameScore.getGame());
        }
    }

    public void initialiseGameScore(Game gameToManage, Map<Integer, Player> mapPlayerTeamA, Map<Integer, Player> mapPlayerTeamB) {

        gameScore.initGameScore(this, gameToManage, mapPlayerTeamA, mapPlayerTeamB);
        gameScore.refreshScreen(true);
    }

    public void showGameSetting() {

        remove(winnerScreen);
        gameSetting.setVisible(true);
        gameScore.setVisible(false);

        refreshStatePage();
    }

    public void showGameScore(boolean fromResumeGame) {
        remove(winnerScreen);
        gameScore.setVisible(true);
        gameSetting.setVisible(false);

        if (!fromResumeGame) {
            //refresh state page will be done by the tab selection
            refreshStatePage();
        }

    }

    public void showWinnerScreen() {
        gameScore.setVisible(false);
        add(winnerScreen);
        winnerScreen.showWinner(gameScore.getGame(), gameScore.getDisplayTeamA(), gameScore.getDisplayTeamB(), gameScore.isMute());
        winnerScreen.setVisible(true);

        refreshStatePage();
    }

}
