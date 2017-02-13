package Controllers;

import GameLogic.Game;
import MapHandlers.Track;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

import static Controllers.ScreenController.closeStage;

public class LoseController {

    @FXML
    private Button newGameBtn;
    @FXML
    private Button quitBtn;
    @FXML
    private AnchorPane gameOverPage;

    public void restartGame(ActionEvent actionEvent) throws IOException {
        Stage stage = (Stage) quitBtn.getScene().getWindow();
        Game.clearObstaclesAndCollectibles();
        closeStage(stage);
        Track.initializeLevel(1);
    }

    public void quitGame(ActionEvent actionEvent) {
        Platform.exit();
    }
}
