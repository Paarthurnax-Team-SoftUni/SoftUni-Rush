package controllers;

import utils.constants.*;
import dataHandler.PlayerData;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Ellipse;
import javafx.stage.Stage;
import utils.stages.StageManager;
import utils.stages.StageManagerImpl;

import java.lang.reflect.Field;

public class ChooseCarController {

    @FXML
    private Button goNextBtn;
    @FXML
    private Ellipse backgroundBox1;
    @FXML
    private Ellipse backgroundBox2;
    @FXML
    private Ellipse backgroundBox3;
    @FXML
    private Ellipse backgroundBox4;
    @FXML
    private Ellipse backgroundBox5;
    @FXML
    private Ellipse backgroundBox6;
    @FXML
    private ImageView locked1;
    @FXML
    private ImageView locked2;
    @FXML
    private ImageView locked3;
    @FXML
    private ImageView locked4;
    @FXML
    private ImageView locked5;
    @FXML
    private ImageView locked6;

    public void initialize() throws NoSuchFieldException, IllegalAccessException {
        long highScores = PlayerData.getInstance().getCurrentPlayer().getHighScore();
        showUnlockedCarsOnly(highScores);
    }

    @FXML
    private void goToChooseMode(ActionEvent actionEvent) {
        Stage currentStage = (Stage) this.goNextBtn.getScene().getWindow();
        StageManager manager = new StageManagerImpl();
        FXMLLoader loader = manager.loadSceneToStage(currentStage, ViewsConstants.CHOOSE_MODE_VIEW_PATH);
    }

    @FXML
    private void chooseCar(MouseEvent ev) throws NoSuchFieldException, IllegalAccessException {
        Node source = (Node) ev.getSource();
        String id = source.getId();
        backgroundFill(id.substring(id.length() - CarConstants.CARS_LIST_LENGTH_OFFSET));
        if (id.substring(0, 3).equals(CarConstants.CAR_STRING)) {
            PlayerData.getInstance().updateCarId(id);
        } else if (source.getId().substring(0, 5).equals(ImagesShortcutConstants.LABEL_STRING)) {
            PlayerData.getInstance().updateCarId(CarConstants.CAR_STRING + id.substring(id.length() - 1));
        }
        this.goNextBtn.setVisible(true);
    }

    @FXML
    private void showUnlockedCarsOnly(Long points) throws NoSuchFieldException, IllegalAccessException {
        Class<ChooseCarController> chooseCarControllerClass = ChooseCarController.class;
        for (int id = 1; id <= CarConstants.CARS_LIST.length; id++) {
            Field ellipseField = chooseCarControllerClass.getDeclaredField(ImagesShortcutConstants.BACKGROUND_STRING + id);
            Ellipse ellipse = ((Ellipse) ellipseField.get(this));
            ellipse.setStyle(null);
            Field lockedField = chooseCarControllerClass.getDeclaredField(ImagesShortcutConstants.LOCKED_CAR_STRING + id);
            ImageView locked = ((ImageView) lockedField.get(this));
            locked.setVisible(false);
            if (points < (id - 1) * GameplayConstants.CAR_UNLOCK_STEP_PTS) {
                ellipse.setStyle(StylesConstants.GREY_COLOUR);
                ellipse.toFront();
                locked.setVisible(true);
            }
        }
    }

    @FXML
    private void backgroundFill(String id) throws NoSuchFieldException, IllegalAccessException {
        long highScores = PlayerData.getInstance().getCurrentPlayer().getHighScore();
        showUnlockedCarsOnly(highScores);

        Class<ChooseCarController> chooseCarControllerClass = ChooseCarController.class;
        Field field = chooseCarControllerClass.getDeclaredField("backgroundBox" + id);
        ((Ellipse) field.get(this)).setStyle(StylesConstants.RED_COLOUR);
    }
}